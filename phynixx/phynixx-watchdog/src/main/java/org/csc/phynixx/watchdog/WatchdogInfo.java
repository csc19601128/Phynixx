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


import java.io.Serializable;


public class WatchdogInfo implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = -4208724399286983526L;

    private String info = null;

    private String[] conditions;

    public WatchdogInfo(IWatchdog wd) {
        this.setConditions(wd);
        this.setInfo(wd);
    }


    private void setInfo(IWatchdog wd) {

        this.info = wd.getWatchdogInfo();

    }

    private void setConditions(IWatchdog wd) {


        this.conditions = wd.getConditionInfos();

    }


    public String getWatchdogInfo() {
        return info;
    }


    public String[] getConditions() {
        return conditions;
    }


    public String[] getWatchdogInfos() {
        String[] conds = this.getConditions();
        String[] infos = new String[conds.length + 1];

        infos[0] = this.getWatchdogInfo();

        for (int i = 0; i < conds.length; i++) {
            infos[i + 1] = "     " + conds[i];
        }
        return infos;

    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Watchdog - executing Thread=" + this.getWatchdogInfo()).append("\n");
        String[] conds = this.getConditions();
        for (int i = 0; i < conds.length; i++) {
            buffer.append("     ").append(conds[i]).append("\n");
        }
        return buffer.toString();
    }


}
