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
import org.csc.phynixx.connection.loggersystem.IPhynixxLoggerSystemStrategy;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.phynixx.testconnection.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MTPooledConnectionIT extends TestCase {
    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());

    private PooledPhynixxManagedConnectionFactory<ITestConnection> factory = null;

    private static final int CONNECTION_POOL_SIZE = 20;

    private TmpDirectory tmpDir = null;

    protected void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        this.tmpDir = new TmpDirectory("howllogger");

        // clears all existing file in dem tmp directory
        this.tmpDir.clear();

        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setMaxTotal(CONNECTION_POOL_SIZE);
        this.factory = new PooledPhynixxManagedConnectionFactory(new TestConnectionFactory(), cfg);
        this.factory.setSynchronizeConnection(true);

        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("pool", this.tmpDir.getDirectory());
        LoggerPerTransactionStrategy strategy = new LoggerPerTransactionStrategy(loggerFactory);

        this.factory.setLoggerSystemStrategy(strategy);

    }

    protected void tearDown() throws Exception {
        TestConnectionStatusManager.clear();

        this.factory.close();


        // delete all tmp files ...
        this.tmpDir.delete();

        this.factory = null;
    }

    private static interface IActOnConnection {
        Object doWork(ITestConnection con);
    }


    private static List exceptions = Collections.synchronizedList(new ArrayList());

    private class Caller implements Callable<Object> {

        private IActOnConnection actOnConnection = null;

        private int repeats= 1;

        private long msecsDelay= -1;

        public Caller(IActOnConnection actOnConnection) {
            this(actOnConnection,1);
        }


        public Caller(IActOnConnection actOnConnection, int repeats) {
            this.actOnConnection = actOnConnection;
            this.repeats= repeats;
        }
        public Object call() {
            ITestConnection con = null;
            Object conId=null;
            try {

                for (int i = 0; i < repeats; i++)
                {
                    try {
                    con = MTPooledConnectionIT.this.factory.getConnection();
                    conId= con.getConnectionId();
                    if(msecsDelay > 0) {
                        Thread.currentThread().sleep(msecsDelay);
                    }
                    try {
                        this.actOnConnection.doWork(con);
                    } catch (ActionInterruptedException e) {
                    } catch (DelegatedRuntimeException e) {
                        if (!(e.getRootCause() instanceof ActionInterruptedException)) {
                            throw e;
                        }
                    }
                    } finally {
                        if (con != null) {
                            con.close();
                        }
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                exceptions.add(new DelegatedRuntimeException("Thread " + Thread.currentThread(), ex));
            }
            return conId;
        }
    };

    private void startRunners(IActOnConnection actOnConnection, int numThreads) throws Exception {
        exceptions.clear();
        ExecutorService executorService= Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            Callable<Object> task = new Caller(actOnConnection);
            executorService.submit(task);
        }


        executorService.shutdown();

        // 10 seconds per execution
        boolean inTime= executorService.awaitTermination(100*CONNECTION_POOL_SIZE, TimeUnit.SECONDS);
        if(!inTime) {
            if(!executorService.isShutdown()) {
                 List<Runnable> runnables = executorService.shutdownNow();
            }
            throw new IllegalStateException("Execution was stopped after "+10*CONNECTION_POOL_SIZE +" seconds");
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

        IActOnConnection actOnConnection = new IActOnConnection() {
            public Object doWork(ITestConnection con) {
                try {
                con.act(5);
                con.act(7);
                con.rollback();
            } finally {
                con.close();

            }

                return con.getConnectionId();
            }
        };

        this.startRunners(actOnConnection,CONNECTION_POOL_SIZE*2);

        // nothing has to be recoverd ...


    }


    public void testInterruptedRollback() throws Exception {

        final int[] counter = new int[1];

        IActOnConnection actOnConnection = new IActOnConnection() {
            public Object doWork(ITestConnection con) {
                Object conId=con.getConnectionId();
                try {
                    con.act(5);
                    con.setInterruptFlag(TestInterruptionPoint.ACT);
                    con.act(7);
                    con.rollback();
                } finally {
                    con.close();
                    return conId;
                }
            }
        };

        this.startRunners(actOnConnection, CONNECTION_POOL_SIZE*2);

        PhynixxRecovery<ITestConnection> recovery= new PhynixxRecovery<ITestConnection>(new TestConnectionFactory());
        IPhynixxLoggerSystemStrategy<ITestConnection> loggerStrategy=  this.factory.getLoggerSystemStrategy();
        loggerStrategy.close();
        recovery.setLoggerSystemStrategy(loggerStrategy);

        recovery.recover(null);

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
