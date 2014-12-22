package org.csc.phynixx.watchdog;

/*
 * #%L
 * phynixx-watchdog
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
import org.csc.phynixx.watchdog.log.CheckConditionFailedLog;


public abstract class TimeoutCondition extends NotificationCondition implements ITimeoutCondition {


    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private long timeout = -1;

    private long start = -1;


    public TimeoutCondition(long timeout) {
        super();
        this.timeout = timeout;
        this.resetCondition();
    }


    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.watchdog.ITimeoutCondition#getTimeout()
     */
    public synchronized long getTimeout() {
        return timeout;
    }


    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.watchdog.ITimeoutCondition#setActive(boolean)
     */
    public synchronized void setActive(boolean active) {
        super.setActive(active);
        if (active) {
            this.start = System.currentTimeMillis();
        } else {
            this.start = -1;
        }
    }


    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.watchdog.ITimeoutCondition#resetCondition()
     */
    public synchronized void resetCondition() {
        this.start = System.currentTimeMillis();
    }


    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.watchdog.ITimeoutCondition#resetCondition(long)
     */
    public synchronized void resetCondition(long timeout) {
        this.timeout = timeout;
        this.start = System.currentTimeMillis();
    }

    /*
     * Not synchronized as to be meant for the watch dog exclusively
     *
     * Do not call it unsynchronized
     *
     * (non-Javadoc)
     * @see de.csc.xaresource.sample.watchdog.ITimeoutCondition#checkCondition()
     */
    public boolean checkCondition() {
        long timeElapsed = System.currentTimeMillis() - start;
        if (timeElapsed > this.timeout) {

            if (log.isDebugEnabled()) {
                //log.info("Current Thread="+Thread.currentThread()+" Condition="+this+" Checkpoint "+ System.currentTimeMillis() +" timeout="+(System.currentTimeMillis() - start));
                String logString = " violated at " + System.currentTimeMillis() +
                        " (elapsed time=" + timeElapsed + ")";
                log.debug(new CheckConditionFailedLog(this, logString).toString());
            }
            return false;
        }
        return true;
    }

    public String toString() {
        return "Timeout Condition " + this.timeout + " msecs  (isActive=" + this.isActive() + ")";
    }


    /*
     *
     * Not synchronized as to be meant for the watch dog exclusively
     *
     * Do not call it unsynchronized
     *
     * (non-Javadoc)
     * @see de.csc.xaresource.sample.watchdog.ITimeoutCondition#conditionViolated()
     */
    public abstract void conditionViolated();

}
