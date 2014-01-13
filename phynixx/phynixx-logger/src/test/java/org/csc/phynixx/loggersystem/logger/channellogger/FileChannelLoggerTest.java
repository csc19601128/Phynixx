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
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class FileChannelLoggerTest {


    private TmpDirectory tmpDir = null;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();
        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("channel");
        this.tmpDir.clear();

        this.tmpDir = new TmpDirectory("channel");

    }

    @After
    public void tearDown() throws Exception {
        // delete all tmp files ...
        this.tmpDir.clear();
    }


    @Test
    public void testChannel() throws Exception {

        FileChannelDataLoggerFactory loggerFactory =
                new FileChannelDataLoggerFactory("test", this.tmpDir.getDirectory().getAbsolutePath());

        File loggerFile = new File(this.tmpDir.getDirectory().getAbsolutePath() + "/+logger_1.log");
        FileChannelDataLogger logger = new FileChannelDataLogger(loggerFile);

        logger.open(AccessMode.WRITE);
        byte[] data1 = "abcde".getBytes();
        byte[] data2 = "abcdef".getBytes();
        logger.write((short) 1, new byte[][]{data1});
        logger.write((short) 2, new byte[][]{data1, data2});

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
