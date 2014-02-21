package org.csc.phynixx.phynixx.recovery;

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


import junit.framework.Assert;
import junit.framework.TestCase;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IPhynixxConnectionHandle;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.connection.PhynixxManagedConnectionFactory;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.phynixx.test_connection.*;
import org.junit.Before;

import java.util.Properties;


public class ConnectionRecoveryIT extends TestCase {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private PhynixxManagedConnectionFactory<ITestConnection> factory = null;

    IDataLoggerFactory loggerFactory = null;

    private TmpDirectory tmpDir = null;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("test");


        this.loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());

        this.factory = new PhynixxManagedConnectionFactory<ITestConnection>(new TestConnectionFactory());
        this.factory.setLoggerSystemStrategy(new LoggerPerTransactionStrategy(loggerFactory));

    }

    protected void tearDown() throws Exception {
        TestConnectionStatusManager.clear();

        // delete all tmp files ...
        this.tmpDir.clear();
        this.factory = null;
    }

    private static interface IActOnConnection {
        void doWork(ITestConnection con);
    }


    private void provokeRecoverySituation(IActOnConnection actOnConnection) throws Exception {
        ITestConnection con = null;
        try {
            con = ConnectionRecoveryIT.this.factory.getConnection();
            actOnConnection.doWork(con);
        } catch (ActionInterruptedException ex) {
            log.info("interrupted by testcase");
        } catch (DelegatedRuntimeException dlg) {
            if (!(dlg.getCause() instanceof ActionInterruptedException)) {
                throw dlg;
            }
        } catch (Throwable t) {
            //t.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }

    }

    public void testGoodcase() throws Exception {


        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
                con.act(5);
                con.act(7);

                con.commit();

                synchronized (counter) {
                    ITestConnection coreCon = (ITestConnection) ((IPhynixxManagedConnection) con).getCoreConnection();
                    counter[0] = coreCon.getCounter();
                }
            }
        };
        this.provokeRecoverySituation(actOnConnection);

        // As the TX is finished correctly the logger has to be null


        // TestCase.assertTrue( messageLogger.isCommitting());
        TestCase.assertEquals(5 + 7, counter[0]);
    }


    public void testInteruptedRollback() throws Exception {

        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
                con.setInitialCounter(7);
                con.act(5);
                con.act(7);
                synchronized (counter) {
                    ITestConnection coreCon = (ITestConnection) ((IPhynixxManagedConnection) con).getCoreConnection();
                    counter[0] = coreCon.getCounter();
                }
                TestConnection coreCon = (TestConnection) ((IPhynixxConnectionHandle) con).getConnection();
                coreCon.setInterruptFlag(TestInterruptionPoint.ROLLBACK, 1);
                con.rollback();
            }
        };
        this.provokeRecoverySituation(actOnConnection);


        TestCase.assertEquals(7 + 5 + 7, counter[0]);


    }

    public void testInterruptedCommit() throws Exception {

        final int[] counter = new int[1];


        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
                con.setInitialCounter(3);
                con.act(5);
                con.act(7);
                synchronized (counter) {
                    ITestConnection coreCon = (ITestConnection) ((IPhynixxManagedConnection) con).getCoreConnection();
                    counter[0] = coreCon.getCounter();
                }
                TestConnection coreCon = (TestConnection) ((IPhynixxConnectionHandle) con).getConnection();
                coreCon.setInterruptFlag(TestInterruptionPoint.COMMIT);
                con.commit();
            }
        };

        this.provokeRecoverySituation(actOnConnection);

        PhynixxManagedConnectionFactory.IRecoveredManagedConnection<ITestConnection> cb =
                new PhynixxManagedConnectionFactory.IRecoveredManagedConnection<ITestConnection>() {
                    @Override
                    public void managedConnectionRecovered(ITestConnection con) {

                        Assert.assertEquals(15, con.getCounter());
                    }
                };


        replayLogRecords(cb);


    }

    public void testInterruptedExecution() throws Exception {

        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
                TestConnection coreCon = (TestConnection) ((IPhynixxConnectionHandle) con).getConnection();
                con.setInitialCounter(3);
                con.act(5);
                coreCon.setInterruptFlag(TestInterruptionPoint.ACT);
                con.act(7);
            }
        };

        this.provokeRecoverySituation(actOnConnection);

        PhynixxManagedConnectionFactory.IRecoveredManagedConnection<ITestConnection> cb =
                new PhynixxManagedConnectionFactory.IRecoveredManagedConnection<ITestConnection>() {
                    @Override
                    public void managedConnectionRecovered(ITestConnection con) {
                        Assert.assertEquals(3, con.getCounter());
                    }
                };

        replayLogRecords(cb);


    }


    private void replayLogRecords(PhynixxManagedConnectionFactory.IRecoveredManagedConnection<ITestConnection> cb) {

        this.factory.recover(cb);
    }

    private Properties loadHowlConfig() throws Exception {
        Properties howlprop = new Properties();
        howlprop.put("listConfig", "false");
        howlprop.put("bufferSize", "32");
        howlprop.put("minBuffers", "16");
        howlprop.put("maxBuffers", "16");
        howlprop.put("maxBlocksPerFile", "100");
        howlprop.put("logFileDir", this.tmpDir.getDirectory().getAbsolutePath());
        howlprop.put("logFileName", "test1");
        howlprop.put("maxLogFiles", "6");

        return howlprop;
    }


}
