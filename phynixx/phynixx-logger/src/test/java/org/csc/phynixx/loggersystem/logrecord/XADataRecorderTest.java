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
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class XADataRecorderTest {

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        new TmpDirectory().clear();

    }

    @After
    public void tearDown() throws Exception {
        // delete all tmp files ...
        new TmpDirectory().clear();
    }

    @Test
    public void testMessageLogger() throws Exception {

        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("reference", new TmpDirectory().getDirectory());
        PhynixxXARecorderRepository xaResource = new PhynixxXARecorderRepository(loggerFactory);

        PhynixxXADataRecorder xaDataRecorder = (PhynixxXADataRecorder) xaResource.createXADataRecorder();


        TestCase.assertTrue(!xaDataRecorder.isCommitting());

        xaDataRecorder.writeRollbackData("XYZ".getBytes("UTF-8"));
        xaDataRecorder.writeRollbackData(new byte[][]{"XYZ".getBytes("UTF-8"), "ZYX".getBytes("UTF-8")});

        TestCase.assertTrue(!xaDataRecorder.isCommitting());
        xaDataRecorder.writeRollforwardData(new byte[][]{"XYZ".getBytes("UTF-8"), "ZYX".getBytes("UTF-8")});
        TestCase.assertTrue(xaDataRecorder.isCommitting());

        xaDataRecorder.writeRollforwardData(new byte[][]{"ABCD".getBytes("UTF-8")});

        // if the transaction is commiting no rollback data may be written
        try {
            xaDataRecorder.writeRollbackData(new byte[][]{});
            throw new AssertionFailedError("No more RF Data allowed; Sequence is committing");
        } catch (Exception e) {
        }


        TestCase.assertTrue(xaDataRecorder.isCommitting());
        TestCase.assertTrue(!xaDataRecorder.isCompleted());


        // reply the logrecord
        IDataRecordReplay replay = new IDataRecordReplay() {
            public void replayRollback(IDataRecord message) {
                switch ((int) message.getOrdinal().longValue()) {
                    case 1:
                        TestCase.assertEquals("XYZ", new String(message.getData()[0]));
                        break;
                    case 2:
                        TestCase.assertEquals("XYZ", new String(message.getData()[0]));
                        TestCase.assertEquals("ZYX", new String(message.getData()[1]));
                        break;
                    default:
                        throw new AssertionFailedError("Unexpected Message " + message);
                }
            }

            public void replayRollforward(IDataRecord message) {
                if (message.getOrdinal().longValue() == 3) {
                    TestCase.assertEquals("XYZ", new String(message.getData()[0]));
                    TestCase.assertEquals("ZYX", new String(message.getData()[1]));
                } else if (message.getOrdinal().longValue() == 4) {
                    TestCase.assertEquals("ABCD", new String(message.getData()[0]));
                } else {
                    throw new AssertionFailedError("Unexpected Message " + message);
                }
            }

        };

        xaDataRecorder.replayRecords(replay);


    }


}
