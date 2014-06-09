package org.csc.phynixx.loggersystem.logger.channellogger;

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
import org.csc.phynixx.loggersystem.logrecord.ILogRecordReplayListener;
import org.csc.phynixx.loggersystem.logrecord.XALogRecordType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

public class FileChannelDataLoggerTest {


    private TmpDirectory tmpDir = null;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();
        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("channel");
        this.tmpDir.clear();
    }

    @After
    public void tearDown() throws Exception {
        // delete all tmp files ...
        this.tmpDir.clear();
    }

    @Test
    public void testWriteOnReadableLogger() throws IOException {

        byte[][] DATA1 = new byte[][]{"abcde".getBytes("UTF-8")};

        File loggerFile = new File(this.tmpDir.getDirectory().getAbsolutePath() + "/+logger_1.log");
        FileChannelDataLogger logger = new FileChannelDataLogger(loggerFile, AccessMode.READ);
        try {

            try {
                logger.write((short) 1, DATA1);
                Assert.fail("Write is not permitted");
            } catch (Exception e) {}
        } finally {
            if (logger != null) {
                logger.destroy();
            }
        }

    }


    @Test
    public void testReopenCloseLogger() throws IOException {

        byte[][] DATA1 = new byte[][]{"abcde".getBytes("UTF-8")};

        File loggerFile = new File(this.tmpDir.getDirectory().getAbsolutePath() + "/+logger_1.log");
        FileChannelDataLogger logger = new FileChannelDataLogger(loggerFile, AccessMode.APPEND);
        try {

            logger.write((short) 1, DATA1);
            logger.close();

            logger.reopen(AccessMode.READ);

            ILogRecordReplayListener replayListener = Mockito.mock(ILogRecordReplayListener.class);
            logger.replay(replayListener);

            Mockito.verify(replayListener, Mockito.times(1)).onRecord(Mockito.<XALogRecordType>any(), Mockito.<byte[][]>any());
        } finally {
            if (logger != null) {
                logger.destroy();
            }
        }

    }


    @Test
    public void testRestLoggerByReopenWithWrite() throws IOException {
        byte[][] DATA1 = new byte[][]{"abcde".getBytes("UTF-8")};

        File loggerFile = new File(this.tmpDir.getDirectory().getAbsolutePath() + "/+logger_1.log");
        FileChannelDataLogger logger = new FileChannelDataLogger(loggerFile, AccessMode.APPEND);

        try {
            logger.write((short) 1, DATA1);
            logger.close();

            logger.reopen(AccessMode.WRITE);

            ILogRecordReplayListener replayListener = Mockito.mock(ILogRecordReplayListener.class);
            logger.replay(replayListener);

            Mockito.verify(replayListener, Mockito.times(0)).onRecord(Mockito.<XALogRecordType>any(), Mockito.<byte[][]>any());
        } finally {
            if (logger != null) {
                logger.destroy();
            }
        }
    }

    @Test
    public void testReopenWithAppend() throws IOException {
        byte[][] DATA1 = new byte[][]{"abcde".getBytes("UTF-8")};

        File loggerFile = new File(this.tmpDir.getDirectory().getAbsolutePath() + "/+logger_1.log");
        FileChannelDataLogger logger = new FileChannelDataLogger(loggerFile, AccessMode.APPEND);

        try {
            logger.write((short) 1, DATA1);
            logger.close();

            logger.reopen(AccessMode.APPEND);

            ILogRecordReplayListener replayListener = Mockito.mock(ILogRecordReplayListener.class);
            logger.replay(replayListener);

            Mockito.verify(replayListener, Mockito.times(1)).onRecord(Mockito.<XALogRecordType>any(), Mockito.<byte[][]>any());
        } finally {
            if (logger != null) {
                // destroy logger to release the file lock
                logger.destroy();
            }
        }
    }


    @Test
    public void testChannel() throws Exception {

        File loggerFile = new File(this.tmpDir.getDirectory().getAbsolutePath() + "/+logger_1.log");
        FileChannelDataLogger logger = new FileChannelDataLogger(loggerFile, AccessMode.APPEND);

        Assert.assertTrue(logger.getAccessMode() == AccessMode.APPEND);

        logger.write((short) 1, new byte[][]{"abcde".getBytes("UTF-8")});
        logger.write((short) 2, new byte[][]{"abcde".getBytes("UTF-8"), "abcdef".getBytes("UTF-8")});

        ILogRecordReplayListener replay = new ILogRecordReplayListener() {

            private int counter = 0;

            public void onRecord(XALogRecordType type, byte[][] data) {
                String message1 = null;
                String message2 = null;
                switch (counter) {
                    case 0:
                        TestCase.assertEquals(1, type.getType());
                        TestCase.assertEquals(1, data.length);
                        message1 = new String(data[0]);
                        // System.out.println("Data "+ counter+" = <" +data+">");
                        counter++;
                        TestCase.assertEquals("abcde", message1);
                        break;

                    case 1:
                        TestCase.assertEquals(2, type.getType());
                        TestCase.assertEquals(2, data.length);

                        message1 = new String(data[0]);
                        message2 = new String(data[1]);
                        // System.out.println("Data "+ counter+" = <" +data+">");
                        counter++;
                        TestCase.assertEquals("abcde", message1);
                        TestCase.assertEquals("abcdef", message2);
                        break;
                    default:
                        break;
                }
            }
        };

        logger.replay(replay);

        logger.close();

    }

}
