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

        FileChannelLoggerFactory channelFactory =
                new FileChannelLoggerFactory("howl", tmpDirectory.getDirectory().getAbsolutePath());
        Set<String> loggerNames = channelFactory.findLoggerNames(GLOBAL_FORMAT_PATTERN);
        for (String loggerName : loggerNames) {
            System.out.println(loggerName);
        }
        Assert.assertEquals(5, loggerNames.size());
        Assert.assertTrue(loggerNames.contains("howl_a_1"));
        Assert.assertTrue(loggerNames.contains("howl_a_2"));
        Assert.assertTrue(loggerNames.contains("howl_a_3"));
        Assert.assertTrue(loggerNames.contains("howl_c_12345"));
        Assert.assertTrue(loggerNames.contains("howl_b_12345"));

        // Muster ist nicht zulaessig
        Assert.assertFalse(loggerNames.contains("howl_a_1_k"));
    }

    @Test
    public void testDestroy() throws Exception {
        String logsystem = "howl";

        String[] filenames = {
                "howl_a_1.log",
                "howl_a_2.log",
                "howl_a_3.log",
                "howl_b_12345.log"};
        TmpDirectory tmpDirectory = new TmpDirectory();

        for (int i = 0; i < filenames.length; i++) {
            tmpDirectory.assertExitsFile(filenames[i]);
        }

        FileChannelLoggerFactory channelFactory =
                new FileChannelLoggerFactory("howl", tmpDirectory.getDirectory().getAbsolutePath());

        channelFactory.cleanup(FORMAT_PATTERN);
        Set<String> loggerNames = channelFactory.findLoggerNames(GLOBAL_FORMAT_PATTERN);
        for (String loggerName : loggerNames) {
            System.out.println(loggerName);
        }
        Assert.assertEquals(2, loggerNames.size());

        // howl_a* ist geloescht
        Assert.assertFalse(loggerNames.contains("howl_a_1"));
        Assert.assertFalse(loggerNames.contains("howl_a_2"));
        Assert.assertFalse(loggerNames.contains("howl_a_3"));

        // restlichen bleiben bestehen
        Assert.assertTrue(loggerNames.contains("howl_c_12345"));
        Assert.assertTrue(loggerNames.contains("howl_b_12345"));
    }

    @Test
    public void testConcurrentWrite() throws Exception {
        TmpDirectory tmpDirectory = new TmpDirectory();

        FileChannelLoggerFactory channelFactory =
                new FileChannelLoggerFactory("howl", tmpDirectory.getDirectory().getAbsolutePath());

        IDataLogger logger1 = channelFactory.instanciateLogger("a");
        IDataLogger logger2 = channelFactory.instanciateLogger("a");

    }

}
