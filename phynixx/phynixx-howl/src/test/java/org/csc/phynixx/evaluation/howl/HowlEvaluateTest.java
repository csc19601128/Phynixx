package org.csc.phynixx.phynixx.evaluation.howl;

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
import org.csc.phynixx.common.generator.IDGenerator;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.objectweb.howl.log.xa.XACommittingTx;
import org.objectweb.howl.log.xa.XALogger;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;


public class HowlEvaluateTest extends TestCase {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private TmpDirectory tmpDirectory = new TmpDirectory();


    private IDGenerator generator = new IDGenerator();

    private String mtMessage = "1234567890qwertzui";


    protected void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDirectory.clear();
    }

    protected void tearDown() throws Exception {
        // delete all tmp files ...
        this.tmpDirectory.clear();
    }

	/*
    private void provokeRecoverySituation() throws Exception
	{
		Runnable runnable= new Runnable() {
			public void run() 
			{				
				ISampleXAConnection xaCon1 = HowlEvaluateTest.this.factory1.getXAConnection();	
				TestConnectionProxy con1= (TestConnectionProxy)xaCon1.getConnection();	
				
				ISampleXAConnection xaCon2 = HowlEvaluateTest.this.factory2.getXAConnection();	
				TestConnectionProxy con2= (TestConnectionProxy)xaCon2.getConnection();	
				
				try {	
					HowlEvaluateTest.this.getJotm().getTransactionManager().begin();
					// act transactional and enlist the current resource
					con1.act();
					con2.act(); 
					// no act on the second  resource ...
					// .... con2.act();
					
					// Thread is aborted during commit ....
					HowlEvaluateTest.this.getJotm().getTransactionManager().commit();
				} catch(Exception e) {
					log.error("Error message "+e.getMessage());
				}	finally {
					if( con1!=null) {
						con1.close();
					}
					if( con2!=null) {
						con2.close();
					}
				}
			}
		};
		
		
		Thread runner= new Thread(runnable);
		runner.start(); 
		
		runner.join();
		
		
		// unlock the files ....
		//this.unlockLogfiles();
		//String property="org.objectweb.howl." + name.getAbsolutePath() + ".locked";
		this.jotm.stop(); 
		this.jotm= new Jotm(true,false); 
		// debug recovery log content ...
        Configuration cfg = this.loadHowlConfig();
        XALogger xaLog = new XALogger(cfg);
        //xaLog.open(new TestReplayListener());
        
        // xaLog.replay(new TestReplayListener());
	}
	*/

    public void testLogging() throws Exception {


        // Start XALogger ....
        XALogger xaLogger = new XALogger(loadHowlConfig(new Properties()));
        xaLogger.open(null);

        ByteBuffer buffer = ByteBuffer.allocate(1000);
        String rec1 = "qwwrzweiru";
        String rec2 = "vbmcvcxmnfdhvb";

        byte[][] rec = new byte[2][];

        rec[0] = rec1.getBytes();
        rec[1] = rec2.getBytes();
        xaLogger.put(rec, true);

        log.info(xaLogger.getStats());
        xaLogger.close();

        xaLogger.open(null);

        log.info("Active mark=" + xaLogger.getActiveMark());
        xaLogger.activeTxDisplay();

        xaLogger.replay(new TestReplayListener());

        xaLogger.close();


    }

    public void testXACommittingTx() throws Exception {


        // Start XALogger ....
        XALogger xaLogger = new XALogger(loadHowlConfig(new Properties()));
        xaLogger.open(null);

        ByteBuffer buffer = ByteBuffer.allocate(1000);
        String rec1 = "qwwrzweiru";
        String rec2 = "vbmcvcxmnfdhvb";
        long refPtr = xaLogger.put(rec1.getBytes(), false);

        byte[][] rec = new byte[2][];

        rec[0] = ("COMMITTING-" + rec1).getBytes();
        rec[1] = ("COMMITTING-" + rec2).getBytes();

        XACommittingTx xaCommittingTX = xaLogger.putCommit(rec);

        // xaLogger.putDone(null, xaCommittingTX);

        System.out.println(xaLogger.getStats());
        xaLogger.close();

        xaLogger.open(null);

        log.info("Active mark=" + xaLogger.getActiveMark());
        xaLogger.activeTxDisplay();

        xaLogger.replay(new TestReplayListener());

        LogRecord tmpRecord = xaLogger.get(null, refPtr);
        log.info("Record at [" + refPtr + "]=" + new String(tmpRecord.getFields()[0]));

        xaLogger.close();


    }


    private class MessageSampler implements Runnable {
        private String message = null;
        private int chunkSize;
        private Logger logger;
        private long messageId = HowlEvaluateTest.this.generator.generateLong();

        private MessageSampler(Logger logger, String message, int chunkSize) {
            this.message = message;
            this.chunkSize = chunkSize;
            this.logger = logger;
        }

        public String getMessage() {
            return message;
        }

        public int getChunkSize() {
            return chunkSize;
        }

        public void run() {
            // sample the  Message
            int countChunks = (message.length() / chunkSize);
            if (message.length() % chunkSize != 0) {
                countChunks++;
            }

            //log.info("ChunkSize="+this.chunkSize+" ChunkCount="+ countChunks+" MessageLength="+message.length());

            try {
                // write the header record ....
                long referencePtr = this.writeData(-1, messageId, message);
                for (int i = 0; i < countChunks; i++) {
                    String messageChunk = message.substring(i * chunkSize,
                            Math.min((i + 1) * chunkSize, message.length()));
                    referencePtr = writeData(referencePtr, messageId, messageChunk);

                    // finish the message ...
                    // xaLogger.
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        private long writeData(long backwardPtr, long messageId, String data) throws IOException,
                LogClosedException, LogRecordSizeException,
                LogFileOverflowException, InterruptedException {
            DataOutputStream io = null;
            long referencePtr = -1;
            try {
                ByteArrayOutputStream byteIO = new ByteArrayOutputStream(4 + 4 + data.length() * 2);
                io = new DataOutputStream(byteIO);

                io.writeLong(backwardPtr);
                io.writeLong(messageId);

                //log.info("Header Size="+io.size());

                io.writeUTF(data);

                byte[] content = byteIO.toByteArray();
                referencePtr = logger.put(content, true);
            } finally {
                if (io != null) {
                    io.close();
                }
            }
            return referencePtr;
        }
    }


    public void testMTLogging() throws Exception {


        // Start XALogger ....
        Logger logger = new Logger(loadHowlConfig(new Properties()));
        TestLogEventListener listener = new TestLogEventListener();
        logger.setLogEventListener(listener);

        try {
            logger.open();

            // Start Threads to fill the Logs.
            List workers = new ArrayList();
            for (int i = 0; i < 100; i++) {
                MessageSampler sampler = new MessageSampler(logger, mtMessage, 10);
                Thread worker = new Thread(sampler);
                worker.run();
                workers.add(worker);
            }

            // wait until all threads are ready ..
            for (int i = 0; i < workers.size(); i++) {
                Thread worker = (Thread) workers.get(i);
                worker.join();
            }

            log.info("XALogger.overflow-Notification=" + listener.count);
        } finally {
            logger.close();
        }

        XALogger xaLogger = new XALogger(loadHowlConfig(new Properties()));
        xaLogger.open(null);
        try {
            log.info("Active mark=" + xaLogger.getActiveMark());
            xaLogger.activeTxDisplay();
            ChainedMessageReplayListener replayListener = new ChainedMessageReplayListener();
            xaLogger.replay(replayListener);

            Map logMessages = replayListener.getLogMessages();
            DataInputStream inputIO = null;
            for (Iterator iterator = logMessages.values().iterator(); iterator.hasNext(); ) {
                LogMessage lm = (LogMessage) iterator.next();
                String message = null;
                byte[][] data = lm.getData();
                try {
                    inputIO = new DataInputStream(new ByteArrayInputStream(data[0]));
                    // first dataChunk is the complete message ....
                    message = inputIO.readUTF();

                } finally {
                    if (inputIO != null) {
                        inputIO.close();
                        inputIO = null;
                    }
                }


                // the remaining chunks are concatenated to represent the same message ....
                StringBuffer buffer = new StringBuffer();
                for (int i = 1; i < data.length; i++) {
                    try {
                        inputIO = new DataInputStream(new ByteArrayInputStream(data[i]));
                        buffer.append(inputIO.readUTF());
                    } finally {
                        if (inputIO != null) {
                            inputIO.close();
                            inputIO = null;
                        }
                    }

                }
                log.info("MessageId=" + lm.getMessageId() + " OriginalMessage=" + message + " concatenated message=" + buffer.toString());
                TestCase.assertEquals(message, buffer.toString());

            }

        } finally {
            xaLogger.close();
        }

    }

    private Configuration loadHowlConfig(Properties systEnv) throws Exception {

        Properties howlprop = new Properties();
        String myhowlprop = null;
        myhowlprop = systEnv.getProperty("howl.log.ListConfiguration", "false");
        howlprop.put("listConfig", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.BufferSize", "32"); // in KB
        howlprop.put("bufferSize", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.MinimumBuffers", "16");
        howlprop.put("minBuffers", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.MaximumBuffers", "16");
        howlprop.put("maxBuffers", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.MaximumBlocksPerFile", "100");
        howlprop.put("maxBlocksPerFile", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.FileDirectory", this.tmpDirectory.dir.getAbsolutePath());
        howlprop.put("logFileDir", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.FileName", "test1");
        howlprop.put("logFileName", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.MaximumFiles", "6");
        howlprop.put("maxLogFiles", myhowlprop);

        return new Configuration(howlprop);
    }


    private static class TmpDirectory {
        private File dir = null;

        public TmpDirectory() {
            dir = new File("./tmp");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        public synchronized void clear() {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }
    }
}
