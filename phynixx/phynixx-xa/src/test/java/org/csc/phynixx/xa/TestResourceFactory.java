package org.csc.phynixx.xa;

/*
 * #%L
 * phynixx-xa
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


import org.csc.phynixx.connection.DynaProxyFactory;
import org.csc.phynixx.test_connection.ITestConnection;
import org.csc.phynixx.test_connection.TestConnection;
import org.csc.phynixx.test_connection.TestConnectionFactory;

import javax.transaction.TransactionManager;


public class TestResourceFactory extends PhynixxResourceFactory {

    public TestResourceFactory(
            TransactionManager transactionManager) {
        this("TestResourceFactory", transactionManager);
    }

    public TestResourceFactory(String id,
                               TransactionManager transactionManager) {
        super(id,
                new XAPooledConnectionFactory(new TestConnectionFactory()),
                new DynaProxyFactory(new Class[]{ITestConnection.class}),
                transactionManager);
    }

    public boolean isReleased(TestConnection connection) {
        return this.isFreeConnection(connection);
    }


}
