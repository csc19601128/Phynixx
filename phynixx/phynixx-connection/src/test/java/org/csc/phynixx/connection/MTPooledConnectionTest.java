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
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.phynixx.test_connection.*;

import java.util.*;


public class MTPooledConnectionTest extends TestCase {
    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());

    private PooledPhynixxManagedConnectionFactory factory = null;

    private static final int POOL_SIZE = 30;


    private TmpDirectory tmpDir = null;

    protected void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        this.tmpDir = new TmpDirectory("howllogger");

        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setMaxTotal(POOL_SIZE);
        this.factory = new PooledPhynixxManagedConnectionFactory(new TestConnectionFactory(), cfg);

        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        LoggerPerTransactionStrategy strategy = new LoggerPerTransactionStrategy(loggerFactory);

        this.factory.setLoggerSystemStrategy(strategy);
        TestConnectionStatusListener recoveryListner = new TestConnectionStatusListener();
        this.factory.addConnectionProxyDecorator(recoveryListner);

    }

    protected void tearDown() throws Exception {
        TestConnectionStatusManager.clear();


        // delete all tmp files ...
        this.tmpDir.clear();

        this.factory = null;
    }

    private static interface IActOnConnection {
        Object doWork(ITestConnection con);
    }


    private static List exceptions = Collections.synchronizedList(new ArrayList());

    private class Runner implements Runnable {

        private IActOnConnection actOnConnection = null;

        public Runner(IActOnConnection actOnConnection) {
            this.actOnConnection = actOnConnection;
        }

        public void run() {
            ITestConnection con = null;
            int repeats = 1; //(int) (System.currentTimeMillis() % 13)+1;
            try {
                Object poolObj = MTPooledConnectionTest.this.factory.getConnection();

                try {
                    con = (ITestConnection) poolObj;
                } catch (ClassCastException e) {
                    throw new ClassCastException("Expected ITestConnection; return " + poolObj.getClass());
                }

                for (int i = 0; i < repeats; i++) {
                    long millis = (long) (System.currentTimeMillis() % 133);
                    Thread.currentThread().sleep(millis);
                    try {
                        this.actOnConnection.doWork((ITestConnection) con);
                    } catch (ActionInterruptedException e) {
                    } catch (DelegatedRuntimeException e) {
                        if (!(e.getRootCause() instanceof ActionInterruptedException)) {
                            throw e;
                        }
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                exceptions.add(new DelegatedRuntimeException("Thread " + Thread.currentThread(), ex));
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        }
    }

    private void startRunners(IActOnConnection actOnConnection, int numThreads) throws Exception {
        exceptions.clear();

        Set workers = new HashSet();
        for (int i = 0; i < numThreads; i++) {
            Runner runner = new Runner(actOnConnection);
            Thread worker = new Thread(runner);
            workers.add(worker);
            worker.start();
        }

        // wait until all threads are ready ..
        for (Iterator iterator = workers.iterator(); iterator.hasNext(); ) {
            Thread worker = (Thread) iterator.next();
            worker.join();
        }

        if (exceptions.size() > 0) {
            for (int i = 0; i < exceptions.size(); i++) {
                Exception ex = (Exception) exceptions.get(i);
                ex.printStackTrace();
            }
            throw new AssertionFailedError("Error occurred");
        }


    }


    public void testGoodCase() throws Exception {

        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public Object doWork(ITestConnection con) {
                con.act(5);
                con.act(7);
                synchronized (counter) {
                    counter[0] = con.getCounter();
                }
                con.rollback();

                return con.getConnectionId();
            }
        };

        this.startRunners(actOnConnection, POOL_SIZE * 4);

        // nothing has to be recoverd ...


    }


    public void testInterruptedRollback() throws Exception {

        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public Object doWork(ITestConnection con) {
                Object conId=con.getConnectionId();
                try {
                    con.act(5);
                    con.act(7);
                     TestConnection coreCon = (TestConnection) ((IPhynixxManagedConnection) con).getCoreConnection();
                    coreCon.setInterruptFlag(TestInterruptionPoint.ACT);
                    con.rollback();
                    } finally {
                        return conId;
                    }
            }
        };

        this.startRunners(actOnConnection, POOL_SIZE * 4);

        this.factory.recover(null);

        // TestStatusStack statusStack= TestConnectionStatusManager.getStatusStack(con.)




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
