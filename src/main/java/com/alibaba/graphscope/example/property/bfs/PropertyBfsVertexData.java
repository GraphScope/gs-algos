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

package com.alibaba.graphscope.example.property.bfs;

import com.alibaba.graphscope.app.DefaultPropertyAppBase;
import com.alibaba.graphscope.app.ParallelPropertyAppBase;
import com.alibaba.graphscope.context.PropertyDefaultContextBase;
import com.alibaba.graphscope.context.PropertyParallelContextBase;
import com.alibaba.graphscope.ds.EmptyType;
import com.alibaba.graphscope.ds.GrapeAdjList;
import com.alibaba.graphscope.ds.GrapeNbr;
import com.alibaba.graphscope.ds.PropertyAdjList;
import com.alibaba.graphscope.ds.PropertyNbrUnit;
import com.alibaba.graphscope.ds.PropertyRawAdjList;
import com.alibaba.graphscope.ds.Vertex;
import com.alibaba.graphscope.ds.VertexRange;
import com.alibaba.graphscope.example.property.sssp.ParallelPropertySSSPVertexData;
import com.alibaba.graphscope.fragment.ArrowFragment;
import com.alibaba.graphscope.parallel.ParallelEngine;
import com.alibaba.graphscope.parallel.ParallelPropertyMessageManager;
import com.alibaba.graphscope.parallel.PropertyMessageManager;
import com.alibaba.graphscope.utils.FFITypeFactoryhelper;
import com.google.common.base.Supplier;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyBfsVertexData
    implements DefaultPropertyAppBase<Long, PropertyBfsVertexDataContext>,
    ParallelEngine {

    private static Logger logger =
        LoggerFactory.getLogger(ParallelPropertySSSPVertexData.class.getName());

    @Override
    public void PEval(
        ArrowFragment<Long> fragment,
        PropertyDefaultContextBase<Long> context,
        PropertyMessageManager messageManager) {
        PropertyBfsVertexDataContext ctx = (PropertyBfsVertexDataContext) context;
        ctx.curDepth = 1;
        Vertex<Long> source = FFITypeFactoryhelper.newVertexLong();
        Vertex<Long> tmp = FFITypeFactoryhelper.newVertexLong();
        EmptyType emptyType = EmptyType.factory.create();
        if (fragment.getInnerVertex(0, (long) ctx.sourceOid, source)) {
            ctx.depth.set(source, 0);
            PropertyRawAdjList<Long> adjList = fragment.getOutgoingRawAdjList(source, 0);
            for (PropertyNbrUnit<Long> nbrUnit : adjList.iterator()) {
                long vid = nbrUnit.vid();
                tmp.SetValue(vid);
                if (ctx.depth.get(vid) == Long.MAX_VALUE) {
                    ctx.depth.set(vid, 1);
                    if (fragment.isOuterVertex(tmp)) {
                        messageManager.syncStateOnOuterVertex(fragment, tmp, emptyType);
                    } else {
                        ctx.curModified.set(vid);
                    }
                }
            }
        }
        messageManager.ForceContinue();
    }

    @Override
    public void IncEval(
        ArrowFragment<Long> fragment,
        PropertyDefaultContextBase<Long> context,
        PropertyMessageManager messageManager) {
        PropertyBfsVertexDataContext ctx = (PropertyBfsVertexDataContext) context;
        long nextDepth = ctx.curDepth + 1;
        ctx.nextModified.clear();

        EmptyType emptyType = EmptyType.factory.create();
        {
            Vertex<Long> vertex = FFITypeFactoryhelper.newVertexLong();
            while (messageManager.getMessage(fragment, vertex, emptyType)) {
                if (ctx.depth.get(vertex) == Long.MAX_VALUE) {
                    ctx.depth.set(vertex, ctx.curDepth);
                    ctx.curModified.set(vertex);
                }
            }
        }
        VertexRange<Long> innerVertices = fragment.innerVertices(0);
        for (Vertex<Long> cur : innerVertices.locals()) {
            if (ctx.curModified.get(cur)) {
                PropertyRawAdjList<Long> adjList = fragment.getOutgoingRawAdjList(cur, 0);
                for (PropertyNbrUnit<Long> nbr : adjList.iterator()) {
                    Vertex<Long> vertex = nbr.getNeighbor();
                    if (ctx.depth.get(vertex) == Long.MAX_VALUE) {
                        ctx.depth.set(vertex, nextDepth);
                        if (fragment.isOuterVertex(vertex)) {
                            messageManager.syncStateOnOuterVertex(fragment, vertex, emptyType);
                        } else {
                            ctx.nextModified.insert(vertex);
                        }
                    }
                }
            }
        }

        ctx.curDepth = nextDepth;

        if (!ctx.nextModified.empty()) {
            messageManager.ForceContinue();
        }
        ctx.curModified.assign(ctx.nextModified);
    }
}
