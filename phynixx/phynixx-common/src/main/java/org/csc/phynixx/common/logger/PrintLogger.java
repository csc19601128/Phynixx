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

    public static final Integer TRACE =Integer.valueOf(6);
    public static final Integer DEBUG =Integer.valueOf(5);
    public static final Integer INFO =Integer.valueOf(4);
    public static final Integer WARN =Integer.valueOf(3);
    public static final Integer ERROR =Integer.valueOf(2);
    public static final Integer FATAL =Integer.valueOf(1);

    private static final Map<Integer,String> VALID_LOG_LEVELS = new HashMap<Integer,String>();

    static {
    	  VALID_LOG_LEVELS.put(TRACE, "TRACE");  
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
            throw new IllegalArgumentException("Invalid log level " + logLevel);
        }
        this.logLevel = logLevel;
    }

    public PrintStream getLogStream() {
        return logStream;
    }

    public void setLogStream(PrintStream logStream) {
        this.logStream = logStream;
    }

    public void debug(String msg) {
        if (this.isDebugEnabled()) {
            this.getLogStream().println(msg);
        }
    }

    public void debug(String msg, Throwable t) {

        if (this.isDebugEnabled()) {
            this.getLogStream().println(msg + "Exception :: " + t.getMessage());
            t.printStackTrace(this.getLogStream());
        }
    }

    public void error(String msg) {
        this.getLogStream().println(msg);
    }

    public void error(String msg, Throwable t) {

        this.getLogStream().println(msg + "Exception :: " + t.getMessage());
        t.printStackTrace(this.getLogStream());
    }

    public void info(String msg) {
        if (this.isInfoEnabled()) {
            this.getLogStream().println(msg);
        }
    }

    public void info(String msg, Throwable t) {

        if (this.isInfoEnabled()) {
            this.getLogStream().println(msg + "Exception :: " + t.getMessage());
            t.printStackTrace(this.getLogStream());
        }
    }

    public void fatal(String msg) {
        this.error(msg);
    }

    public void fatal(String msg, Throwable t) {
        this.error(msg, t);
    }


    public boolean isTraceEnabled() {
        return this.logLevel.compareTo(TRACE) < 0;
    }
    
    public boolean isDebugEnabled() {
        return this.logLevel.compareTo(DEBUG) < 0;
    }

    public boolean isInfoEnabled() {
        return this.logLevel.compareTo(INFO) < 0;
    }
    
    

    public void warn(String msg) {
        this.error(msg);

    }

    public void warn(String msg, Throwable t) {
        this.error(msg, t);
    }

	public void trace(String msg, Throwable t) {
		  if (this.isTraceEnabled()) {
	            this.getLogStream().println(msg + "Exception :: " + t.getMessage());
	            t.printStackTrace(this.getLogStream());
	        }
	}

	public void trace(String msg) {
		 if (this.isTraceEnabled()) {
	            this.getLogStream().println(msg);
	        }
	}

}
