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
import org.csc.phynixx.connection.DynaProxyFactory;
import org.csc.phynixx.connection.IPhynixxConnectionHandle;
import org.csc.phynixx.connection.IPhynixxConnectionProxy;
import org.csc.phynixx.connection.IPhynixxConnectionProxyFactory;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.Dev0Strategy;
import org.csc.phynixx.loggersystem.ILoggerFactory;
import org.csc.phynixx.loggersystem.ILoggerSystemStrategy;
import org.csc.phynixx.loggersystem.PerTransactionStrategy;
import org.csc.phynixx.loggersystem.channellogger.FileChannelLoggerFactory;
import org.csc.phynixx.loggersystem.messages.ILogRecord;
import org.csc.phynixx.loggersystem.messages.ILogRecordReplay;
import org.csc.phynixx.loggersystem.messages.IRecordLogger;
import org.csc.phynixx.test_connection.*;

import java.util.Properties;

public class ConnectionRecoveryTest extends TestCase {
    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private TestConnectionFactory factory = null;

    private IPhynixxConnectionProxyFactory proxyFactory = null;

    private ILoggerSystemStrategy strategy = null;

    private TmpDirectory tmpDir = null;

    protected void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("howllogger");

        this.factory = new TestConnectionFactory();
        this.proxyFactory = new DynaProxyFactory(new Class[]{ITestConnection.class});

        this.strategy = new Dev0Strategy();
    }

    protected void tearDown() throws Exception {
        TestConnectionStatusManager.clear();

        // delete all tmp files ...
        this.tmpDir.clear();
        this.factory = null;
        this.strategy = new Dev0Strategy();
    }

    private static interface IActOnConnection {
        void doWork(ITestConnection con);
    }

    private class Runner implements Runnable {
        private IRecordLogger messageLogger = null;
        private IActOnConnection actOnConnection = null;

        public Runner(IActOnConnection actOnConnection) {
            this.actOnConnection = actOnConnection;
        }

        public void run() {
            ITestConnection con = null;

            try {
                con = (ITestConnection) ConnectionRecoveryTest.this.factory.getConnection();
                IPhynixxConnectionProxy proxy = ConnectionRecoveryTest.this.proxyFactory.getConnectionProxy();
                proxy.addConnectionListener(ConnectionRecoveryTest.this.strategy);
                proxy.setConnection(con);

                this.actOnConnection.doWork((ITestConnection) proxy);
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
                messageLogger = con.getRecordLogger();
            }
        }
    }

    ;

    private IRecordLogger provokeRecoverySituation(IActOnConnection actOnConnection) throws Exception {

        Runner runner = new Runner(actOnConnection);

        Thread th = new Thread(runner);
        th.start();

        th.join();

        return runner.messageLogger;

    }

    public void testGoodcase() throws Exception {


        ILoggerFactory loggerFactory = new FileChannelLoggerFactory("mt", this.tmpDir.getDirectory());
        this.strategy = new PerTransactionStrategy("abcd", loggerFactory);

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
        IRecordLogger messageLogger = this.provokeRecoverySituation(actOnConnection);

        // As the TX is finished correctly the logger has to be null

        TestCase.assertTrue(messageLogger == null);

        // TestCase.assertTrue( messageLogger.isCommitting());
        TestCase.assertEquals(5 + 7 + ITestConnection.RF_INCREMENT, counter[0]);
    }


    public void testInteruptedRollback() throws Exception {

        ILoggerFactory loggerFactory = new FileChannelLoggerFactory("mt", this.tmpDir.getDirectory());
        this.strategy = new PerTransactionStrategy("abcd", loggerFactory);

        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
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
        IRecordLogger messageLogger = this.provokeRecoverySituation(actOnConnection);

        log.info(replayLogRecords(messageLogger));

        TestCase.assertEquals(5 + 7, counter[0]);

        TestConnection con = (TestConnection) ConnectionRecoveryTest.this.factory.getConnection();
        con.setRecordLogger(messageLogger);
        con.recover();
        TestCase.assertEquals(5 + 7, con.getCurrentCounter());


    }

    public void testInterruptedCommit() throws Exception {

        ILoggerFactory loggerFactory = new FileChannelLoggerFactory("mt", this.tmpDir.getDirectory());
        this.strategy = new PerTransactionStrategy("abcd", loggerFactory);

        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
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
        IRecordLogger messageLogger = this.provokeRecoverySituation(actOnConnection);

        log.info(replayLogRecords(messageLogger));

        // Recover the Connection
        TestConnection con = (TestConnection) ConnectionRecoveryTest.this.factory.getConnection();
        con.setRecordLogger(messageLogger);
        con.recover();
        TestCase.assertEquals(5 + 7 + TestConnection.RF_INCREMENT, con.getCurrentCounter());


    }

    public void testInterruptedExecution() throws Exception {

        ILoggerFactory loggerFactory = new FileChannelLoggerFactory("mt", this.tmpDir.getDirectory());
        this.strategy = new PerTransactionStrategy("abcd", loggerFactory);

        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public void doWork(ITestConnection con) {
                TestConnection coreCon = (TestConnection) ((IPhynixxConnectionHandle) con).getConnection();
                con.act(5);
                coreCon.setInterruptFlag(true);
                con.act(7);
            }
        };
        IRecordLogger messageLogger = this.provokeRecoverySituation(actOnConnection);

        log.info(replayLogRecords(messageLogger));

        // Recover the Connection
        TestConnection con = (TestConnection) ConnectionRecoveryTest.this.factory.getConnection();
        con.setRecordLogger(messageLogger);
        con.recover();
        TestCase.assertEquals(5 + 7, con.getCurrentCounter());


    }


    private String replayLogRecords(IRecordLogger logger) {
        final StringBuffer buffer = new StringBuffer();
        ILogRecordReplay replay = new ILogRecordReplay() {

            public void replayRollback(ILogRecord message) {
                buffer.append(message).append("\n");
            }

            public void replayRollforward(ILogRecord message) {
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
