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


import java.util.List;
import java.util.Set;

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

   

    @Test
    public void testXAResourceLogger() throws Exception {

        // Start XALogger ....
        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("mt", this.tmpDir.getDirectory());
        PhynixxXARecorderRepository xaRecorderRepository = new PhynixxXARecorderRepository(loggerFactory);
        IXARecorderRecovery recorderRecovery=null;

        int countMessages = 0;

        try {

        	xaRecorderRepository.open();

            // start the sequence to be tested
            PhynixxXADataRecorder xaDataRecorder1 = (PhynixxXADataRecorder) xaRecorderRepository.createXADataRecorder();
            

            XADataLoggerFacade.startXA(xaDataRecorder1,"test1", "XID".getBytes("UTF-8"));
            LogRecordWriter logWriter1 = new LogRecordWriter();
            logWriter1.writeUTF("Log1").close();
            XADataLoggerFacade.logUserData(xaDataRecorder1, logWriter1.toByteArray());
            XADataLoggerFacade.preparedXA(xaDataRecorder1);
            TestCase.assertTrue(xaDataRecorder1.isPrepared());

            LogRecordPageWriter page = new LogRecordPageWriter();
            page.newLine().writeUTF("A").close();
            page.newLine().writeUTF("B").close();


            XADataLoggerFacade.committingXA(xaDataRecorder1, page.toByteByte());
            TestCase.assertTrue(xaDataRecorder1.isCommitting());
            try {
                LogRecordPageWriter page1 = new LogRecordPageWriter();
                page1.newLine().writeUTF("A").close();
                page1.newLine().writeUTF(".").close();
                XADataLoggerFacade.logUserData(xaDataRecorder1, page1.toByteByte());
                throw new AssertionFailedError("No more RB Data; Sequence is committing");
            } catch (Exception e) {
            }

            // more commiting data are accepted
            XADataLoggerFacade.committingXA(xaDataRecorder1, new LogRecordPageWriter().toByteByte());

            XADataLoggerFacade.doneXA(xaDataRecorder1);
            TestCase.assertTrue(xaDataRecorder1.isCompleted());


            countMessages = xaDataRecorder1.getDataRecords().size();
            
            System.out.println(xaDataRecorder1.getDataRecords());

         xaDataRecorder1.disqualify();


            // xaRecorderRepository.close();
            
            xaRecorderRepository.open();

            // recover the message sequences
            xaRecorderRepository.close();
            
                     
            
			recorderRecovery = new XARecorderRecovery(loggerFactory);
            
            
            Set<IXADataRecorder> xaDataRecorders = recorderRecovery.getRecoveredXADataRecorders();            
            
            

            log.info(xaDataRecorders.toString());

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


            msg = messages.get(2);
            TestCase.assertTrue(msg.getLogRecordType() == XALogRecordType.XA_PREPARED);

            msg = messages.get(3);
            TestCase.assertTrue(msg.getLogRecordType() == XALogRecordType.ROLLFORWARD_DATA);
            TestCase.assertEquals(2, msg.getData().length);
            //TestCase.assertTrue(Arrays.equals("A".getBytes(), msg.getData()[0]));
            //TestCase.assertTrue(Arrays.equals("B".getBytes(), msg.getData()[1]));


            msg = messages.get(5);
            TestCase.assertTrue(msg.getLogRecordType() == XALogRecordType.XA_DONE);

        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	recorderRecovery.destroy();
        	
        }


    }


}
