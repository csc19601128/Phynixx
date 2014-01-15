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


import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * Diese Factory erzeugt FileChannelDataLogger .
 * Es wird ein logischer Name mitgegeben und es wird im Verzeichnis eine datei mit diesem namen angelegt und auf dieser Datei eine TAEnabledRandomAccessFile instanziert.
 * <p/>
 * The logic Name is unique concerning the loggerssytem
 */
public class FileChannelDataLoggerFactory implements IDataLoggerFactory {

    private static final String LOGGERSYSTEM_FORMAT_PATTERN = "({0})_([a-z,A-Z,0-9]*[^_])_([0-9]*[^\\.])\\.[\\w]*";

    private static final String LOGGER_FORMAT_PATTERN = "({0})_([0-9]*[^\\.])\\.[\\w]*";

    private static final IPhynixxLogger LOGGER = PhynixxLogManager.getLogger(FileChannelDataLoggerFactory.class);

    private File directory = null;
    private String loggerSystemName = null;

    public FileChannelDataLoggerFactory(String loggerSystemName, String directoryName) {
        super();
        this.loggerSystemName = loggerSystemName;
        this.directory = new File(directoryName);
        if (this.directory.exists() && !this.directory.isDirectory()) {
            throw new IllegalArgumentException("Directory " + directory.getAbsolutePath() + " doesn't exist or is not a directory");
        }
        if (!this.directory.canExecute() && !this.directory.canWrite()) {
            throw new IllegalArgumentException("Directory " + directory.getAbsolutePath() + " could not be written");
        }
    }

    public FileChannelDataLoggerFactory(String loggerSystemName, File directory) {
        super();
        this.loggerSystemName = loggerSystemName;
        this.directory = directory;
        if (this.directory.exists() && !this.directory.isDirectory()) {
            throw new IllegalArgumentException("Argument 'directory' has to be an existing directory");
        }
    }

    public String getLoggerSystemName() {
        return loggerSystemName;
    }


    public File getLoggingDirectory() {
        return directory;
    }


    private String createQualifiedLoggerName(String loggerName, int qualifier) {
        return new StringBuilder(this.loggerSystemName).append("_").append(loggerName).append("_").append(qualifier).toString();
    }


    /**
     * @param loggerName unique Identifier of the logger (concering the logger system)
     * @return
     * @throws IOException
     */
    public IDataLogger instanciateLogger(String loggerName) throws IOException {


        File logFile = this.provideFile(createQualifiedLoggerName(loggerName, 1), this.directory);
        return new FileChannelDataLogger(logFile);
    }

    @Override
    public void cleanup() {
        String pattern = MessageFormat.format(LOGGERSYSTEM_FORMAT_PATTERN, this.loggerSystemName);
        LogFilenameMatcher matcher = new LogFilenameMatcher(pattern);

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
                FileChannelDataLoggerFactory.this.getLoggingDirectory(), cb);
    }

    @Override
    public void destroyLogger(String loggerName) {
        String pattern = MessageFormat.format(LOGGER_FORMAT_PATTERN, loggerName);
        LogFilenameMatcher matcher = new LogFilenameMatcher(pattern);

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
                FileChannelDataLoggerFactory.this.getLoggingDirectory(), cb);
    }

    @Override
    public Set<String> findLoggerNames() throws IOException {

        String pattern = MessageFormat.format(LOGGERSYSTEM_FORMAT_PATTERN, this.loggerSystemName);
        LogFilenameMatcher matcher = new LogFilenameMatcher(pattern);

        final Set<String> loggerNames = new HashSet<String>();
        LogFileCollector.ICollectorCallback cb = new LogFileCollector.ICollectorCallback() {
            public void match(File file, LogFilenameMatcher.LogFilenameParts parts) {
                loggerNames.add(parts.getLoggerName());
            }
        };
        LogFileCollector logfileCollector = new LogFileCollector(matcher, this.getLoggingDirectory(), cb);

        return loggerNames;
    }

    private File provideFile(String fileName, File directory) throws IOException {

        String fileCompleteName = directory + File.separator + fileName + "_1.log";

        File file = new File(directory, fileName + ".log");

        // Falls existent, so ist nichts zu tun.
        if (file.exists()) {
            return file;
        }

        if (!directory.exists()) {
            directory.mkdirs();
        }

        file.createNewFile();

        return file;

    }


}
