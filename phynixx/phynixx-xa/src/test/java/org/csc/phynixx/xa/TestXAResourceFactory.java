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


import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csc.phynixx.connection.PhynixxManagedConnectionFactory;
import org.csc.phynixx.connection.PooledPhynixxManagedConnectionFactory;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.phynixx.test_connection.ITestConnection;
import org.csc.phynixx.phynixx.test_connection.TestConnectionFactory;

import javax.transaction.TransactionManager;
import java.io.File;


public class TestXAResourceFactory extends PhynixxXAResourceFactory<ITestConnection> {

    public static final int POOL_SIZE = 10;

    public TestXAResourceFactory(TransactionManager transactionManager) {
        this("TestXAResourceFactory", null, transactionManager);
    }

    public TestXAResourceFactory(String id,
                                 File dataLoggerDirectory,
                                 TransactionManager transactionManager) {
        super(id, createManagedConnectionFactory(dataLoggerDirectory), transactionManager);
    }

    private static PhynixxManagedConnectionFactory<ITestConnection> createManagedConnectionFactory(File dataLoggerDirectory) {
        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setMaxTotal(POOL_SIZE);
        PooledPhynixxManagedConnectionFactory<ITestConnection> factory =
                new PooledPhynixxManagedConnectionFactory(new TestConnectionFactory(), cfg);

        if (dataLoggerDirectory != null) {
            IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("testResource", dataLoggerDirectory);
            LoggerPerTransactionStrategy strategy = new LoggerPerTransactionStrategy(loggerFactory);
            factory.setLoggerSystemStrategy(strategy);
        }

        return factory;
    }

}
