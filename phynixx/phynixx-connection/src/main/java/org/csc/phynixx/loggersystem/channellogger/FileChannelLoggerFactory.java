package org.csc.phynixx.loggersystem.channellogger;

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


import org.csc.phynixx.loggersystem.ILogger;
import org.csc.phynixx.loggersystem.ILoggerFactory;

import java.io.File;
import java.io.IOException;

public class FileChannelLoggerFactory implements ILoggerFactory {

    private File directory = null;
    private String loggerSystemName = null;

    public FileChannelLoggerFactory(String loggerSystemName, String directoryName) {
        super();
        this.loggerSystemName = loggerSystemName;
        this.directory = new File(directoryName);
        if (this.directory.exists() && !this.directory.isDirectory()) {
            throw new IllegalArgumentException("Argument 'directoryname' has to referece an existing directory");
        }
    }

    public FileChannelLoggerFactory(String loggerSystemName, File directory) {
        super();
        this.loggerSystemName = loggerSystemName;
        this.directory = directory;
        if (this.directory.exists() && !this.directory.isDirectory()) {
            throw new IllegalArgumentException("Argument 'directory' has to be an existing directory");
        }
    }

    public File getLoggingDirectory() {
        return directory;
    }

    public ILogger instanciateLogger(String loggerName) throws IOException {
        return new FileChannelLogger(loggerName, this.directory);
    }

}
