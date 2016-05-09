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


import java.io.File;
import java.util.Set;

import javax.transaction.TransactionManager;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csc.phynixx.connection.IPhynixxManagedConnectionFactory;
import org.csc.phynixx.connection.PooledPhynixxManagedConnectionFactory;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.phynixx.testconnection.ITestConnection;
import org.csc.phynixx.phynixx.testconnection.TestConnectionFactory;
import org.csc.phynixx.phynixx.testconnection.TestConnectionStatusListener;


public class TestXAResourceFactory extends PhynixxXAResourceFactory<ITestConnection> {

    public static final int  POOL_SIZE= 10;

    public TestXAResourceFactory(TransactionManager transactionManager) {
        this("TestXAResourceFactory", null, transactionManager);
    }
    
    public TestXAResourceFactory(
            int poolSize,
            String id, 
            File dataLoggerDirectory,
            TransactionManager transactionManager) {
        super(id, createManagedConnectionFactory(poolSize,id,dataLoggerDirectory), transactionManager);

        
    }

    public TestXAResourceFactory(String id,
                                 File dataLoggerDirectory,
                                 TransactionManager transactionManager) {
        this(POOL_SIZE,id,dataLoggerDirectory, transactionManager);
    }

    private static IPhynixxManagedConnectionFactory<ITestConnection> createManagedConnectionFactory(int poolSize,String id, File dataLoggerDirectory) {
        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setMaxTotal(poolSize);
        PooledPhynixxManagedConnectionFactory<ITestConnection> factory =
                new PooledPhynixxManagedConnectionFactory<ITestConnection>(new TestConnectionFactory(id+"-"), cfg);

        if (dataLoggerDirectory != null) {
            IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory(id, dataLoggerDirectory);
            LoggerPerTransactionStrategy<ITestConnection> strategy = new LoggerPerTransactionStrategy<ITestConnection>(loggerFactory);
            factory.setLoggerSystemStrategy(strategy);
        }

        factory.addManagedConnectionDecorator(new TestConnectionStatusListener());

        return factory;
    }

    @Override
    public Set<PhynixxXAResource<ITestConnection>> getUnreleasedXAResources() {
        return super.getUnreleasedXAResources();
    }
    
    
    

}
