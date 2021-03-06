/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.asterix.external.api;

import org.apache.asterix.common.exceptions.ErrorCode;
import org.apache.asterix.common.exceptions.RuntimeDataException;
import org.apache.hyracks.api.comm.IFrameWriter;
import org.apache.hyracks.api.exceptions.HyracksDataException;

@FunctionalInterface
public interface IDataFlowController {

    public void start(IFrameWriter writer) throws HyracksDataException, InterruptedException;

    public default boolean pause() throws HyracksDataException {
        throw new RuntimeDataException(ErrorCode.OPERATION_NOT_SUPPORTED);
    }

    public default boolean resume() throws HyracksDataException {
        throw new RuntimeDataException(ErrorCode.OPERATION_NOT_SUPPORTED);
    }

    public default void flush() throws HyracksDataException {
        throw new RuntimeDataException(ErrorCode.OPERATION_NOT_SUPPORTED);
    }

    public default boolean stop(long timeout) throws HyracksDataException {
        throw new RuntimeDataException(ErrorCode.OPERATION_NOT_SUPPORTED);
    }

    /**
     * @return The number of processed tuples by this controller
     */
    default long getProcessedTuples() {
        throw new UnsupportedOperationException();
    }
}
