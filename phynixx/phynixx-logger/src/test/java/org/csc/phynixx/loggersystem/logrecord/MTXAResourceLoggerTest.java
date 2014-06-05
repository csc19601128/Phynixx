package org.csc.phynixx.loggersystem.logrecord;

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
import org.csc.phynixx.common.io.LogRecordPageWriter;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * test the mult threading capabilities of phynixx logger
 *
 * phynixx logger aren't multi thread safe. The calling environment has to separet the different logger or has to take that they are synchronized.
 */
public class MTXAResourceLoggerTest extends TestCase {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private TmpDirectory tmpDir = null;
    private final String  MT_MESSAGE = "1234567890qwertzui";

    protected void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("test");
        this.tmpDir.clear();

        this.tmpDir = new TmpDirectory("test");
        System.getProperties().setProperty("howl.log.logFileDir",
                this.tmpDir.getDirectory().getCanonicalPath());

    }

    protected void tearDown() throws Exception {
        // delete all tmp files ...
        this.tmpDir.clear();
    }

    public void testMTLogging() throws Exception {

        // Start XALogger ....

        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        PhynixxXADataRecorder logger =
                PhynixxXADataRecorder.openRecorderForWrite(1, new XADataLogger(loggerFactory.instanciateLogger("1")), null);

        try {
            // Start Threads to fill the Logs.
            List workers = new ArrayList();
            for (int i = 0; i < 1; i++) {
                MessageSampler sampler = new MessageSampler(Integer.valueOf(i).longValue(), logger, MT_MESSAGE, (10 % (i + 1) + 2));
                Thread worker = new Thread(sampler);
                worker.start();
                workers.add(worker);
            }

            // wait until all threads are ready ..
            for (int i = 0; i < workers.size(); i++) {
                Thread worker = (Thread) workers.get(i);
                worker.join();
            }

        } finally {
        }

        try {

            logger.recover();

            List<IDataRecord> openMessages = logger.getDataRecords();

            log.info(openMessages);

            MessageReplay replay = new MessageReplay();
            for (int i = 0; i < openMessages.size(); i++) {
                replay.replayRollback( openMessages.get(i));
               // replay.check();
            }

        } finally {
            logger.close();
        }
    }

    private static class MessageReplay implements IDataRecordReplay {

        private String content = null;
        private StringBuffer contentParts = new StringBuffer();

        public void replayRollback(IDataRecord message) {
            if (message.getOrdinal().intValue() == 1) {
                content = new String(message.getData()[0]);
            } else {
                String part = new String(message.getData()[0]);
                contentParts.append(part);
            }
        }

        public void replayRollforward(IDataRecord message) {
        }

        public void check() {
            if (!content.equals(contentParts.toString())) {
                throw new IllegalStateException("content=" + content + " parts=" + contentParts.toString());
            }
        }

    }

    private class MessageSampler implements Runnable {
        private String message = null;
        private int chunkSize;
        private PhynixxXADataRecorder messageSequence;

        private MessageSampler(long xaDataRecorderId, PhynixxXADataRecorder logger,
                               String message, int chunkSize) throws IOException, InterruptedException {
            this.messageSequence=logger;
            this.message = message;
            this.chunkSize = chunkSize;
            this.messageSequence.createDataRecord(XALogRecordType.USER,
                    new LogRecordPageWriter().newLine().writeUTF(message).toByteArray());

        }

        public String getMessage() {
            return message;
        }

        public int getChunkSize() {
            return chunkSize;
        }

        public void run() {
            // sample the Message
            int countChunks = (message.length() / chunkSize);
            if (message.length() % chunkSize != 0) {
                countChunks++;
            }

            log.info("ChunkSize="+this.chunkSize+" ChunkCount="+countChunks+" MessageLength="+message.length());

            try {
                // write the header record ....
                this.messageSequence.createDataRecord(XALogRecordType.USER, message.getBytes());

                for (int i = 0; i < countChunks; i++) {
                    String messageChunk = message.substring(i * chunkSize, Math
                            .min((i + 1) * chunkSize, message.length()));
                    this.messageSequence.createDataRecord(XALogRecordType.USER, messageChunk.getBytes());

                    // finish the message ...
                    // xaLogger.
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }

    }

}
