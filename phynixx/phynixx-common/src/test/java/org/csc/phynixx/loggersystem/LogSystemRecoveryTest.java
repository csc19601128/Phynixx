package org.csc.phynixx.loggersystem;

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
import org.apache.commons.io.FilenameUtils;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.LogFileCollector.ICollectorCallback;
import org.csc.phynixx.loggersystem.LogFilenameMatcher.LogFilenameParts;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class LogSystemRecoveryTest extends TestCase {

    private static final String FORMAT_PATTREN = "(howl_[a-z,A-Z,0-9]*[^_])_([0-9]*[^\\.])\\.[\\w]*";

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

    protected void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        new TmpDirectory().clear();

    }

    protected void tearDown() throws Exception {
        // delete all tmp files ...
        new TmpDirectory().clear();
    }


    public void testMatches() {

        LogFilenameMatcher recovery = new LogFilenameMatcher(FORMAT_PATTREN);

        LogFilenameMatcher.LogFilenameParts result = recovery.matches("howl_xyz_1.log");
        log.info(result.getLoggerName() + "[" + result.getLogfileIndex() + "]");

    }


    public void testLoggerInfoCollector() throws Exception {
        String logsystem = "howl";

        String[] filenames = {"howl_a_1.log", "howl_a_2.log", "howl_a_3.log",
                "howl_b_12345.log",
                "subdir/howl_c_12345.log",
                "xyz",
                "howl_123_1_k.log"};
        TmpDirectory tmpDirectory = new TmpDirectory();

        for (int i = 0; i < filenames.length; i++) {
            tmpDirectory.assertExitsFile(filenames[i]);
        }


        final Map loggerInfos = new HashMap();
        ICollectorCallback cb = new ICollectorCallback() {
            public void match(File file, LogFilenameParts parts) {
                if (!loggerInfos.containsKey(parts.getLoggerName())) {
                    loggerInfos.put(parts.getLoggerName(),
                            new LoggerInfo(parts.getLoggerName(),
                                    file)
                    );
                }
            }
        };

        // Recover the loggers ....
        LogFilenameMatcher matcher = new LogFilenameMatcher(FORMAT_PATTREN);
        LogFileCollector collector = new LogFileCollector(matcher, tmpDirectory.getDirectory(), cb);
        for (Iterator iterator = loggerInfos.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iterator.next();
            log.info("Key=" + entry.getKey() + " value=" + entry.getValue());

        }
        TestCase.assertEquals(3, loggerInfos.size());
        TestCase.assertTrue(loggerInfos.get("howl_a") != null);
        TestCase.assertEquals("howl_a", ((LoggerInfo) loggerInfos.get("howl_a")).getLoggerName());
        TestCase.assertEquals(tmpDirectory.getDirectory(),
                ((LoggerInfo) loggerInfos.get("howl_a")).getFile().getParentFile());
        TestCase.assertTrue(loggerInfos.get("howl_b") != null);
        ;
        TestCase.assertEquals(tmpDirectory.getDirectory(),
                ((LoggerInfo) loggerInfos.get("howl_b")).getFile().getParentFile());

        TestCase.assertEquals("howl_b_12345.log",
                FilenameUtils.getName(((LoggerInfo) loggerInfos.get("howl_b")).getFile().getCanonicalPath()));
        TestCase.assertTrue(loggerInfos.get("howl_c") != null);
        TestCase.assertEquals(tmpDirectory.assertExitsDirectory("subdir"),
                ((LoggerInfo) loggerInfos.get("howl_c")).getFile().getParentFile());

    }

}
