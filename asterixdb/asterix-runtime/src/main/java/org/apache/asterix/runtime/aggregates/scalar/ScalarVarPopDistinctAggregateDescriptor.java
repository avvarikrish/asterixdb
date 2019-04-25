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

package org.apache.asterix.runtime.aggregates.scalar;

import org.apache.asterix.om.functions.BuiltinFunctions;
import org.apache.asterix.om.functions.IFunctionDescriptorFactory;
import org.apache.asterix.runtime.aggregates.std.VarPopAggregateDescriptor;
import org.apache.asterix.runtime.functions.FunctionTypeInferers;
import org.apache.asterix.runtime.utils.DescriptorFactoryUtil;
import org.apache.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;

public class ScalarVarPopDistinctAggregateDescriptor extends AbstractScalarDistinctAggregateDescriptor {

    private static final long serialVersionUID = 1L;

    public static final FunctionIdentifier FID = BuiltinFunctions.SCALAR_VAR_POP_DISTINCT;

    public static final IFunctionDescriptorFactory FACTORY = DescriptorFactoryUtil
            .createFactory(ScalarVarPopDistinctAggregateDescriptor::new, FunctionTypeInferers.SET_ARGUMENT_TYPE);

    private ScalarVarPopDistinctAggregateDescriptor() {
        super(VarPopAggregateDescriptor.FACTORY);
    }

    @Override
    public FunctionIdentifier getIdentifier() {
        return FID;
    }
}