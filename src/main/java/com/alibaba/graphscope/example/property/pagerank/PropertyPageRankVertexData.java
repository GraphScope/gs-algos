/*
 * Copyright 2021 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.graphscope.example.property.pagerank;

import com.alibaba.graphscope.app.DefaultPropertyAppBase;
import com.alibaba.graphscope.app.ParallelPropertyAppBase;
import com.alibaba.graphscope.communication.Communicator;
import com.alibaba.graphscope.context.PropertyDefaultContextBase;
import com.alibaba.graphscope.context.PropertyParallelContextBase;
import com.alibaba.graphscope.ds.PropertyNbrUnit;
import com.alibaba.graphscope.ds.PropertyRawAdjList;
import com.alibaba.graphscope.ds.Vertex;
import com.alibaba.graphscope.ds.VertexRange;
import com.alibaba.graphscope.ds.adaptor.AdjList;
import com.alibaba.graphscope.ds.adaptor.Nbr;
import com.alibaba.graphscope.fragment.ArrowFragment;
import com.alibaba.graphscope.parallel.ParallelEngine;
import com.alibaba.graphscope.parallel.ParallelPropertyMessageManager;
import com.alibaba.graphscope.parallel.PropertyMessageManager;
import com.alibaba.graphscope.parallel.message.DoubleMsg;
import com.alibaba.graphscope.utils.DoubleArrayWrapper;
import com.alibaba.graphscope.utils.FFITypeFactoryhelper;
import com.alibaba.graphscope.utils.TriConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyPageRankVertexData extends Communicator
    implements DefaultPropertyAppBase<Long, PropertyPageRankVertexDataContext>,
    ParallelEngine {
    private static Logger logger =
        LoggerFactory.getLogger(PropertyPageRankVertexData.class.getName());

    @Override
    public void PEval(
        ArrowFragment<Long> fragment,
        PropertyDefaultContextBase<Long> context,
        PropertyMessageManager messageManager) {
        PropertyPageRankVertexDataContext ctx =
            (PropertyPageRankVertexDataContext) context;
        ctx.superStep = 0;

        VertexRange<Long> innerVertices = fragment.innerVertices(0);
        int totalVertexNum = (int) fragment.getTotalVerticesNum();
        ctx.superStep = 0;
        double base = 1.0 / totalVertexNum;
        double local_dangling_sum = 0.0;

        for (Vertex<Long> vertex : fragment.innerVertices(0).locals()) {
            PropertyRawAdjList<Long> nbrs = fragment.getOutgoingRawAdjList(vertex, 0);
            ctx.degree.set(vertex.GetValue(), nbrs.size());
            DoubleMsg msg = DoubleMsg.factory.create();
            if (nbrs.size() > 0) {
                ctx.pagerank.setValue(
                    vertex, base / ctx.degree.get(vertex.GetValue()));
                msg.setData(ctx.pagerank.get(vertex));
                messageManager.sendMsgThroughOEdges(fragment, vertex, 0,msg);
            } else {
                ctx.pagerank.setValue(vertex, base);
                local_dangling_sum += base;
            }
        }
        DoubleMsg msgDanglingSum = FFITypeFactoryhelper.newDoubleMsg(0.0);
        DoubleMsg localSumMsg = FFITypeFactoryhelper.newDoubleMsg(local_dangling_sum);
        sum(localSumMsg, msgDanglingSum);
        ctx.danglingSum = msgDanglingSum.getData();

        messageManager.ForceContinue();
    }

    @Override
    public void IncEval(
        ArrowFragment<Long> fragment,
        PropertyDefaultContextBase<Long> context,
        PropertyMessageManager messageManager) {
        PropertyPageRankVertexDataContext ctx =
            (PropertyPageRankVertexDataContext) context;
        long innerVertexNum = fragment.getInnerVerticesNum(0);
        //
        ctx.superStep = ctx.superStep + 1;
        VertexRange<Long> innerVertices = fragment.innerVertices(0);
        int totalVertexNum = fragment.getTotalVerticesNum(0);

        {
            Vertex<Long> vertex=  FFITypeFactoryhelper.newVertexLong();
            if (ctx.superStep > ctx.maxIteration) {
                for (long i = 0; i < innerVertexNum; ++i) {
                    if (ctx.degree.get(i) != 0) {
                        vertex.SetValue(i);
                        ctx.pagerank.setValue(vertex, ctx.degree.get((int)i) * ctx.pagerank.get(vertex));
                    }
                }
                return;
            }
        }

        double base =
            (1.0 - ctx.delta) / totalVertexNum
                + ctx.delta * ctx.danglingSum / totalVertexNum;

        double local_dangling_sum = 0.0;

        DoubleArrayWrapper nextResult = new DoubleArrayWrapper((int)innerVertexNum, 0.0);
        // System.out.println("dangling sum: " + context.danglingSum);
        // msgs are all out vertex in this frag, and has incoming edges to the vertex in this frag
        {
            Vertex<Long> vertex = innerVertices.begin();
            DoubleMsg msg = DoubleMsg.factory.create();
            while (messageManager.getMessage(fragment, vertex, msg)) {
                ctx.pagerank.setValue(vertex, msg.getData());
            }
        }

        for (Vertex<Long> vertex : fragment.innerVertices(0).locals()) {
            if (ctx.degree.get(vertex.GetValue()) == 0) {
                nextResult.set(vertex, base);
                local_dangling_sum += base;
            } else {
                double cur = 0.0;
                PropertyRawAdjList<Long> nbrs = fragment.getIncomingRawAdjList(vertex, 0);
                for (PropertyNbrUnit<Long> nbr : nbrs.iterator()) {
                    cur += ctx.pagerank.get(nbr.getNeighbor());
                }
                cur = (ctx.delta * cur + base) / ctx.degree.get(vertex.GetValue());
                nextResult.set(vertex.GetValue(), cur);
            }
        }
        DoubleMsg msg = DoubleMsg.factory.create();
        for (Vertex<Long> vertex : fragment.innerVertices(0).locals()) {
            ctx.pagerank.setValue(vertex, nextResult.get(vertex.GetValue()));
            msg.setData(ctx.pagerank.get(vertex));
            messageManager.sendMsgThroughOEdges(fragment, vertex, 0, msg);
        }

        DoubleMsg msgDanglingSum = FFITypeFactoryhelper.newDoubleMsg(0.0);
        DoubleMsg localSumMsg = FFITypeFactoryhelper.newDoubleMsg(local_dangling_sum);
        sum(localSumMsg, msgDanglingSum);
        ctx.danglingSum = msgDanglingSum.getData();

        messageManager.ForceContinue();
    }
}
