
package org.csc.phynixx.common.logger;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Implements {@link IPhynixxLogger} on base of log4j
 */
public class SLF4JLogger implements IPhynixxLogger {

    private Logger log = null;

    SLF4JLogger(Class<?> cls) {
        this.log = LoggerFactory.getLogger(cls);
    }

    SLF4JLogger(String logger) {
        this.log = LoggerFactory.getLogger(logger);
    }
    
    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ILogger#debug(java.lang.Object)
	 */
    @Override
	public void trace(String o) {
        log.trace(o);
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ILogger#debug(java.lang.Object, java.lang.Throwable)
	 */
    @Override
	public void trace(String o, Throwable t) {
        log.trace(o, t);
    }


    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ILogger#debug(java.lang.Object)
	 */
    public void debug(String o) {
        log.debug(o);
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ILogger#debug(java.lang.Object, java.lang.Throwable)
	 */
    public void debug(String o, Throwable t) {
        log.debug(o, t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#info(java.lang.Object)
	 */
    public void info(String o) {
        log.info(o);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#info(java.lang.Object, java.lang.Throwable)
	 */
    public void info(String o, Throwable t) {
        log.info(o, t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#warn(java.lang.Object)
	 */
    public void warn(String o) {
        log.warn(o);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#warn(java.lang.Object, java.lang.Throwable)
	 */
    public void warn(String o, Throwable t) {
        log.warn(o, t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#error(java.lang.Object)
	 */
    public void error(String o) {
        log.error(o);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#error(java.lang.Object, java.lang.Throwable)
	 */
    public void error(String o, Throwable t) {
        log.error(o, t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#fatal(java.lang.Object)
	 */
    public void fatal(String o) {
        log.error(o);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#fatal(java.lang.Object, java.lang.Throwable)
	 */
    public void fatal(String o, Throwable t) {
        log.error(o, t);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }


    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }


}
