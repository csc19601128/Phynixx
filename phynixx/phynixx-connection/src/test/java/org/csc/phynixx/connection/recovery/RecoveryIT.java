package org.csc.phynixx.connection.recovery;

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
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IPhynixxRecovery;
import org.csc.phynixx.connection.PhynixxRecovery;
import org.csc.phynixx.connection.PooledPhynixxManagedConnectionFactory;
import org.csc.phynixx.connection.loggersystem.IPhynixxLoggerSystemStrategy;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.phynixx.testconnection.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;


public class RecoveryIT {


    {
        System.setProperty("log4j_level", "INFO");

    }

    private IPhynixxLogger LOG = PhynixxLogManager.getLogger(this.getClass());

    private PooledPhynixxManagedConnectionFactory<ITestConnection> factory = null;

    private static final int POOL_SIZE = 30;

    private TmpDirectory tmpDir = null;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        this.tmpDir = new TmpDirectory("test");

        this.factory=createManagedConnectionFactory();

    }

    private PooledPhynixxManagedConnectionFactory<ITestConnection> createManagedConnectionFactory() {

        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setMaxTotal(POOL_SIZE);
        PooledPhynixxManagedConnectionFactory<ITestConnection> fac = new PooledPhynixxManagedConnectionFactory(new TestConnectionFactory(), cfg);

        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        LoggerPerTransactionStrategy strategy = new LoggerPerTransactionStrategy(loggerFactory);

        fac.setLoggerSystemStrategy(strategy);
        fac.addConnectionProxyDecorator(new TestConnectionStatusListener());

        return fac;
    }

    @After
    public void tearDown() throws Exception {
        TestConnectionStatusManager.clear();

        this.factory.close();
        this.factory = null;

        // delete all tmp files ...
        this.tmpDir.clear();

    }
    @Test
    public void testGoodCaseCommit() throws Exception {

        ITestConnection con = RecoveryIT.this.factory.getConnection();

        con.act(5);
        con.act(7);
        int counter = con.getCounter();
        Assert.assertEquals(12, counter);

        con.commit();

        con.close();

        // Close the factory an close the pooled connections
        this.factory.close();

        final ITestConnection[] recoveredConnection = new ITestConnection[1];

        PhynixxRecovery<ITestConnection> recovery= new PhynixxRecovery<ITestConnection>(new TestConnectionFactory());
        IPhynixxLoggerSystemStrategy<ITestConnection> loggerStrategy=  this.factory.getLoggerSystemStrategy();
        loggerStrategy.close();
        recovery.setLoggerSystemStrategy(loggerStrategy);
        recovery.addConnectionProxyDecorator(new TestConnectionStatusListener());

        recovery.recover(new IPhynixxRecovery.IRecoveredManagedConnection<ITestConnection>() {

            @Override
            public void managedConnectionRecovered(ITestConnection con) {
                recoveredConnection[0] = con;
            }
        });
        LOG.info(TestConnectionStatusManager.toDebugString());


        // in XADataLogger.openForWrite() wird die DatRecorderID als erster Satz geschrieben. Dieser zaehlt mit
        // da Protokll neben commitForward/rollbackData ist genau zu spezifizieren
        Assert.assertTrue(recoveredConnection[0] == null);


    }

    @Test
    public void testGoodCaseRollback() throws Exception {

        ITestConnection con = RecoveryIT.this.factory.getConnection();

        con.act(5);
        con.act(7);
        int counter = con.getCounter();
        Assert.assertEquals(12, counter);

        con.rollback();

        con.close();

        final ITestConnection[] recoveredConnection = new ITestConnection[1];



        PhynixxRecovery<ITestConnection> recovery= new PhynixxRecovery<ITestConnection>(new TestConnectionFactory());
        IPhynixxLoggerSystemStrategy<ITestConnection> loggerStrategy=  this.factory.getLoggerSystemStrategy();
        loggerStrategy.close();
        recovery.setLoggerSystemStrategy(loggerStrategy);

        recovery.recover(new IPhynixxRecovery.IRecoveredManagedConnection<ITestConnection>() {

            @Override
            public void managedConnectionRecovered(ITestConnection con) {
                recoveredConnection[0] = con;
            }
        });
        LOG.info(TestConnectionStatusManager.toDebugString());
        Assert.assertTrue(recoveredConnection[0]==null);
    }

    @Test
    public void testInterruptedCommit() throws Exception {

        ITestConnection con = RecoveryIT.this.factory.getConnection();

        con.setInitialCounter(3);
        con.act(2);
        con.act(7);
        con.setInterruptFlag(TestInterruptionPoint.COMMIT);
        try {
            con.commit();
            throw new AssertionFailedError("ActionInterruptedException expected");
        } catch (Exception e) {;}

        // close on an incomplete connections does not destroy the restore data
        con.close();


        LOG.info(TestConnectionStatusManager.toDebugString());
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(con.getConnectionId());
        Assert.assertTrue(!statusStack.isCommitted());

        final ITestConnection[] recoveredConnection = new ITestConnection[1];

        PhynixxRecovery<ITestConnection> recovery= new PhynixxRecovery<ITestConnection>(new TestConnectionFactory());
        IPhynixxLoggerSystemStrategy<ITestConnection> loggerStrategy=  this.factory.getLoggerSystemStrategy();
        loggerStrategy.close();
        recovery.addConnectionProxyDecorator(new TestConnectionStatusListener());
        recovery.setLoggerSystemStrategy(loggerStrategy);

        recovery.recover(new IPhynixxRecovery.IRecoveredManagedConnection<ITestConnection>() {

            @Override
            public void managedConnectionRecovered(ITestConnection con) {
                recoveredConnection[0] = con;
                Assert.assertEquals(12, con.getCounter());
            }
        });

        LOG.info(TestConnectionStatusManager.toDebugString());

        TestStatusStack recoveredStatusStack = TestConnectionStatusManager.getStatusStack(recoveredConnection[0].getConnectionId());
        Assert.assertTrue(recoveredStatusStack.isRecoverd());
        Assert.assertTrue(recoveredStatusStack.isFreed());

    }


    @Test
    public void testInterruptedRollback() throws Exception {

        ITestConnection con = RecoveryIT.this.factory.getConnection();

        con.setInitialCounter(3);
        con.act(5);
        con.act(4);
        con.setInterruptFlag(TestInterruptionPoint.ROLLBACK);
        try {
            con.rollback();
            throw new AssertionFailedError("ActionInterruptedException expected");

        } catch (Exception e) {

        }

        // close on an incomplete connections does not destroy the restore data
        con.close();

        final ITestConnection[] recoveredConnection = new ITestConnection[1];

        PhynixxRecovery<ITestConnection> recovery= new PhynixxRecovery<ITestConnection>(new TestConnectionFactory());
        IPhynixxLoggerSystemStrategy<ITestConnection> loggerStrategy=  this.factory.getLoggerSystemStrategy();
        loggerStrategy.close();
        recovery.addConnectionProxyDecorator(new TestConnectionStatusListener());
        recovery.setLoggerSystemStrategy(loggerStrategy);

        recovery.recover(new IPhynixxRecovery.IRecoveredManagedConnection<ITestConnection>() {

            @Override
            public void managedConnectionRecovered(ITestConnection con) {
                recoveredConnection[0] = con;
                Assert.assertEquals(3, con.getCounter());
            }
        });

        LOG.info(TestConnectionStatusManager.toDebugString());

        TestStatusStack recoveredStatusStack = TestConnectionStatusManager.getStatusStack(recoveredConnection[0].getConnectionId());
        Assert.assertTrue(recoveredStatusStack.isRecoverd());
        Assert.assertTrue(recoveredStatusStack.isFreed());


    }


    private Properties loadHowlConfig() throws Exception {
        Properties howlprop = new Properties();
        howlprop.put("listConfig", "false");
        howlprop.put("bufferSize", "32");
        howlprop.put("minBuffers", "1");
        howlprop.put("maxBuffers", "1");
        howlprop.put("maxBlocksPerFile", "100");
        howlprop.put("logFileDir", this.tmpDir.getDirectory().getAbsolutePath());
        howlprop.put("logFileName", "test1");
        howlprop.put("maxLogFiles", "2");

        return howlprop;
    }

}
