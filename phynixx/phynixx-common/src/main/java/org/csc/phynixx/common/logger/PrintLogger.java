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


import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class PrintLogger implements IPhynixxLogger {

    public static final Integer DEBUG = new Integer(5);
    public static final Integer INFO = new Integer(4);
    public static final Integer WARN = new Integer(3);
    public static final Integer ERROR = new Integer(2);
    public static final Integer FATAL = new Integer(1);

    private static Map VALID_LOG_LEVELS = new HashMap();

    static {
        VALID_LOG_LEVELS.put(DEBUG, "DEBUG");
        VALID_LOG_LEVELS.put(INFO, "INFO");
        VALID_LOG_LEVELS.put(WARN, "WARN");
        VALID_LOG_LEVELS.put(ERROR, "ERROR");
        VALID_LOG_LEVELS.put(FATAL, "FATAL");
    }


    private PrintStream logStream = System.out;
    private Integer logLevel = ERROR;


    public Integer getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Integer logLevel) {
        if (!VALID_LOG_LEVELS.containsKey(logLevel)) {
            throw new IllegalArgumentException("Invalid Loglevel " + logLevel);
        }
        this.logLevel = logLevel;
    }

    public PrintStream getLogStream() {
        return logStream;
    }

    public void setLogStream(PrintStream logStream) {
        this.logStream = logStream;
    }

    public void debug(Object o) {
        if (this.isDebugEnabled()) {
            this.getLogStream().println(o);
        }
    }

    public void debug(Object o, Throwable t) {

        if (this.isDebugEnabled()) {
            this.getLogStream().println(o + "Exception :: " + t.getMessage());
            t.printStackTrace(this.getLogStream());
        }
    }

    public void error(Object o) {
        this.getLogStream().println(o);
    }

    public void error(Object o, Throwable t) {

        this.getLogStream().println(o + "Exception :: " + t.getMessage());
        t.printStackTrace(this.getLogStream());
    }

    public void info(Object o) {
        if (this.isInfoEnabled()) {
            this.getLogStream().println(o);
        }
    }

    public void info(Object o, Throwable t) {

        if (this.isInfoEnabled()) {
            this.getLogStream().println(o + "Exception :: " + t.getMessage());
            t.printStackTrace(this.getLogStream());
        }
    }

    public void fatal(Object o) {
        this.error(o);
    }

    public void fatal(Object o, Throwable t) {
        this.error(o, t);
    }


    public boolean isDebugEnabled() {
        return this.logLevel.compareTo(DEBUG) < 0;
    }

    public boolean isInfoEnabled() {
        return this.logLevel.compareTo(INFO) < 0;
    }

    public void warn(Object o) {
        this.error(o);

    }

    public void warn(Object o, Throwable t) {
        this.error(o, t);
    }

}
