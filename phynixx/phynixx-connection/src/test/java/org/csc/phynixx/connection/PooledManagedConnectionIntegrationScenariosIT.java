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


import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.common.logger.PrintLogManager;
import org.csc.phynixx.common.logger.PrintLogger;
import org.csc.phynixx.connection.loggersystem.IPhynixxLoggerSystemStrategy;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.phynixx.testconnection.*;
import org.junit.Assert;

import java.io.File;
import java.util.Properties;
import java.util.Set;

public class PooledManagedConnectionIntegrationScenariosIT extends TestCase {

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(PooledManagedConnectionIntegrationScenariosIT.class);
    TmpDirectory tmpDirectory = null;
    private PooledPhynixxManagedConnectionFactory<ITestConnection> factory;

    protected void setUp() throws Exception {
        this.tmpDirectory = new TmpDirectory();
        PhynixxLogManager.setLogManager(new PrintLogManager(PrintLogger.ERROR));


        // instanciate a connection pool
        this.factory = this.createConnectionFactory(10);
        TestConnectionStatusListener eventListener = new TestConnectionStatusListener();
        factory.addConnectionProxyDecorator(eventListener);
    }

    protected void tearDown() throws Exception {
        // instanciate a connection pool
        this.factory.close();


        this.tmpDirectory.clear();

    }

    public void testSampleConnectionFactory() throws Exception {


        ITestConnection con = null;
        try {
            // get a connection ....
            con = factory.getManagedConnection().toConnection();

            con.setInitialCounter(43);

            con.act(6);
            con.act(-3);

            // rollback the connection ....
            con.rollback();
            Assert.assertEquals(43, con.getCounter());

        } finally {
            // close the connection to the pool ....
            if (con != null) {
                con.close();
            }
        }
    }


    public void testSampleConnectionPool() throws Exception {
        // instanciate a connection pool


        ITestConnection con = null;
        try {
            // get a connection ....
            con = (ITestConnection) factory.getConnection();

            con.setInitialCounter(43);

            con.act(6);
            con.act(-3);

            // increments are performed during commit
            Assert.assertEquals(46, con.getCounter());

            // rollback the connection ....
            con.rollback();

            Assert.assertEquals(43, con.getCounter());


        } finally {
            // close the connection to the pool ....
            if (con != null) {
                con.close();
            }
        }
    }

    public void testDecorationConnections() throws Exception {

        ITestConnection con = null;
        try {
            // get a connection ....
            con = factory.getConnection();

            con.setInitialCounter(43);

            con.act(6);
            con.act(-3);

            // increments are performed during commit
            Assert.assertEquals(46, con.getCounter());

            // rollback the connection ....
            con.rollback();
            Assert.assertEquals(43, con.getCounter());


            LOG.info(TestConnectionStatusManager.toDebugString());
            TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(con.getConnectionId());
            Assert.assertTrue(statusStack.isRolledback());
            Assert.assertTrue(!statusStack.isCommitted());
            Assert.assertTrue(!statusStack.isReleased());


        } finally {
            // close the connection to the pool ....
            if (con != null) {
                con.close();
            }
        }
    }

    /**
     * if the connection has no transactiona data no rollback/commit is performed
     * a connection is said tro have transactional dat if at least one method with {@link @RequiresTransaction}
     * is called an no rollback/commit closes the transactional data
     *
     * @throws Exception
     */
    public void testHasTransactionalData1() throws Exception {

        ITestConnection con = null;
        // get a connection ....
        con = factory.getConnection();
        // rollback the connection ....
        con.rollback();
        LOG.info(TestConnectionStatusManager.toDebugString());
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(con.getConnectionId());
        Assert.assertTrue(!statusStack.isRolledback());
        Assert.assertTrue(!statusStack.isCommitted());
        Assert.assertTrue(!statusStack.isReleased());
    }

    /**
     * if the connection has no transactiona data no rollback/commit is performed
     * a connection is said tro have transactional dat if at least one method with {@link @RequiresTransaction}
     * is called an no rollback/commit closes the transactional data
     *
     * @throws Exception
     */
    public void testHasTransactionalData2() throws Exception {

        ITestConnection con = null;

        // get a connection ....
        con = factory.getConnection();
        con.act(1);
        // rollback the connection ....
        con.rollback();
        con.commit();

        LOG.info(TestConnectionStatusManager.toDebugString());
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(con.getConnectionId());
        Assert.assertTrue(statusStack.isRolledback());
        Assert.assertTrue(!statusStack.isCommitted());
        Assert.assertTrue(!statusStack.isReleased());
    }

    public void testRecovery() throws Exception {

        ITestConnection con =  factory.getConnection();

        con.setInitialCounter(43);

        // expection when act is called the 3rd time
        con.setInterruptFlag(TestInterruptionPoint.ACT, 3);
        con.act(6);
        con.act(-3);

        try {
            con.act(7);
            throw new AssertionFailedError("ActionInterrupted expected");
        } catch (Exception e) {
        }

        // increments are performed during commit -- act(7) is not registered in con
        Assert.assertEquals(46, con.getCounter());

        // connection is not closed ...

        // close the factory and leave the connection in a recoverable state
        // rollback the connection ....
        factory.close();

        // instanciate a new connection pool

        TestConnectionStatusManager.clear();
        PhynixxRecovery<ITestConnection> recovery = new PhynixxRecovery<ITestConnection>(new TestConnectionFactory());
        IPhynixxLoggerSystemStrategy<ITestConnection> loggerStrategy = factory.getLoggerSystemStrategy();
        loggerStrategy.close();
        recovery.setLoggerSystemStrategy(loggerStrategy);
        recovery.addConnectionProxyDecorator(new TestConnectionStatusListener());

        recovery.recover(null);

        LOG.info(TestConnectionStatusManager.toDebugString());
        Set<TestStatusStack> statusStacks = TestConnectionStatusManager.getStatusStacks();
        Assert.assertEquals(1, statusStacks.size());

        TestStatusStack statusStack = statusStacks.iterator().next();

        Assert.assertTrue(statusStack.isRecoverd());
        Assert.assertTrue(!statusStack.isCommitted());

        // recovering closes the recovered connection
        Assert.assertTrue(statusStack.isFreed());

    }

    private PooledPhynixxManagedConnectionFactory<ITestConnection> createConnectionFactory(int poolSize) throws Exception {

        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setMaxTotal(poolSize);
        PooledPhynixxManagedConnectionFactory<ITestConnection> factory = new PooledPhynixxManagedConnectionFactory(new TestConnectionFactory(), cfg);

        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("testConnection", this.tmpDirectory.getDirectory());
        //IDataLoggerFactory loggerFactory= new HowlLoggerFactory("reference", this.loadHowlConfig(tmpDirectory.getDirectory()));

        factory.setLoggerSystemStrategy(new LoggerPerTransactionStrategy(loggerFactory));


        TestConnectionStatusListener statusListener = new TestConnectionStatusListener();
        factory.addConnectionProxyDecorator(statusListener);
        return factory;
    }


    /**
     * this setting for the logger system
     *
     * @return
     * @throws Exception
     */
    private Properties loadHowlConfig(File loggingDirectory) throws Exception {
        Properties howlprop = new Properties();
        howlprop.put("listConfig", "false");
        howlprop.put("bufferSize", "32");
        howlprop.put("minBuffers", "1");
        howlprop.put("maxBuffers", "1");
        howlprop.put("maxBlocksPerFile", "100");
        howlprop.put("logFileDir", loggingDirectory.getAbsolutePath());
        howlprop.put("logFileName", "test1");
        howlprop.put("maxLogFiles", "2");

        return howlprop;
    }


}
