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


import java.io.File;

class LogFileCollector {

    public interface ICollectorCallback {
        void match(File file, LogFilenameMatcher.LogFilenameParts parts);
    }

    private LogFilenameMatcher logFileMatcher = null;


    public LogFileCollector(LogFilenameMatcher logFileMatcher, File startDirectory, ICollectorCallback cb) {
        super();
        this.logFileMatcher = logFileMatcher;
        if (startDirectory.exists() && startDirectory.isDirectory()) {
            this.iterateDirectory(startDirectory, cb);
        }
    }


    private void iterateDirectory(File startDirectory, ICollectorCallback cb) {
        File[] files = startDirectory.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                iterateDirectory(files[i], cb);
            } else {
                LogFilenameMatcher.LogFilenameParts parts =
                        this.logFileMatcher.matches(files[i].getName());
                if (parts != null) {
                    cb.match(files[i], parts);
                }
            }
        }

    }


}
