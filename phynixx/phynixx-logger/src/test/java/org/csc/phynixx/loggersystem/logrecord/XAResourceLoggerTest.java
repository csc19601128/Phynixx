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


import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.io.LogRecordPageReader;
import org.csc.phynixx.common.io.LogRecordPageWriter;
import org.csc.phynixx.common.io.LogRecordWriter;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;
import java.util.Set;

public class XAResourceLoggerTest {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private TmpDirectory tmpDir = null;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("howllogger");
        this.tmpDir.clear();

        this.tmpDir = new TmpDirectory("howllogger");
        System.getProperties().setProperty("howl.log.logFileDir", this.tmpDir.getDirectory().getCanonicalPath());

    }

    @After
    public void tearDown() throws Exception {
        // delete all tmp files ...
        this.tmpDir.clear();
    }

    private Properties loadHowlConfig() throws Exception {
        Properties howlprop = new Properties();
        howlprop.put("listConfig", "true");
        howlprop.put("bufferSize", "32");
        howlprop.put("minBuffers", "16");
        howlprop.put("maxBuffers", "16");
        howlprop.put("maxBlocksPerFile", "10");
        howlprop.put("logFileDir", this.tmpDir.getDirectory().getAbsolutePath());
        howlprop.put("logFileName", "test1");
        howlprop.put("maxLogFiles", "6");

        return howlprop;
    }


    @Test
    public void testXAResourceLogger() throws Exception {

        // Start XALogger ....
        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        PhynixxXARecorderRepository xaRecorderResource = new PhynixxXARecorderRepository(loggerFactory);

        int countMessages = 0;

        try {

            xaRecorderResource.open();

            // start the sequence to be tested
            PhynixxXADataRecorder xaDataRecorder1 = (PhynixxXADataRecorder) xaRecorderResource.createXADataRecorder();

            xaRecorderResource.startXA(xaDataRecorder1, "test1", "XID".getBytes());
            LogRecordWriter logWriter1 = new LogRecordWriter();
            logWriter1.writeUTF("Log1").close();
            xaRecorderResource.logUserData(xaDataRecorder1, logWriter1.toByteArray());
            xaRecorderResource.preparedXA(xaDataRecorder1);
            TestCase.assertTrue(xaDataRecorder1.isPrepared());

            LogRecordPageWriter page = new LogRecordPageWriter();
            page.newLine().writeUTF("A").close();
            page.newLine().writeUTF("B").close();


            xaRecorderResource.committingXA(xaDataRecorder1, page.toByteByte());
            TestCase.assertTrue(xaDataRecorder1.isCommitting());
            try {
                LogRecordPageWriter page1 = new LogRecordPageWriter();
                page1.newLine().writeUTF("A").close();
                page1.newLine().writeUTF(".").close();
                xaRecorderResource.logUserData(xaDataRecorder1, page1.toByteByte());
                throw new AssertionFailedError("No more RB Data; Sequence is committing");
            } catch (Exception e) {
            }

            // more commiting data are accepted
            xaRecorderResource.committingXA(xaDataRecorder1, new LogRecordPageWriter().toByteByte());

            xaRecorderResource.doneXA(xaDataRecorder1);
            TestCase.assertTrue(xaDataRecorder1.isCompleted());


            countMessages = xaDataRecorder1.getDataRecords().size();


            xaRecorderResource.open();

            // recover the message sequences
            Set<IXADataRecorder> xaDataRecorders = xaRecorderResource.getXADataRecorders();

            log.info(xaDataRecorders);

            TestCase.assertEquals(1, xaDataRecorders.size());

            PhynixxXADataRecorder dataRecorder2 = (PhynixxXADataRecorder) xaDataRecorders.iterator().next();
            List<IDataRecord> messages = dataRecorder2.getDataRecords();

            TestCase.assertTrue(dataRecorder2.isCompleted());
            TestCase.assertEquals(countMessages, messages.size());

            IDataRecord msg = messages.get(0);
            TestCase.assertTrue(msg.getLogRecordType() == XALogRecordType.XA_START);

            msg = messages.get(1);
            TestCase.assertTrue(msg.getLogRecordType() == XALogRecordType.USER);
            TestCase.assertTrue(msg.getData().length == 1);
            LogRecordPageReader pageReader1 = new LogRecordPageReader(msg.getData());
            TestCase.assertEquals("Log1", pageReader1.getLogReaders().get(0).readUTF());


            msg = (IDataRecord) messages.get(2);
            TestCase.assertTrue(msg.getLogRecordType() == XALogRecordType.XA_PREPARED);

            msg = (IDataRecord) messages.get(3);
            TestCase.assertTrue(msg.getLogRecordType() == XALogRecordType.ROLLFORWARD_DATA);
            TestCase.assertEquals(2, msg.getData().length);
            //TestCase.assertTrue(Arrays.equals("A".getBytes(), msg.getData()[0]));
            //TestCase.assertTrue(Arrays.equals("B".getBytes(), msg.getData()[1]));


            msg = (IDataRecord) messages.get(5);
            TestCase.assertTrue(msg.getLogRecordType() == XALogRecordType.XA_DONE);


        } finally {
            xaRecorderResource.close();
        }


    }


}
