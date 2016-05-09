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


import org.csc.phynixx.loggersystem.logger.channellogger.AccessMode;

import java.io.IOException;
import java.util.Set;


/**
 *
 */
public interface IDataLoggerFactory {

    String getLoggerSystemName();

    /**
     * open the logger with mode {@link org.csc.phynixx.loggersystem.logger.channellogger.AccessMode#APPEND}
     * @param loggerName unique Identifier of the logger (concering the logger system)
     * @return logger
     * @throws IOException
     */
    IDataLogger instanciateLogger(String loggerName) throws IOException;


    /**
     * @param loggerName unique Identifier of the logger (concering the logger system)
     * @return logger
     * @throws IOException
     */
    IDataLogger instanciateLogger(String loggerName, AccessMode accessMode) throws IOException;

    /**
     * Destroys all Logger of the logerSystem
     */
    void cleanup();

    /**
     * destroys the logger an all is associated resources an information
     */
    void cleanupLoggers(String loggerName);

    /**
     * @return the loggernames of all loggers of the logger system
     */
    Set<String> findLoggerNames() throws IOException;
}
