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
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;

public class MTXAResourceLoggerTest extends TestCase {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private TmpDirectory tmpDir = null;

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
    /**

     private class MessageSampler implements Runnable {
     private String message = null;
     private int chunkSize;
     private PhynixxXADataRecorder messageSequence;

     private MessageSampler(String xaDataRecorderId, PhynixxXARecorderResource logger,
     String message, int chunkSize) throws IOException, InterruptedException {
     this.message = message;
     this.chunkSize = chunkSize;
     this.messageSequence = logger.logUserData(XALogRecordType.USER, message.getBytes(Charsets.UTF_8);
     // this.messageSequence.addLogRecordListener(logger);

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

     public void testMTLogging() throws Exception {

     // Start XALogger ....

     IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt",this.tmpDir.getDirectory());
     PhynixxXADataRecorder logger=PhynixxXADataRecorder.openRecorderForWrite("1", new XADataLogger(loggerFactory.instanciateLogger("1")), null);

     try {
     // Start Threads to fill the Logs.
     List workers = new ArrayList();
     for (int i = 0; i < 10; i++) {
     MessageSampler sampler = new MessageSampler(i, logger, mtMessage, (10 % (i + 1) + 2));
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

     logger.reopen();

     // recover the message sequences
     logger.readMessageSequences();

     List openMessages = logger.getXADataRecorders();

     log.info(openMessages);

     for (int i = 0; i < openMessages.size(); i++) {

     MessageReply replay = new MessageReply();
     //((IDataRecordSequence) openMessages.get(i)).replayRecords(replay);
     replay.check();
     }

     } finally {
     logger.close();
     }

     }

     private Properties loadHowlConfig() throws Exception {
     Properties howlprop = new Properties();
     howlprop.associate("listConfig", "false");
     howlprop.associate("bufferSize", "32");
     howlprop.associate("minBuffers", "16");
     howlprop.associate("maxBuffers", "16");
     howlprop.associate("maxBlocksPerFile", "100");
     howlprop
     .associate("logFileDir", this.tmpDir.getDirectory().getAbsolutePath());
     howlprop.associate("logFileName", "test1");
     howlprop.associate("maxLogFiles", "6");

     return howlprop;
     }
     **/
}
