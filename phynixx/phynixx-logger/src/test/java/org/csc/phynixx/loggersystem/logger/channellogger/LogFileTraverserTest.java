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


import org.apache.commons.io.FilenameUtils;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.channellogger.LogFileTraverser.ICollectorCallback;
import org.csc.phynixx.loggersystem.logger.channellogger.LogFilenameMatcher.LogFilenameParts;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


@RunWith(JUnit4.class)
public class LogFileTraverserTest {

    private static final String FORMAT_PATTERN = "(howl)_([a-z,A-Z,0-9]*[^_])_([0-9]*[^\\.])\\.[\\w]*";

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    public static class LoggerInfo {
        private File file = null;
        private String loggerName = null;

        public LoggerInfo(String loggerName, File file) {
            super();
            this.loggerName = loggerName;
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public String getLoggerName() {
            return loggerName;
        }

        public String toString() {
            return "LoggerInfo Logger=" + loggerName + "_<index>.log (parent directory=" + this.file + ")";
        }
    }

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


    /**
     *
     */
    @Test
    public void testMatches() {

        LogFilenameMatcher recovery = new LogFilenameMatcher(FORMAT_PATTERN);

        LogFilenameMatcher.LogFilenameParts result = recovery.matches("howl_xyz_1.log");
        log.info(result.getLoggerName() + "[" + result.getLogfileIndex() + "]");

    }


    @Test
    public void testLoggerInfoCollector() throws Exception {
        String logsystem = "howl";

        String[] filenames = {
                "howl_a_1.log",
                "howl_a_2.log",
                "howl_a_3.log",
                "howl_b_12345.log",
                "subdir/howl_c_12345.log",
                "xyz",
                "howl_123_1_k.log"};
        TmpDirectory tmpDirectory = new TmpDirectory();

        for (int i = 0; i < filenames.length; i++) {
            tmpDirectory.assertExitsFile(filenames[i]);
        }

        final Map<String, LoggerInfo> loggerInfos = new HashMap<String, LoggerInfo>();
        ICollectorCallback cb = new ICollectorCallback() {
            public void match(File file, LogFilenameParts parts) {
                if (!loggerInfos.containsKey(parts.getLoggerName())) {
                    loggerInfos.put(parts.getLoggerName(),
                            new LoggerInfo(parts.getLoggerName(), file)
                    );
                }
            }
        };

        // Recover the loggers ....
        LogFilenameMatcher matcher = new LogFilenameMatcher(FORMAT_PATTERN);
        LogFileTraverser collector = new LogFileTraverser(matcher, tmpDirectory.getDirectory(), cb);

        for (Iterator<Map.Entry<String, LoggerInfo>> iterator = loggerInfos.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, LoggerInfo> entry = iterator.next();
            log.info("Key=" + entry.getKey() + " value=" + entry.getValue());
        }

        Assert.assertEquals(3, loggerInfos.size());

        Assert.assertTrue(loggerInfos.get("a") != null);

        Assert.assertEquals("a", ((LoggerInfo) loggerInfos.get("a")).getLoggerName());

        Assert.assertEquals(tmpDirectory.getDirectory(),
                loggerInfos.get("a").getFile().getParentFile());

        Assert.assertTrue(loggerInfos.get("b") != null);

        Assert.assertEquals(tmpDirectory.getDirectory(),
                loggerInfos.get("b").getFile().getParentFile());

        Assert.assertEquals("howl_b_12345.log",
                FilenameUtils.getName(loggerInfos.get("b").getFile().getCanonicalPath()));

        Assert.assertTrue(loggerInfos.get("c") != null);

        Assert.assertEquals(tmpDirectory.assertExitsDirectory("subdir"),
                loggerInfos.get("c").getFile().getParentFile());

        FileChannelDataLoggerFactory channelFactory =
                new FileChannelDataLoggerFactory("howl", tmpDirectory.getDirectory().getAbsolutePath());
        Set<String> loggerNames = channelFactory.findLoggerNames();
        for (String loggerName : loggerNames) {
            System.out.println(loggerName);
        }
        Assert.assertEquals(3, loggerNames.size());
        Assert.assertTrue(loggerNames.contains("a"));
        Assert.assertTrue(loggerNames.contains("c"));
        Assert.assertTrue(loggerNames.contains("b"));

    }

}
