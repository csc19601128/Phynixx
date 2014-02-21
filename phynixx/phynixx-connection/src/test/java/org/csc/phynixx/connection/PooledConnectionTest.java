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
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.phynixx.test_connection.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;


public class PooledConnectionTest {


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

        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setMaxTotal(POOL_SIZE);
        this.factory = new PooledPhynixxManagedConnectionFactory(new TestConnectionFactory(), cfg);

        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        LoggerPerTransactionStrategy strategy = new LoggerPerTransactionStrategy(loggerFactory);

        this.factory.setLoggerSystemStrategy(strategy);
        this.factory.addConnectionProxyDecorator(new TestConnectionStatusListener());

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
    public void testGoodCaseRollback() throws Exception {

        ITestConnection con = PooledConnectionTest.this.factory.getConnection();

        con.act(5);
        con.act(7);
        int counter = con.getCounter();
        Assert.assertEquals(12, counter);

        con.rollback();

        con.close();

    }

    @Test
    public void testInterruptedCommit() throws Exception {

        ITestConnection con = PooledConnectionTest.this.factory.getConnection();

        int counterInitial = con.getCounter();
        con.setInitialCounter(3);
        con.act(2);
        con.act(7);
        con.setInterruptFlag(TestInterruptionPoint.COMMIT);
        try {
            con.commit();
            throw new AssertionFailedError("ActionInterruptedException expected");
        } catch (Exception e) {;}


        LOG.info(TestConnectionStatusManager.toDebugString());
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(con.getConnectionId());
        Assert.assertTrue(!statusStack.isCommitted());

        final ITestConnection[] recoveredConnection = new ITestConnection[1];
        this.factory.recover(new IPhynixxManagedConnectionFactory.IRecoveredManagedConnection<ITestConnection>() {

            @Override
            public void managedConnectionRecovered(ITestConnection con) {
                recoveredConnection[0] = con;
            }
        });
        Assert.assertEquals(12, recoveredConnection[0].getCounter());


    }


    @Test
    public void testInterruptedRollback() throws Exception {

        ITestConnection con = PooledConnectionTest.this.factory.getConnection();

        int counterInitial = con.getCounter();
        con.setInitialCounter(3);
        con.act(5);
        con.act(4);
        con.setInterruptFlag(TestInterruptionPoint.ROLLBACK);
        try {
            con.rollback();

        } catch (Exception e) {
            System.out.println(e.getClass());
        }


        final ITestConnection[] recoveredConnection = new ITestConnection[1];
        this.factory.recover(new IPhynixxManagedConnectionFactory.IRecoveredManagedConnection<ITestConnection>() {

            @Override
            public void managedConnectionRecovered(ITestConnection con) {
                recoveredConnection[0] = con;
            }
        });
        Assert.assertEquals(3, recoveredConnection[0].getCounter());


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
