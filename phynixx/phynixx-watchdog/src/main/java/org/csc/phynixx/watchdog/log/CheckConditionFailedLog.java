package org.csc.phynixx.watchdog.log;

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


import org.csc.phynixx.watchdog.IWatchedCondition;

/**
 *
 * Created by christoph on 09.06.2012.
 */
public class CheckConditionFailedLog implements IWatchdogLog {

    private long timestamp = 0l;
    private String condition = null;
    private String description = null;

    public CheckConditionFailedLog(IWatchedCondition condition) {
        super();
        this.timestamp = System.currentTimeMillis();
        this.condition = condition.toString();

        this.description = condition != null ? condition.toString() : "?";
    }

    public CheckConditionFailedLog(IWatchedCondition condition, String description) {
        super();
        if( condition==null) {
            throw new IllegalArgumentException("Condition must be defined");
        }
        this.timestamp = System.currentTimeMillis();
        this.condition = condition.toString();
        this.description = this.condition + " " + description;

    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCondition() {
        return condition;
    }

    public String getDescription() {
        return this.description;
    }

    public String toString() {
        return "CheckConditionFailed : " + this.getDescription();
    }


}
