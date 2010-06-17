/*
 * Copyright 2009-2010 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.hyracks.coreops.group;

import java.nio.ByteBuffer;

import edu.uci.ics.hyracks.api.comm.IFrameWriter;
import edu.uci.ics.hyracks.api.dataflow.IActivityGraphBuilder;
import edu.uci.ics.hyracks.api.dataflow.IOperatorDescriptor;
import edu.uci.ics.hyracks.api.dataflow.IOperatorNodePullable;
import edu.uci.ics.hyracks.api.dataflow.IOperatorNodePushable;
import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparatorFactory;
import edu.uci.ics.hyracks.api.dataflow.value.ITuplePartitionComputerFactory;
import edu.uci.ics.hyracks.api.dataflow.value.RecordDescriptor;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.api.job.IOperatorEnvironment;
import edu.uci.ics.hyracks.api.job.JobPlan;
import edu.uci.ics.hyracks.api.job.JobSpecification;
import edu.uci.ics.hyracks.comm.io.FrameTupleAccessor;
import edu.uci.ics.hyracks.context.HyracksContext;
import edu.uci.ics.hyracks.coreops.base.AbstractActivityNode;
import edu.uci.ics.hyracks.coreops.base.AbstractOperatorDescriptor;

public class HashGroupOperatorDescriptor extends AbstractOperatorDescriptor {
    private static final String HASHTABLE = "hashtable";

    private static final long serialVersionUID = 1L;

    private final int[] keys;
    private final ITuplePartitionComputerFactory tpcf;
    private final IBinaryComparatorFactory[] comparatorFactories;
    private final IAccumulatingAggregatorFactory aggregatorFactory;
    private final int tableSize;

    public HashGroupOperatorDescriptor(JobSpecification spec, int[] keys, ITuplePartitionComputerFactory tpcf,
            IBinaryComparatorFactory[] comparatorFactories, IAccumulatingAggregatorFactory aggregatorFactory,
            RecordDescriptor recordDescriptor, int tableSize) {
        super(spec, 1, 1);
        this.keys = keys;
        this.tpcf = tpcf;
        this.comparatorFactories = comparatorFactories;
        this.aggregatorFactory = aggregatorFactory;
        recordDescriptors[0] = recordDescriptor;
        this.tableSize = tableSize;
    }

    @Override
    public void contributeTaskGraph(IActivityGraphBuilder builder) {
        HashBuildActivity ha = new HashBuildActivity();
        builder.addTask(ha);

        OutputActivity oa = new OutputActivity();
        builder.addTask(oa);

        builder.addSourceEdge(0, ha, 0);
        builder.addTargetEdge(0, oa, 0);
        builder.addBlockingEdge(ha, oa);
    }

    private class HashBuildActivity extends AbstractActivityNode {
        private static final long serialVersionUID = 1L;

        @Override
        public IOperatorNodePullable createPullRuntime(HyracksContext ctx, JobPlan plan, IOperatorEnvironment env,
                int partition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IOperatorNodePushable createPushRuntime(final HyracksContext ctx, JobPlan plan,
                final IOperatorEnvironment env, int partition) {
            final RecordDescriptor rd0 = plan.getJobSpecification()
                    .getOperatorInputRecordDescriptor(getOperatorId(), 0);
            final FrameTupleAccessor accessor = new FrameTupleAccessor(ctx, rd0);
            return new IOperatorNodePushable() {
                private GroupingHashTable table;

                @Override
                public void open() throws HyracksDataException {
                    table = new GroupingHashTable(ctx, keys, comparatorFactories, tpcf, aggregatorFactory, rd0,
                            recordDescriptors[0], tableSize);
                }

                @Override
                public void nextFrame(ByteBuffer buffer) throws HyracksDataException {
                    accessor.reset(buffer);
                    int tupleCount = accessor.getTupleCount();
                    for (int i = 0; i < tupleCount; ++i) {
                        table.insert(accessor, i);
                    }
                }

                @Override
                public void close() throws HyracksDataException {
                    env.set(HASHTABLE, table);
                }

                @Override
                public void setFrameWriter(int index, IFrameWriter writer) {
                    throw new IllegalArgumentException();
                }
            };
        }

        @Override
        public IOperatorDescriptor getOwner() {
            return HashGroupOperatorDescriptor.this;
        }

        @Override
        public boolean supportsPullInterface() {
            return false;
        }

        @Override
        public boolean supportsPushInterface() {
            return true;
        }
    }

    private class OutputActivity extends AbstractActivityNode {
        private static final long serialVersionUID = 1L;

        @Override
        public IOperatorNodePullable createPullRuntime(HyracksContext ctx, JobPlan plan, IOperatorEnvironment env,
                int partition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IOperatorNodePushable createPushRuntime(HyracksContext ctx, JobPlan plan, final IOperatorEnvironment env,
                int partition) {
            return new IOperatorNodePushable() {
                private IFrameWriter writer;

                @Override
                public void open() throws HyracksDataException {
                    GroupingHashTable table = (GroupingHashTable) env.get(HASHTABLE);
                    writer.open();
                    table.write(writer);
                    writer.close();
                    env.set(HASHTABLE, null);
                }

                @Override
                public void nextFrame(ByteBuffer buffer) throws HyracksDataException {
                    throw new IllegalStateException();
                }

                @Override
                public void close() throws HyracksDataException {
                    // do nothing
                }

                @Override
                public void setFrameWriter(int index, IFrameWriter writer) {
                    if (index != 0) {
                        throw new IllegalArgumentException();
                    }
                    this.writer = writer;
                }
            };
        }

        @Override
        public IOperatorDescriptor getOwner() {
            return HashGroupOperatorDescriptor.this;
        }

        @Override
        public boolean supportsPullInterface() {
            return false;
        }

        @Override
        public boolean supportsPushInterface() {
            return true;
        }
    }
}