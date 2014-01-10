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


import java.io.IOException;
import java.util.Set;


/**
 *
 */
public interface ILoggerFactory {

    /**
     * @param loggerName unique Identifier of the logger (concering the logger system)
     * @return logger
     * @throws IOException
     */
    ILogger instanciateLogger(String loggerName) throws IOException;

    /**
     * Destroys all Logger matching the regex-pattern
     *
     * @param loggerNamePattern
     */
    void cleanup(String loggerNamePattern);

    /**
     * RegExpattern for die logger name
     *
     * @param loggerNamePattern
     * @return
     */
    Set<String> findLoggerNames(String loggerNamePattern) throws IOException;
}
