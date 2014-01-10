package org.csc.phynixx.loggersystem.logger.channellogger;

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


import org.apache.commons.io.FilenameUtils;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.ILogger;
import org.csc.phynixx.loggersystem.logger.ILoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Diese Factory erzeugt FileChannelLogger .
 * Es wird ein logischer Name mitgegeben und es wird im Verzeichnis eine datei mit diesem namen angelegt und auf dieser Datei eine TAEnabledRandomAccessFile instanziert.
 */
public class FileChannelLoggerFactory implements ILoggerFactory {


    private static final IPhynixxLogger LOGGER = PhynixxLogManager.getLogger(FileChannelLoggerFactory.class);

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

    /**
     * @param loggerName unique Identifier of the logger (concering the logger system)
     * @return
     * @throws IOException
     */
    public ILogger instanciateLogger(String loggerName) throws IOException {
        return new FileChannelLogger(loggerName, this.directory);
    }

    @Override
    public void cleanup(String loggerNamePattern) {
        LogFilenameMatcher matcher = new LogFilenameMatcher(loggerNamePattern);

        LogFileCollector.ICollectorCallback cb = new LogFileCollector.ICollectorCallback() {
            public void match(File file,
                              LogFilenameMatcher.LogFilenameParts parts) {
                boolean success = file.delete();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Deleting " + file + " success=" + success);
                }
            }
        };
        LogFileCollector logfileCollector = new LogFileCollector(matcher,
                FileChannelLoggerFactory.this.getLoggingDirectory(), cb);
    }

    @Override
    public Set<String> findLoggerNames(String loggerNamePattern) throws IOException {

        LogFilenameMatcher matcher = new LogFilenameMatcher(loggerNamePattern);

        final Set<String> loggerNames = new HashSet<String>();
        LogFileCollector.ICollectorCallback cb = new LogFileCollector.ICollectorCallback() {
            public void match(File file, LogFilenameMatcher.LogFilenameParts parts) {
                loggerNames.add(FilenameUtils.getBaseName(file.getName()));
            }
        };
        LogFileCollector logfileCollector = new LogFileCollector(matcher, this.getLoggingDirectory(), cb);

        return loggerNames;
    }


}
