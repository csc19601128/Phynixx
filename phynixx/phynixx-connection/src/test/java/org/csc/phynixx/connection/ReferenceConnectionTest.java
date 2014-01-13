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
import org.csc.phynixx.connection.loggersystem.Dev0Strategy;
import org.csc.phynixx.connection.loggersystem.ILoggerSystemStrategy;
import org.csc.phynixx.connection.loggersystem.PerTransactionStrategy;
import org.csc.phynixx.connection.reference.IReferenceConnection;
import org.csc.phynixx.connection.reference.ReferenceConnection;
import org.csc.phynixx.connection.reference.ReferenceConnectionFactory;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.test_connection.TestConnectionStatusManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReferenceConnectionTest {

    public static final String LOGGER = "logger";
    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private ReferenceConnectionFactory factory = null;

    private IPhynixxConnectionProxyFactory proxyFactory = null;

    private ILoggerSystemStrategy strategy = null;

    private TmpDirectory tmpDir = null;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDir = new TmpDirectory(LOGGER);

        this.factory = new ReferenceConnectionFactory();
        this.proxyFactory = new DynaProxyFactory(new Class[]{IReferenceConnection.class});

        this.strategy = new Dev0Strategy();
    }

    @After
    public void tearDown() throws Exception {
        TestConnectionStatusManager.clear();

        // delete all tmp files ...
        this.tmpDir.clear();
        this.factory = null;
        this.strategy = new Dev0Strategy();
    }


    @Test
    public void testCommit() throws Exception {


        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        this.strategy = new PerTransactionStrategy(loggerFactory);

        IReferenceConnection con = (IReferenceConnection) ReferenceConnectionTest.this.factory.getConnection();
        IPhynixxConnectionProxy proxy = ReferenceConnectionTest.this.proxyFactory.getConnectionProxy();
        proxy.addConnectionListener(ReferenceConnectionTest.this.strategy);
        proxy.setConnection(con);

        con.setInitialCounter(13);

        con.incCounter(5);
        con.incCounter(7);

        con.commit();

        con.close();

        con.recover();


        Assert.assertEquals(37, con.getCounter());


    }

    @Test
    public void testRollback() throws Exception {


        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        this.strategy = new PerTransactionStrategy(loggerFactory);

        IReferenceConnection con = (IReferenceConnection) ReferenceConnectionTest.this.factory.getConnection();
        IPhynixxConnectionProxy proxy = ReferenceConnectionTest.this.proxyFactory.getConnectionProxy();
        proxy.addConnectionListener(ReferenceConnectionTest.this.strategy);
        proxy.setConnection(con);

        con.setInitialCounter(13);

        con.incCounter(5);
        con.incCounter(7);

        con.rollback();

        con.close();

        con.recover();
        Assert.assertEquals(13, con.getCounter());


    }

    @Test
    public void testCommitFailure() throws Exception {


        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        this.strategy = new PerTransactionStrategy(loggerFactory);

        IReferenceConnection con = (IReferenceConnection) ReferenceConnectionTest.this.factory.getConnection();
        IPhynixxConnectionProxy proxy = ReferenceConnectionTest.this.proxyFactory.getConnectionProxy();
        proxy.addConnectionListener(ReferenceConnectionTest.this.strategy);
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

        Assert.assertEquals(20 + ReferenceConnection.ERRONEOUS_INC, con.getCounter());

    }


}
