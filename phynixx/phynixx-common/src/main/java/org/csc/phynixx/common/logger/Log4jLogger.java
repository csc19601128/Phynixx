
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


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Log4jLogger implements IPhynixxLogger {

    private Logger log = null;

    Log4jLogger(Class cls) {
        this.log = LogManager.getLogger(cls);
    }

    Log4jLogger(String logger) {
        this.log = LogManager.getLogger(logger);
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ILogger#debug(java.lang.Object)
	 */
    public void debug(Object o) {
        log.debug(o);
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ILogger#debug(java.lang.Object, java.lang.Throwable)
	 */
    public void debug(Object o, Throwable t) {
        log.debug(o, t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#info(java.lang.Object)
	 */
    public void info(Object o) {
        log.info(o);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#info(java.lang.Object, java.lang.Throwable)
	 */
    public void info(Object o, Throwable t) {
        log.info(o, t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#warn(java.lang.Object)
	 */
    public void warn(Object o) {
        log.warn(o);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#warn(java.lang.Object, java.lang.Throwable)
	 */
    public void warn(Object o, Throwable t) {
        log.warn(o, t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#error(java.lang.Object)
	 */
    public void error(Object o) {
        log.error(o);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#error(java.lang.Object, java.lang.Throwable)
	 */
    public void error(Object o, Throwable t) {
        log.error(o, t);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#fatal(java.lang.Object)
	 */
    public void fatal(Object o) {
        log.fatal(o);
    }

    /* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ILogger#fatal(java.lang.Object, java.lang.Throwable)
	 */
    public void fatal(Object o, Throwable t) {
        log.fatal(o, t);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }


}
