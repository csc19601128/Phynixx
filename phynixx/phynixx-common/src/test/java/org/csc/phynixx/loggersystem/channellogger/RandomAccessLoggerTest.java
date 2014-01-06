package org.csc.phynixx.loggersystem.channellogger;

/*
 * #%L
 * phynixx-common
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
import org.csc.phynixx.loggersystem.ILogRecordReplayListener;

public class RandomAccessLoggerTest extends TestCase {


    private TmpDirectory tmpDir = null;

    protected void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();
        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("channel");
        this.tmpDir.clear();

        this.tmpDir = new TmpDirectory("channel");

    }

    protected void tearDown() throws Exception {
        // delete all tmp files ...
        this.tmpDir.clear();
    }


    public void testChannel() throws Exception {

        FileChannelLoggerFactory loggerFactory =
                new FileChannelLoggerFactory("test", this.tmpDir.getDirectory().getAbsolutePath());
        FileChannelLogger logger =
                (FileChannelLogger) loggerFactory.instanciateLogger("logger_1234456");

        logger.open();
        byte[] data1 = "abcde".getBytes();
        byte[] data2 = "abcdef".getBytes();
        logger.write((short) 1, new byte[][]{data1});
        logger.write((short) 2, new byte[][]{data1, data2});

        ILogRecordReplayListener replay = new ILogRecordReplayListener() {

            private int counter = 0;

            public void onRecord(short type, byte[][] data) {
                String message1 = null;
                String message2 = null;
                switch (counter) {
                    case 0:
                        TestCase.assertEquals(1, type);
                        TestCase.assertEquals(1, data.length);
                        message1 = new String(data[0]);
                        // System.out.println("Data "+ counter+" = <" +data+">");
                        counter++;
                        TestCase.assertEquals("abcde", message1);
                        break;

                    case 1:
                        TestCase.assertEquals(2, type);
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
