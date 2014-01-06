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


import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.watchdog.log.CheckConditionFailedLog;
import org.csc.phynixx.watchdog.log.ConditionViolatedLog;

/**
 * Checks if the watchdig is alive.
 * If not it is restarted ...
 */

class RestartCondition extends TimeoutCondition implements IWatchedCondition {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private WatchdogReference watchdogReference = null;

    RestartCondition(long checkInterval, Watchdog wd) {
        super(checkInterval);
        this.watchdogReference = new WatchdogReference(wd);
    }

    /**
     * Not synchronized as to be meant for the watch dog exclusively
     * <p/>
     * Do not call it unsynchronized
     */
    public boolean checkCondition() {

        // assure that the checkinterval is elapsed .....
        if (super.checkCondition()) {
            return true;
        }

        if (this.watchdogReference.isStale()) {
            return true;
        }

        Watchdog wd = this.watchdogReference.getWatchdog();

        //log.info("Checking "+this+"\n watched WD is alive="+watchedWatchdog.isAlive()+" is killed="+watchedWatchdog.isKilled()+" WD-Thead="+this.watchedWatchdog.getThreadHandle());
        if (!wd.isAlive()) {
            if (log.isInfoEnabled()) {
                String logString = "RestartCondition :: Watchdog " + wd.getThreadHandle() + " is not alive ";
                log.info(new CheckConditionFailedLog(this, logString).toString());
            }
            return false;
        }
        return true;
    }


    public String toString() {
        return " RestartCondition referenced Watchdog " + watchdogReference.getId() + " and check interval=" + this.getTimeout();
    }

    /**
     * Not synchronized as to be meant for the watch dog exclusively
     * <p/>
     * Do not call it unsynchronized
     */
    public void conditionViolated() {
        if (this.watchdogReference.isStale()) {
            return;
        }

        Watchdog wd = this.watchdogReference.getWatchdog();

        wd.restart();
        if (log.isInfoEnabled()) {
            log.info(new ConditionViolatedLog(this, "Watchdog " + wd.getId() + " is restarted by Condition  " + this.toString()).toString());
        }

    }


    public boolean isUseless() {
        return this.watchdogReference.isStale();
    }


}
