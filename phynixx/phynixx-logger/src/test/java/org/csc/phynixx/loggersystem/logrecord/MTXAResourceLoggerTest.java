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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.io.LogRecordPageReader;
import org.csc.phynixx.common.io.LogRecordPageWriter;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * test the mult threading capabilities of phynixx logger
 *
 * phynixx logger aren't multi thread safe. The calling environment has to separet the different logger or has to take that they are synchronized.
 */
public class MTXAResourceLoggerTest {

    public static final int NUM_THREADS = 10;
    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private TmpDirectory tmpDir = null;
    private final String  MT_MESSAGE = "1234567890qwertzui";

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("test");
        this.tmpDir.clear();

        this.tmpDir = new TmpDirectory("test");
        System.getProperties().setProperty("howl.log.logFileDir",
                this.tmpDir.getDirectory().getCanonicalPath());

    }

    @After
    public void tearDown() throws Exception {
        // delete all tmp files ...
        this.tmpDir.clear();
    }

    @Test
    public void testMTLogging() throws Exception {

        // Start XALogger ....

        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        PhynixxXARecorderRepository repository= new PhynixxXARecorderRepository(loggerFactory);
        try {
            // Start Threads to fill the Logs.
            List<Thread> workers = new ArrayList<Thread>();
            for (int i = 0; i < NUM_THREADS; i++) {
                MessageSampler sampler = new MessageSampler(i, repository, MT_MESSAGE, (10 % (i + 1) + 2));
                Thread worker = new Thread(sampler);
                worker.start();
                workers.add(worker);
            }

            // wait until all threads are ready ..
            for (int i = 0; i < workers.size(); i++) {
                Thread worker = workers.get(i);
                worker.join();
            }

        } finally {
        }


        
        repository.close();
        IXARecorderRecovery recorderRecovery=null;

        try {
			recorderRecovery = new XARecorderRecovery(loggerFactory);

            Set<IXADataRecorder> dataRecorders = recorderRecovery.getRecoveredXADataRecorders();
            Assert.assertEquals(NUM_THREADS, dataRecorders.size());

            log.info(dataRecorders.toString());

            for (IXADataRecorder dataRecorder : dataRecorders) {
                final List<IDataRecord> records = dataRecorder.getDataRecords();
                MessageReplay replay = new MessageReplay();
                for (int i = 0; i < records.size(); i++) {
                    replay.replayRollback(records.get(i));
                }
                replay.check();

            }

        } finally {
        	recorderRecovery.close();
        }
    }

    private static class MessageReplay implements IDataRecordReplay {

        private String content = null;
        private StringBuffer contentParts = new StringBuffer();

        @Override
        public void notifyNoMoreData() {

        }


        @Override
      public void replayRollback(IDataRecord message) {
            String value=null;
            try {
                value = new LogRecordPageReader(message.getData()).getLogReaders().get(0).readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message.getOrdinal().intValue() == 1) {
                this.content= value;
            } else {
                String part =value;
                contentParts.append(part);
            }
        }

        @Override
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
        private IXADataRecorder dataRecorder;

        private MessageSampler(int index,IXARecorderRepository repository,
                               String message, int chunkSize) throws Exception {
            this.dataRecorder = repository.createXADataRecorder();
            this.message = message;
            this.chunkSize = chunkSize;
            this.dataRecorder.createDataRecord(XALogRecordType.USER,
                    new LogRecordPageWriter().newLine().writeUTF(message).toByteArray());

        }

        public String getMessage() {
            return message;
        }

        public int getChunkSize() {
            return chunkSize;
        }

        @Override
      public void run() {
            // sample the Message
            int countChunks = (message.length() / chunkSize);
            if (message.length() % chunkSize != 0) {
                countChunks++;
            }

            log.info("ChunkSize="+this.chunkSize+" ChunkCount="+countChunks+" MessageLength="+message.length());

            try {
                for (int i = 0; i < countChunks; i++) {
                    if(i*chunkSize < message.length()) {
                        String messageChunk = message.substring(i * chunkSize, Math.min((i + 1) * chunkSize, message.length()));
                        this.dataRecorder.createDataRecord(XALogRecordType.USER, new LogRecordPageWriter().newLine().writeUTF(messageChunk).toByteArray());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }

        }

    }

}
