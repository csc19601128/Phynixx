package org.csc.phynixx.common.logger;

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


public class PrintLogManager implements IPhynixxLogManager {

    private final PrintLogger printLogger = new PrintLogger();


    public PrintLogManager() {
        printLogger.setLogLevel(PrintLogger.ERROR);
    }


    public PrintLogManager(Integer logLevel) {
        printLogger.setLogLevel(logLevel);
    }

    public IPhynixxLogger getLogger(Class cls) {
        return printLogger;
    }

    public IPhynixxLogger getLogger(String logger) {
        return printLogger;
    }
}
