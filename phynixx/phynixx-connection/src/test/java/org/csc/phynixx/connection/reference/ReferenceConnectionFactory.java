package org.csc.phynixx.connection.reference;

/*
 * #%L
 * phynixx-connection
 * %%
 * Copyright (C) 2014 csc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.csc.phynixx.common.generator.IDGenerator;
import org.csc.phynixx.common.generator.IDGenerators;
import org.csc.phynixx.connection.IPhynixxConnectionFactory;


public class ReferenceConnectionFactory implements IPhynixxConnectionFactory<IReferenceConnection> {

    private IDGenerator<Long> idGenerator = IDGenerators.synchronizeGenerator(IDGenerators.createLongGenerator(1l));

    public IReferenceConnection getConnection() {
        Object connectionId = null;
            connectionId = idGenerator.generate();
        return new ReferenceConnection(connectionId);
    }

    public Class getConnectionInterface() {
        return IReferenceConnection.class;
    }


    public void close() {

    }


}
