package org.csc.phynixx.loggersystem;

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
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.ILoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelLoggerFactory;
import org.csc.phynixx.loggersystem.messages.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MTXAResourceLoggerTest extends TestCase {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private TmpDirectory tmpDir = null;

    protected void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("howllogger");
        this.tmpDir.clear();

        this.tmpDir = new TmpDirectory("howllogger");
        System.getProperties().setProperty("howl.log.logFileDir",
                this.tmpDir.getDirectory().getCanonicalPath());

    }

    protected void tearDown() throws Exception {
        // delete all tmp files ...
        this.tmpDir.clear();
    }

    private String mtMessage = "1234567890qwertzui";

    private static class MessageReply implements IDataRecordReplay {

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
                throw new IllegalStateException("content=" + content
                        + " parts=" + contentParts.toString());
            }
        }

    }

    private class MessageSampler implements Runnable {
        private String message = null;
        private int chunkSize;
        private IDataRecordSequence messageSequence;

        private MessageSampler(long id, PhynixxXAResourceRecorder logger,
                               String message, int chunkSize) {
            this.message = message;
            this.chunkSize = chunkSize;
            this.messageSequence = new PhynixxXADataRecorder(id, );
            this.messageSequence.addLogRecordListener(logger);

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

            // log.info("ChunkSize="+this.chunkSize+" ChunkCount="+
            // countChunks+" MessageLength="+message.length());

            try {
                // write the header record ....
                this.messageSequence.createNewMessage(XALogRecordType.USER)
                        .setData(message.getBytes());

                for (int i = 0; i < countChunks; i++) {
                    String messageChunk = message.substring(i * chunkSize, Math
                            .min((i + 1) * chunkSize, message.length()));
                    this.messageSequence.createNewLogRecord().setData(
                            messageChunk.getBytes());

                    // finish the message ...
                    // xaLogger.
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }

    }

    public void testMTLogging() throws Exception {

        // Start XALogger ....

        ILoggerFactory loggerFactory = new FileChannelLoggerFactory("mt",
                this.tmpDir.getDirectory());
        PhynixxXAResourceRecorder logger = new PhynixxXAResourceRecorder(loggerFactory
                .instanciateLogger("mt_1"));

        try {
            logger.open();

            // Start Threads to fill the Logs.
            List workers = new ArrayList();
            for (int i = 0; i < 10; i++) {
                MessageSampler sampler = new MessageSampler(i, logger,
                        mtMessage, (10 % (i + 1) + 2));
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
            logger.destroy();
        }

        try {

            logger.open();

            // recover the message sequences
            logger.readMessageSequences();

            List openMessages = logger.getOpenMessageSequences();

            log.info(openMessages);

            for (int i = 0; i < openMessages.size(); i++) {

                MessageReply replay = new MessageReply();
                ((IDataRecordSequence) openMessages.get(i))
                        .replayRecords(replay);
                replay.check();
            }

        } finally {
            logger.close();
        }

    }

    private Properties loadHowlConfig() throws Exception {
        Properties howlprop = new Properties();
        howlprop.put("listConfig", "false");
        howlprop.put("bufferSize", "32");
        howlprop.put("minBuffers", "16");
        howlprop.put("maxBuffers", "16");
        howlprop.put("maxBlocksPerFile", "100");
        howlprop
                .put("logFileDir", this.tmpDir.getDirectory().getAbsolutePath());
        howlprop.put("logFileName", "test1");
        howlprop.put("maxLogFiles", "6");

        return howlprop;
    }

}
