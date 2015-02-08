package org.csc.phynixx.loggersystem.logger;

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


import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.channellogger.AccessMode;

import java.io.IOException;
import java.util.*;

/**
 * the current class is responsible for instanciating new {@link org.csc.phynixx.loggersystem.logger.IDataLogger} and managing their lifecycle. It use a {@link org.csc.phynixx.loggersystem.logger.IDataLoggerFactory} for creating new Logger.
 *
 *
 * the current class managed the lifecycle instances of IDataLoggers.
 * <p/>
 * The current implementation do not re-use closed Logger, but further implementation can implement kind pof Caching/Polling strategies
 *
 * @author christoph
 */
class DataLoggerRespository {

    private static final String GLOBAL_FORMAT_PATTERN = "({0}_[a-z,A-Z,0-9]*[^_])_([0-9]*[^\\.])\\.[\\w]*";

    private static final String LOGGER_FORMAT_PATTERN = "({0})_([0-9]*[^\\.])\\.[\\w]*";

    private IDataLoggerFactory loggerFactory = null;

    private Map<String, IDataLogger> openLoggers = new HashMap<String, IDataLogger>();

    private long idGenerator = System.currentTimeMillis();

  
    private static final IPhynixxLogger LOGGER = PhynixxLogManager.getLogger(DataLoggerRespository.class);

    private String loggerSystemName = null;

    public DataLoggerRespository(String loggerSystemName, IDataLoggerFactory loggerFactory) {
        super();
        this.loggerSystemName = loggerSystemName;
        this.loggerFactory = loggerFactory;
    }

    public String getLoggerSystemName() {
        return this.loggerSystemName;
    }


    public IDataLogger instanciateLogger(String loggerName, boolean open) throws IOException, InterruptedException {

        // is logger reopen
        if (this.openLoggers.containsKey(loggerName)) {
            return openLoggers.get(loggerName);
        }

        IDataLogger logger = this.loggerFactory.instanciateLogger(loggerName);

        if (open) {
            logger.reopen(AccessMode.WRITE);
        }
        this.openLoggers.put(loggerName, logger);

        return logger;
    }


    /**
     * cloes a logger. This logger can be re-reopen
     *
     * @param loggerName
     */
    public void closeLogger(String loggerName) {
        if (this.openLoggers.containsKey(loggerName)) {
            try {
                this.openLoggers.get(loggerName).close();
            } catch (Exception e) {
                throw new DelegatedRuntimeException(e);
            }
            this.openLoggers.remove(loggerName);
        }
    }

    /**
     * destroys the logge an all its resources. the logger cannot be reopen and all logged information are vanished
     *
     * @param loggerName
     */
    public void destroyLogger(String loggerName) {
    }

    public synchronized void destroy(String loggerName) {
        if (this.openLoggers.containsKey(loggerName)) {
            try {
                IDataLogger dataLogger = this.openLoggers.get(loggerName);
                dataLogger.close();
            } catch (Exception e) {
                throw new DelegatedRuntimeException(e);
            }
            this.openLoggers.remove(loggerName);
        }
        this.loggerFactory.destroyLogger(loggerName);
    }


    /**
     * recovers all {@link org.csc.phynixx.loggersystem.logger.IDataLogger} having LOGGER files
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public synchronized Set<IDataLogger> recover() throws Exception {
        Set<IDataLogger> dataLoggers = new HashSet<IDataLogger>();

        Set<String> loggerNames = this.loggerFactory.findLoggerNames();
        for (String loggerName : loggerNames) {
            this.instanciateLogger(loggerName, true);
        }
        return dataLoggers;
    }

    /**
     * closes all reopen loggers
     */
    public synchronized void close() {
        Map<String, IDataLogger> copiedLoggers = new HashMap(this.openLoggers);

        for (IDataLogger dataLogger : copiedLoggers.values()) {
            try {
                dataLogger.close();
            } catch (Exception e) { }
        }
        this.openLoggers.clear();

    }

}
