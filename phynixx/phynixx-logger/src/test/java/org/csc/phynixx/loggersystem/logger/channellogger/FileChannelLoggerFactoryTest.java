package org.csc.phynixx.loggersystem.logger.channellogger;

/*
 * #%L
 * phynixx-logger
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
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


import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * Created by zf4iks2 on 08.01.14.
 */
public class FileChannelLoggerFactoryTest {

    private static final String GLOBAL_FORMAT_PATTERN = "(howl_[a-z,A-Z,0-9]*[^_])_([0-9]*[^\\.])\\.[\\w]*";
    private static final String FORMAT_PATTERN = "(howl_a)_([0-9]*[^\\.])\\.[\\w]*";

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

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
    public void testLoggerInfoCollector() throws Exception {
        String logsystem = "howl";

        String[] filenames = {
                "howl_a_1.log",
                "howl_a_2.log",
                "howl_a_3.log",
                "howl_b_12345.log",
                "howl_a_1_k.log"
        };
        TmpDirectory tmpDirectory = new TmpDirectory();

        for (int i = 0; i < filenames.length; i++) {
            tmpDirectory.assertExitsFile(filenames[i]);
        }

        FileChannelDataLoggerFactory channelFactory =
                new FileChannelDataLoggerFactory("howl", tmpDirectory.getDirectory().getAbsolutePath());
        Set<String> loggerNames = channelFactory.findLoggerNames();
        for (String loggerName : loggerNames) {
            System.out.println(loggerName);
        }
        Assert.assertEquals(3, loggerNames.size());
        Assert.assertTrue(loggerNames.contains("howl_a"));
        Assert.assertTrue(loggerNames.contains("howl_b"));
        Assert.assertTrue(loggerNames.contains("howl_c"));

        // Muster ist nicht zulaessig
        Assert.assertFalse(loggerNames.contains("howl_a_1_k"));
    }

    @Test
    public void testDestroy() throws Exception {
        String logsystem = "howl";

        String[] filenames = {
                "test_a_1.log",
                "test_a_2.log",
                "test_b_3.log",
                "test2_b_12345.log"};
        TmpDirectory tmpDirectory = new TmpDirectory();

        for (int i = 0; i < filenames.length; i++) {
            tmpDirectory.assertExitsFile(filenames[i]);
        }

        FileChannelDataLoggerFactory channelFactory1 =
                new FileChannelDataLoggerFactory("test", tmpDirectory.getDirectory().getAbsolutePath());

        // logger test_a mit 2 logFiles und test_b mit einem
        Assert.assertEquals(2, channelFactory1.findLoggerNames().size());

        channelFactory1.cleanup();
        Assert.assertEquals(0, channelFactory1.findLoggerNames().size());

        FileChannelDataLoggerFactory channelFactory2 =
                new FileChannelDataLoggerFactory("test2", tmpDirectory.getDirectory().getAbsolutePath());

        Set<String> loggerNames = channelFactory2.findLoggerNames();
        Assert.assertEquals(1, channelFactory2.findLoggerNames().size());

        Assert.assertTrue(loggerNames.contains("test2_b"));
    }

    @Test
    public void testConcurrentWrite() throws Exception {
        TmpDirectory tmpDirectory = new TmpDirectory();

        FileChannelDataLoggerFactory channelFactory =
                new FileChannelDataLoggerFactory("howl", tmpDirectory.getDirectory().getAbsolutePath());

        IDataLogger logger1 = channelFactory.instanciateLogger("a");
        IDataLogger logger2 = channelFactory.instanciateLogger("a");

    }

}
