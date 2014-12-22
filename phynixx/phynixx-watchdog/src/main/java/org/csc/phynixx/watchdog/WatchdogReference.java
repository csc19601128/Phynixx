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


import java.util.Set;

public class WatchdogReference implements IWatchdog {

    private Long id = null;

    public WatchdogReference(Long id) {
        super();
        this.id = id;
    }


    public WatchdogReference(IWatchdog wd) {
        this(wd.getId());
    }

    public Long getId() {
        return id;
    }


    Watchdog getWatchdog() {
        Watchdog wd =
                WatchdogRegistry.getTheRegistry().findWatchdog(id);
        if (wd == null) {
            throw new IllegalStateException("Watchdog is stale and does not exist any longer");
        }
        return wd;
    }

    public boolean isStale() {
        return WatchdogRegistry.getTheRegistry().findWatchdog(id) == null;
    }


    public void activate() {
        this.getWatchdog().activate();

    }


    public void deactivate() {
        this.getWatchdog().deactivate();

    }


    public Set getAliveConditions() {
        return this.getWatchdog().getAliveConditions();
    }


    public long getCheckInterval() {
        return this.getWatchdog().getCheckInterval();
    }


    public String[] getConditionInfos() {
        return this.getWatchdog().getConditionInfos();
    }


    public int getCountRegisteredConditions() {
        return this.getWatchdog().getCountRegisteredConditions();
    }


    public String getWatchdogInfo() {
        return this.getWatchdog().getWatchdogInfo();
    }


    public boolean isAlive() {
        return this.getWatchdog().isAlive();
    }


    public boolean isKilled() {
        return this.getWatchdog().isKilled();
    }


    public boolean isUseless() {
        return this.getWatchdog().isUseless();
    }


    public void registerCondition(IWatchedCondition cond) {
        this.getWatchdog().registerCondition(cond);

    }


    public void unregisterCondition(IWatchedCondition cond) {
        this.getWatchdog().unregisterCondition(cond);
    }

}
