package org.csc.phynixx.phynixx.testconnection;

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


public class TestConnectionFactory implements IPhynixxConnectionFactory<ITestConnection> {

    private static final IDGenerator<Long> idGenerator = IDGenerators.createLongGenerator(1, true);
    
    private String decorator; 
    
    

    public TestConnectionFactory() {
        this.decorator="test_";
    }

    public TestConnectionFactory(String decorator) {
        super();
        this.decorator = decorator;
    }

    @Override
    public ITestConnection getConnection() {
        Long connectionId = null;
        connectionId = idGenerator.generate();
        return new TestConnection(decorator+Long.toString(connectionId));
    }

    @Override
    public Class<ITestConnection> getConnectionInterface() {
        return ITestConnection.class;
    }


    @Override
    public void close() {

    }

}
