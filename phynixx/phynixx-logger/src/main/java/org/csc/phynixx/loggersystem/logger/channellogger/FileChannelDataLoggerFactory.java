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


import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * create DataLogger using Files to store data.
 * A logger is qualified by its name and a subsequent integer qualifier.
 * It is possible to have more than one log file for a given logger.
 * The logfile differ in the qualifier.
 * <p/>
 * <pre>
 *  A log file is named according to the follwing pattern
 *  'loggerSystemName'_'loggerName'_'qualifier'.log
 *
 * The different parts of the name make i possible to deduce the following information from the logfile name
 * 1.) loggerSystem
 * 2.) loggername
 * 3.) qualifier pof the logfile for the logger
 * </pre>
 * <p/>
 * A logical logger name has to unique for all logfile of the current loggerfactory.
 * Diese Factory erzeugt FileChannelDataLogger .
 * Es wird ein logischer Name mitgegeben und es wird im Verzeichnis eine datei mit diesem namen angelegt und auf dieser Datei eine TAEnabledRandomAccessFile instanziert.
 * <p/>
 * The logic Name is unique concerning the loggerssytem
 */
public class FileChannelDataLoggerFactory implements IDataLoggerFactory {

    private static final String LOGGER_SYSTEM_FORMAT_PATTERN = "({0})_([a-z,A-Z,0-9]*[^_])_([0-9]*[^\\.])\\.[\\w]*";

    private static final String LOGGER_FORMAT_PATTERN = "({0})_([0-9]*[^\\.])\\.[\\w]*";

    private static final IPhynixxLogger LOGGER = PhynixxLogManager.getLogger(FileChannelDataLoggerFactory.class);

    private File directory = null;
    private String loggerSystemName = null;


    /**
     * @param loggerSystemName
     * @param directoryName    logfile are created in this directory
     */
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

    /**
     * @param loggerSystemName
     * @param directory        logfile are created in this directory
     */
    public FileChannelDataLoggerFactory(String loggerSystemName, File directory) {
        super();
        this.loggerSystemName = loggerSystemName;
        this.directory = directory;

        if (directory == null) {
            throw new IllegalArgumentException("Log directory must be specified");
        }


        if (!directory.exists()) {
            throw new IllegalArgumentException("Log directory must exists");
        }

        if (this.directory.exists() && !this.directory.isDirectory()) {
            throw new IllegalArgumentException("Argument 'directory' has to be an existing directory");
        }
    }

    /**
     * @return Name of the loggerSystem
     */
    public String getLoggerSystemName() {
        return loggerSystemName;
    }


    /**
     * @return directory containing the logfiles
     */
    public File getLoggingDirectory() {
        return directory;
    }


    private String createQualifiedLoggerName(String loggerName, int qualifier) {
        return new StringBuilder(this.loggerSystemName).append("_").append(loggerName).append("_").append(qualifier).toString();
    }


    /**
     * @param loggerName unique Identifier of the logger (concering to the logger system)
     * @return dataLogger encapsulating the logfile
     * @throws IOException
     */
    public synchronized IDataLogger instanciateLogger(String loggerName) throws IOException {
        return this.instanciateLogger(loggerName, AccessMode.APPEND);
    }

    /**
     * @param loggerName unique Identifier of the logger (concering to the logger system)
     * @return dataLogger encapsulating the logfile
     * @throws IOException
     */
    public synchronized IDataLogger instanciateLogger(String loggerName, AccessMode accessMode) throws IOException {

        File logFile = this.provideFile(createQualifiedLoggerName(loggerName, 1), this.directory);
        return new FileChannelDataLogger(logFile, accessMode);
    }

    /**
     * destroys a logfiles
     */
    @Override
    public synchronized void cleanup() {
        String pattern = MessageFormat.format(LOGGER_SYSTEM_FORMAT_PATTERN, this.loggerSystemName);
        LogFilenameMatcher matcher = new LogFilenameMatcher(pattern);

        LogFileTraverser.ICollectorCallback cb = new LogFileTraverser.ICollectorCallback() {
            public void match(File file,
                              LogFilenameMatcher.LogFilenameParts parts) {
                boolean success = file.delete();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Deleting " + file + " success=" + success);
                }
            }
        };
        new LogFileTraverser(matcher,
                FileChannelDataLoggerFactory.this.getLoggingDirectory(), cb);
    }

    @Override
    /**
     * destroyes all logfile of the logger
     */
    public synchronized void cleanupLoggers(String loggerName) {
        String pattern = MessageFormat.format(LOGGER_FORMAT_PATTERN, loggerName);
        LogFilenameMatcher matcher = new LogFilenameMatcher(pattern);

        LogFileTraverser.ICollectorCallback cb = new LogFileTraverser.ICollectorCallback() {
            public void match(File file,
                              LogFilenameMatcher.LogFilenameParts parts) {
                boolean success = file.delete();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Deleting " + file + " success=" + success);
                }
            }
        };
         new LogFileTraverser(matcher,
                FileChannelDataLoggerFactory.this.getLoggingDirectory(), cb);
    }

    @Override
    /**
     * @return logger having at least one logfile accociated
     */
    public synchronized Set<String> findLoggerNames() throws IOException {

        String pattern = MessageFormat.format(LOGGER_SYSTEM_FORMAT_PATTERN, this.loggerSystemName);
        LogFilenameMatcher matcher = new LogFilenameMatcher(pattern);

        final Set<String> loggerNames = new HashSet<String>();
        LogFileTraverser.ICollectorCallback cb = new LogFileTraverser.ICollectorCallback() {
            public void match(File file, LogFilenameMatcher.LogFilenameParts parts) {
                loggerNames.add(parts.getLoggerName());
            }
        };
        new LogFileTraverser(matcher, this.getLoggingDirectory(), cb);

        return loggerNames;
    }

    private File provideFile(String fileName, File directory) throws IOException {


        File file = new File(directory, fileName + ".log");

        // Falls existent, so ist nichts zu tun.
        if (file.exists()) {
            return file;
        }

        if (!directory.exists()) {
            final boolean mkdirs = directory.mkdirs();
            if(!mkdirs) {
                throw new IOException("Failed to create directory " + directory + " or one of its children");
            }
        }

        if(!file.createNewFile()) {

            throw new IOException("Failed to create file "+file);
        }

        return file;

    }


}
