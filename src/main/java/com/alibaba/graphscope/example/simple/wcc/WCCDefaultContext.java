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

package com.alibaba.graphscope.example.simple.wcc;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.graphscope.app.DefaultContextBase;
import com.alibaba.graphscope.ds.Vertex;
import com.alibaba.graphscope.ds.VertexRange;
import com.alibaba.graphscope.ds.VertexSet;
import com.alibaba.graphscope.fragment.SimpleFragment;
import com.alibaba.graphscope.parallel.DefaultMessageManager;
import com.alibaba.graphscope.utils.LongArrayWrapper;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WCCDefaultContext implements DefaultContextBase<Long, Long, Long, Double> {
    public VertexSet currModified, nextModified;
    public LongArrayWrapper comp_id;
    public int innerVerticesNum;

    @Override
    public void Init(
            SimpleFragment<Long, Long, Long, Double> frag,
            DefaultMessageManager messageManager,
            JSONObject jsonObject) {
        comp_id = new LongArrayWrapper(frag.vertices(), Long.MAX_VALUE);
        currModified = new VertexSet(frag.vertices());
        nextModified = new VertexSet(frag.vertices());
        innerVerticesNum = frag.getInnerVerticesNum().intValue();
    }

    @Override
    public void Output(SimpleFragment<Long, Long, Long, Double> frag) {
        String prefix = "/tmp/wcc_default_output";
        String filePath = prefix + "_frag_" + frag.fid();
        try {
            FileWriter fileWritter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWritter);
            VertexRange<Long> innerNodes = frag.innerVertices();

            Vertex<Long> cur = innerNodes.begin();
            for (long index = 0; index < frag.getInnerVerticesNum(); ++index) {
                cur.SetValue(index);
                Long oid = frag.getId(cur);
                bufferedWriter.write(oid + "\t" + comp_id.get(index) + "\n");
            }
            bufferedWriter.close();
            System.out.println("writing output to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
