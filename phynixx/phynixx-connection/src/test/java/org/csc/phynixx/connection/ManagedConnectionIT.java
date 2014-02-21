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
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.loggersystem.IPhynixxLoggerSystemStrategy;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;
import org.csc.phynixx.phynixx.test_connection.ITestConnection;
import org.csc.phynixx.phynixx.test_connection.TestConnectionFactory;
import org.csc.phynixx.phynixx.test_connection.TestConnectionStatusManager;
import org.csc.phynixx.phynixx.test_connection.TestInterruptionPoint;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ManagedConnectionIT {

    public static final String LOGGER = "logger";
    private IPhynixxLogger LOG = PhynixxLogManager.getLogger(this.getClass());

    private PhynixxManagedConnectionFactory<ITestConnection> connectionFactory = null;

    private IPhynixxLoggerSystemStrategy strategy = null;

    private TmpDirectory tmpDir = null;

    @Before
    public void setUp() throws Exception {
        // configuring the LOG-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDir = new TmpDirectory(LOGGER);


        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        IPhynixxLoggerSystemStrategy strategy = new LoggerPerTransactionStrategy(loggerFactory);

        this.connectionFactory =
                new PhynixxManagedConnectionFactory<ITestConnection>(new TestConnectionFactory());
        connectionFactory.setLoggerSystemStrategy(strategy);
    }

    @After
    public void tearDown() throws Exception {
        TestConnectionStatusManager.clear();

        if (connectionFactory != null) {
            this.connectionFactory.close();
        }

        // delete all tmp files ...
        this.tmpDir.clear();
    }
    @Test
    public void testCommit1() throws Exception {


        ITestConnection con = this.connectionFactory.getConnection();

        con.setInitialCounter(13);

        con.act(5);
        con.act(7);

        IXADataRecorder xaDataRecorder = con.getXADataRecorder();

        con.commit();
        Assert.assertEquals(25, con.getCounter());

        con.close();

        Assert.assertEquals(0, con.getCounter());


    }

    @Test
    public void testCommit() throws Exception {


        ITestConnection con = connectionFactory.getConnection();

        con.setInitialCounter(13);

        con.act(5);
        con.act(7);

        con.commit();

        Assert.assertEquals(25, con.getCounter());

        con.close();




    }

    @Test
    public void testRollback() throws Exception {


        ITestConnection con = connectionFactory.getConnection();

        con.setInitialCounter(13);

        con.act(5);
        con.act(7);

        con.rollback();

        Assert.assertEquals(13, con.getCounter());

        con.close();

    }

    @Test
    public void testCommitFailure() throws Exception {


        ITestConnection con = connectionFactory.getConnection();

        con.setInitialCounter(13);
        con.setInterruptFlag(TestInterruptionPoint.ACT);
        try {
            con.act(7);
            con.commit();
            throw new AssertionFailedError("Invalid value to commit");

        } catch (Exception e) {
        }


        // act(7) doesn't change the state of con
        Assert.assertEquals(13, con.getCounter());

        con.close();
    }

    @Test
    public void testAutoCommit() throws Exception {


        ITestConnection con = connectionFactory.getConnection();
        con.setAutoCommit(true);

        con.setInitialCounter(13);
        con.act(7);
        try {
            con.rollback();
        } catch (Exception e) {
        }
        // autocommit lets act() committing the change
        Assert.assertEquals(20, con.getCounter());


        con.close();



    }


    @Test
    public void testAutoCommit2() throws Exception {


        ITestConnection con = connectionFactory.getConnection();
        con.setAutoCommit(false);

        con.setInitialCounter(13);

        con.act(7);

        try {
            con.rollback();
        } catch (Exception e) {
        }
        Assert.assertEquals(13, con.getCounter());

        con.close();
    }

}
