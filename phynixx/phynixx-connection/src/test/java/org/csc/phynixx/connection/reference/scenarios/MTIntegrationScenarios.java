package org.csc.phynixx.connection.reference.scenarios;

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
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.PooledPhynixxManagedConnectionFactory;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.connection.reference.IReferenceConnection;
import org.csc.phynixx.connection.reference.ReferenceConnectionFactory;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.csc.phynixx.loggersystem.logrecord.IXARecorderResource;
import org.csc.phynixx.loggersystem.logrecord.IXARecorderResourceListener;

import java.util.*;


public class MTIntegrationScenarios extends TestCase {
    private static final int NUMBER_OF_THREADS = 5;
    private static final int NUMBER_OPF_TRIALS = 25;

    private PooledPhynixxManagedConnectionFactory factory = null;

    private TmpDirectory tmpDirectory = null;

    private List runners = null;

    private LoggerSystemManagement loggerManagement = new LoggerSystemManagement();


    private IWorkOnConnection workOnConnection;

    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());

    protected void setUp() throws Exception {
        System.getProperties().setProperty("log4j_level", "INFO");
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDirectory = new TmpDirectory("phynixx_logger");

        this.setUpConnectionFactory();

        EventListenerPhynixx eventListener = new EventListenerPhynixx();
        this.factory.addConnectionProxyDecorator(eventListener);

        this.workOnConnection = new WorkOnConnection();
    }

    private void setUpConnectionFactory() throws Exception {

        if (this.factory != null) {
            this.factory.close();
        }

        // instanciate a connection pool
        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setMaxTotal(100);
        this.factory = new PooledPhynixxManagedConnectionFactory(new ReferenceConnectionFactory(), cfg);
        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("reference", this.tmpDirectory.getDirectory());
        //IDataLoggerFactory loggerFactory= new HowlLoggerFactory("reference", this.loadHowlConfig());

        LoggerPerTransactionStrategy loggingStrategy = new LoggerPerTransactionStrategy(loggerFactory);
        loggingStrategy.addLoggerListener(loggerManagement);
        this.factory.setLoggerSystemStrategy(loggingStrategy);
    }

    protected void tearDown() throws Exception {
        this.factory.close();
        if (this.runners != null) {
            this.shutdown();
        }

        this.tmpDirectory.clear();
    }

    private interface IWorkOnConnection {
        void work(IReferenceConnection con) throws InterruptedException;
    }

    private class WorkOnConnection implements IWorkOnConnection {

        public void work(IReferenceConnection con) throws InterruptedException {
            int rd = new Random().nextInt(100);
            con.setInitialCounter(rd);
            con.incCounter(12);
            con.incCounter(-13);
            try {
                Thread.currentThread().sleep(rd);
            } catch (InterruptedException e) {
                MTIntegrationScenarios.this.logger.info("Interrupted " + con.getId());
                throw e;
            }
            if (rd % 2 == 1) {
                con.rollback();
            } else {
                con.commit();
            }

        }

    }

    private class Runner implements Runnable {

        private volatile boolean killed = false;

        private Thread runningThread = null;


        public boolean isKilled() {
            return killed;
        }

        public void kill() {
            this.killed = true;
        }

        public Thread getRunningThread() {
            return runningThread;
        }

        public void run() {
            IReferenceConnection con = null;

            this.runningThread = Thread.currentThread();
            int printValue = (int) (NUMBER_OPF_TRIALS / 4);

            int cc = 0;


            while (!isKilled() && (++cc) < NUMBER_OPF_TRIALS) {
                try {
                    con = (IReferenceConnection) MTIntegrationScenarios.this.factory.getConnection();
                    try {
                        MTIntegrationScenarios.this.workOnConnection.work(con);
                    } catch (InterruptedException e) {
                    }
                } finally {
                    if (con != null) {
                        con.close();
                    }
                }
                //if( cc%printValue ==1 ) System.out.print(".");

            }
            this.killed = true;
        }
    }


    private void start(int numbers) {

        List runners = new ArrayList(numbers);
        for (int i = 0; i < numbers; i++) {
            Runner runner = new Runner();
            new Thread(runner).start();
            runners.add(runner);
        }
        this.runners = runners;

    }

    private void shutdown() {


        for (Iterator iterator = this.runners.iterator(); iterator.hasNext(); ) {
            Runner runner = (Runner) iterator.next();
            runner.kill();
        }

        for (Iterator iterator = this.runners.iterator(); iterator.hasNext(); ) {
            Runner runner = (Runner) iterator.next();
            try {
                runner.getRunningThread().join();
            } catch (InterruptedException e) {
            }
        }

        TestCase.assertEquals(0, this.loggerManagement.getOpenLoggers());


    }


    public void recover() {
        this.factory.recover(null);

    }

    public class LoggerSystemManagement implements IXARecorderResourceListener {

        private int openLoggerCounter = 0;

        public int getOpenLoggers() {
            return this.openLoggerCounter;
        }


        @Override
        public void recorderResourceClosed(IXARecorderResource recorderResource) {

            this.openLoggerCounter--;
            //System.out.println("Logger "+logger + " closed - No of open Threads " + openLoggerCounter);

        }

        @Override
        public void recorderResourceOpened(IXARecorderResource recorderResource) {
            this.openLoggerCounter++;
            System.out.println("Logger " + logger + " opened - No of open Threads " + openLoggerCounter);

        }
    }


    public void testRunners() throws Exception {

        this.recover();

        start(NUMBER_OF_THREADS);

        System.out.println("Press any Key to stop the test or stop the JVM to recover the connection  next start");
        System.in.read();

        this.shutdown();
        this.runners = null;


    }


    private Properties loadHowlConfig() throws Exception {
        Properties howlprop = new Properties();
        howlprop.put("listConfig", "false");
        howlprop.put("bufferSize", "32");
        howlprop.put("minBuffers", "1");
        howlprop.put("maxBuffers", "1");
        howlprop.put("maxBlocksPerFile", "100");
        howlprop.put("logFileDir", this.tmpDirectory.getDirectory().getAbsolutePath());
        howlprop.put("logFileName", "test1");
        howlprop.put("maxLogFiles", "2");

        return howlprop;
    }


}
