
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link org.csc.phynixx.common.logger.IPhynixxLogger} on base of log4j
 */
public class LogbackLogger implements IPhynixxLogger {

    private Logger log = null;

    LogbackLogger(Class cls) {
        this.log = LoggerFactory.getLogger(cls);
    }

    LogbackLogger(String logger) {
        this.log = LoggerFactory.getLogger(logger);
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ILogger#debug(java.lang.Object)
	 */
    public void debug(Object o) {
        log.debug(toString(o));
    }

    private String toString(Object o) {
        return (o==null)?"NULL":o.toString();
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ILogger#debug(java.lang.Object, java.lang.Throwable)
	 */
    public void debug(Object o, Throwable t) {
        log.debug(toString(o), t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#info(java.lang.Object)
	 */
    public void info(Object o) {
        log.info(toString(o));
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#info(java.lang.Object, java.lang.Throwable)
	 */
    public void info(Object o, Throwable t) {
        log.info(toString(o), t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#warn(java.lang.Object)
	 */
    public void warn(Object o) {
        log.warn(toString(o));
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#warn(java.lang.Object, java.lang.Throwable)
	 */
    public void warn(Object o, Throwable t) {
        log.warn(toString(o), t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#error(java.lang.Object)
	 */
    public void error(Object o) {
        log.error(toString(o));
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#error(java.lang.Object, java.lang.Throwable)
	 */
    public void error(Object o, Throwable t) {
        log.error(toString(o), t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#fatal(java.lang.Object)
	 */
    public void fatal(Object o) {
        log.error(toString(o));
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#fatal(java.lang.Object, java.lang.Throwable)
	 */
    public void fatal(Object o, Throwable t) {
        log.error(toString(o), t);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }


}
