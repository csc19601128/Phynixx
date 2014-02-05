package org.csc.phynixx.connection;

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


import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.connection.reference.IReferenceConnection;
import org.csc.phynixx.connection.reference.ReferenceConnectionFactory;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;
import org.csc.phynixx.test_connection.TestConnectionStatusManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReferenceConnectionProxyTest {

    public static final String LOGGER = "logger";
    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private PhynixxManagedConnectionFactory factory = null;

    private TmpDirectory tmpDirectory = null;

    private PhynixxManagedConnectionFactory createConnectionFactory() throws Exception {

        PhynixxManagedConnectionFactory factory = new PhynixxManagedConnectionFactory(new ReferenceConnectionFactory());
        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("reference", this.tmpDirectory.getDirectory());
        factory.setLoggerSystemStrategy(new LoggerPerTransactionStrategy(loggerFactory));


        return factory;
    }

    @Before
    public void setUp() throws Exception {

        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDirectory = new TmpDirectory(LOGGER);

        this.factory = createConnectionFactory();
    }

    @After
    public void tearDown() throws Exception {
        TestConnectionStatusManager.clear();

        // delete all tmp files ...
        this.tmpDirectory.clear();
        this.factory.close();

        this.factory = null;
    }


    @Test
    public void testCommit() throws Exception {


        IReferenceConnection con = (IReferenceConnection) ReferenceConnectionProxyTest.this.factory.getConnection();

        con.setInitialCounter(13);

        con.incCounter(5);
        con.incCounter(7);

        IXADataRecorder xaDataRecorder = con.getXADataRecorder();

        con.commit();

        con.close();

        xaDataRecorder.recover();
        con.setXADataRecorder(xaDataRecorder);


        Assert.assertEquals(37, con.getCounter());


    }
/**
 @Test public void testRollback() throws Exception {


 IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDirectory.getDirectory());
 this.strategy = new LoggerPerTransactionStrategy(loggerFactory);

 IReferenceConnection con = (IReferenceConnection) ReferenceConnectionProxyTest.this.factory.getCoreConnection();
 IPhynixxManagedConnection proxy = ReferenceConnectionProxyTest.this.proxyFactory.getManagedConnection();
 proxy.addConnectionListener(ReferenceConnectionProxyTest.this.strategy);
 proxy.setConnection(con);

 con.setInitialCounter(13);

 con.incCounter(5);
 con.incCounter(7);

 con.rollback();

 con.close();

 con.recover();
 Assert.assertEquals(13, con.getCounter());


 }

 @Test public void testCommitFailure() throws Exception {


 IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDirectory.getDirectory());
 this.strategy = new LoggerPerTransactionStrategy(loggerFactory);

 IReferenceConnection con = (IReferenceConnection) ReferenceConnectionProxyTest.this.factory.getCoreConnection();
 IPhynixxManagedConnection proxy = ReferenceConnectionProxyTest.this.proxyFactory.getManagedConnection();
 proxy.addConnectionListener(ReferenceConnectionProxyTest.this.strategy);
 proxy.setConnection(con);



 con.setInitialCounter(13);

 con.incCounter(ReferenceConnection.ERRONEOUS_INC);
 con.incCounter(7);
 try {
 con.commit();
 throw new AssertionFailedError("Invalid value to commit");

 } catch (Exception e) {
 }
 con.close();

 con.recover();

 Assert.assertEquals(20+ReferenceConnection.ERRONEOUS_INC, con.getCounter());

 }

 **/
}
