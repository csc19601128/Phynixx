package org.csc.phynixx.recovery;

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


import junit.framework.TestCase;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.connection.IPhynixxConnectionHandle;
import org.csc.phynixx.connection.IPhynixxConnectionProxy;
import org.csc.phynixx.connection.ManagedConnectionFactory;
import org.csc.phynixx.connection.loggersystem.PerTransactionStrategy;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.loggersystem.logrecord.IDataRecord;
import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;
import org.csc.phynixx.test_connection.*;
import org.junit.Before;

import java.util.Properties;


public class ConnectionRecoveryTest extends TestCase {
    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private ManagedConnectionFactory<ITestConnection> factory = null;
    IDataLoggerFactory loggerFactory = null;

    private TmpDirectory tmpDir = null;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("howllogger");


        this.loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());

        this.factory = new ManagedConnectionFactory<ITestConnection>(new TestConnectionFactory());
        factory.setLoggerSystemStrategy(new PerTransactionStrategy(loggerFactory));

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

    private class Runner implements Runnable {
        private IDataLogger messageLogger = null;
        private IActOnConnection actOnConnection = null;

        public Runner(IActOnConnection actOnConnection) {
            this.actOnConnection = actOnConnection;
        }

        public void run() {
            ITestConnection con = null;
            final long[] dataRecorderId = new long[]{-1};
            try {
                con = ConnectionRecoveryTest.this.factory.getConnection();
                this.actOnConnection.doWork(con);
                TestConnection coreCon = (TestConnection) ((IPhynixxConnectionHandle) con).getConnection();
                IXADataRecorder xaDataRecorder = coreCon.getXADataRecorder();
                if (xaDataRecorder != null) {
                    dataRecorderId[0] = xaDataRecorder.getXADataRecorderId();
                }
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
                if (dataRecorderId[0] > 0) {
                    try {
                        messageLogger = ConnectionRecoveryTest.this.loggerFactory.instanciateLogger(Long.toString(dataRecorderId[0]));
                    } catch (Exception e) {
                        throw new DelegatedRuntimeException(e);
                    }
                }
            }
        }
    }

    private IDataLogger provokeRecoverySituation(IActOnConnection actOnConnection) throws Exception {

        Runner runner = new Runner(actOnConnection);

        Thread th = new Thread(runner);
        th.start();

        th.join();

        return (runner.messageLogger == null) ? null :;

    }

    public void testGoodcase() throws Exception {


        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
                con.act(5);
                con.act(7);

                con.commit();

                synchronized (counter) {
                    ITestConnection coreCon = (ITestConnection) ((IPhynixxConnectionProxy) con).getConnection();
                    counter[0] = coreCon.getCurrentCounter();
                }
            }
        };
        IDataLogger messageLogger = this.provokeRecoverySituation(actOnConnection);

        // As the TX is finished correctly the logger has to be null

        TestCase.assertTrue(messageLogger == null);

        // TestCase.assertTrue( messageLogger.isCommitting());
        TestCase.assertEquals(5 + 7 + ITestConnection.RF_INCREMENT, counter[0]);
    }


    public void testInteruptedRollback() throws Exception {

        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
                con.setInitialCounter(7);
                con.act(5);
                con.act(7);
                synchronized (counter) {
                    ITestConnection coreCon = (ITestConnection) ((IPhynixxConnectionProxy) con).getConnection();
                    counter[0] = coreCon.getCurrentCounter();
                }
                TestConnection coreCon = (TestConnection) ((IPhynixxConnectionHandle) con).getConnection();
                coreCon.setInterruptFlag(true);
                con.rollback();
            }
        };
        IXADataRecorder messageLogger = this.provokeRecoverySituation(actOnConnection);

        log.info(replayLogRecords(messageLogger));

        TestCase.assertEquals(7 + 5 + 7, counter[0]);

        TestConnection con = (TestConnection) ConnectionRecoveryTest.this.factory.getConnection();
        con.setXADataRecorder(messageLogger);
        con.recover();
        TestCase.assertEquals(0, con.getCurrentCounter());


    }

    public void testInterruptedCommit() throws Exception {

        final int[] counter = new int[1];


        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
                con.setInitialCounter(7);
                con.act(5);
                con.act(7);
                synchronized (counter) {
                    ITestConnection coreCon = (ITestConnection) ((IPhynixxConnectionProxy) con).getConnection();
                    counter[0] = coreCon.getCurrentCounter();
                }
                TestConnection coreCon = (TestConnection) ((IPhynixxConnectionHandle) con).getConnection();
                coreCon.setInterruptFlag(true);
                con.commit();
            }
        };
        IXADataRecorder messageLogger = this.provokeRecoverySituation(actOnConnection);

        log.info(replayLogRecords(messageLogger));

        // Recover the Connection
        TestConnection con = (TestConnection) ConnectionRecoveryTest.this.factory.getConnection();
        con.setXADataRecorder(messageLogger);
        con.recover();
        TestCase.assertEquals(5 + 7 + TestConnection.RF_INCREMENT, con.getCurrentCounter());


    }

    public void testInterruptedExecution() throws Exception {

        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
                TestConnection coreCon = (TestConnection) ((IPhynixxConnectionHandle) con).getConnection();
                con.act(5);
                coreCon.setInterruptFlag(true);
                con.act(7);
            }
        };
        IXADataRecorder messageLogger = this.provokeRecoverySituation(actOnConnection);

        log.info(replayLogRecords(messageLogger));

        // Recover the Connection
        TestConnection con = (TestConnection) ConnectionRecoveryTest.this.factory.getConnection();
        con.setXADataRecorder(messageLogger);
        con.recover();
        TestCase.assertEquals(5 + 7, con.getCurrentCounter());


    }


    private String replayLogRecords(IXADataRecorder logger) {
        final StringBuffer buffer = new StringBuffer();
        IDataRecordReplay replay = new IDataRecordReplay() {

            public void replayRollback(IDataRecord message) {
                buffer.append(message).append("\n");
            }

            public void replayRollforward(IDataRecord message) {
                buffer.append(message).append("\n");
            }
        };
        logger.replayRecords(replay);
        return buffer.toString();
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
