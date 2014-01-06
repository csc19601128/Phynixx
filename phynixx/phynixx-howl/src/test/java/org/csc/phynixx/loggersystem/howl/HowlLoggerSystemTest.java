package org.csc.phynixx.loggersystem.howl;

/*
 * #%L
 * phynixx-howl
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
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.ILoggerFactory;
import org.csc.phynixx.loggersystem.LoggerSystem;
import org.csc.phynixx.loggersystem.XAResourceLogger;
import org.csc.phynixx.loggersystem.messages.ILogRecordSequence;
import org.objectweb.howl.log.InvalidFileSetException;
import org.objectweb.howl.log.InvalidLogBufferException;
import org.objectweb.howl.log.LogConfigurationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class HowlLoggerSystemTest extends TestCase {

    private static class Chatterer implements Runnable {

        private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

        private XAResourceLogger logger = null;

        public Chatterer(XAResourceLogger logger) throws InvalidFileSetException, LogConfigurationException, InvalidLogBufferException, IOException, InterruptedException {
            super();
            this.logger = logger;
            // log.info("Initialized " + this.logger.getLoggerName());
        }


        public void run() {
            TestUtils.sleep(200);
            try {
                ILogRecordSequence sequence = logger.createMessageSequence();
                for (int i = 0; i < 7; i++) {
                    // log.info("Writing " + this.logger.getLoggerName());
                    this.logger.logUserData(sequence, new byte[][]{this.logger.getLoggerName().getBytes()});
                    TestUtils.sleep((System.currentTimeMillis() % 17) * 20);
                }
            } catch (Exception e) {
                this.log.error(this.logger + " :: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    this.logger.destroy();
                } catch (Exception e) {
                    this.log.error(this.logger + " :: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private TmpDirectory tmpDir = null;

    protected void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();
        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("howllogger");
        this.tmpDir.clear();

        this.tmpDir = new TmpDirectory("howllogger");
        System.getProperties().setProperty("howl.log.logFileDir", this.tmpDir.getDirectory().getCanonicalPath());

    }

    protected void tearDown() throws Exception {
        // delete all tmp files ...
        this.tmpDir.clear();
    }

    public void testHowlLoggerSystem() throws Exception {
        ILoggerFactory loggerFactory = new HowlLoggerFactory("howler");
        LoggerSystem loggerSystem = new LoggerSystem("howler", loggerFactory);

        // Start Threads to fill the Logs.
        Map workers = new HashMap();
        for (int i = 0; i < 5; i++) {
            TestUtils.sleep(10);
            Chatterer chatterer = new Chatterer(loggerSystem.instanciateLogger());
            Thread worker = new Thread(chatterer);
            workers.put(chatterer.logger.getLoggerName(), worker);
            worker.start();
        }

        // wait until all threads are ready ..
        for (Iterator iterator = workers.values().iterator(); iterator.hasNext(); ) {
            Thread worker = (Thread) iterator.next();
            worker.join();
        }


        // recover from file system ...
        Set loggers = loggerSystem.recover();

        for (Iterator iterator = loggers.iterator(); iterator.hasNext(); ) {
            XAResourceLogger logger = (XAResourceLogger) iterator.next();
            TestCase.assertTrue(workers.containsKey(logger.getLoggerName()));
            log.info(logger);
            ILogRecordSequence sequence = logger.createMessageSequence();
            logger.logUserData(sequence, new byte[][]{logger.toString().getBytes()});
            loggerSystem.destroy(logger);
        }

        // this.tmpDir.clear();

        // all loggers have to be destroyed
        loggers = loggerSystem.recover();
        TestCase.assertEquals(0, loggers.size());

    }


}
