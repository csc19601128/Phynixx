package org.csc.phynixx.watchdog.jmx;

/*
 * #%L
 * phynixx-jmx
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


import org.csc.phynixx.watchdog.WatchdogInfo;
import org.csc.phynixx.watchdog.WatchdogRegistry;

public class WatchTheWatchdogs implements WatchTheWatchdogsMBean {

    public String getState() {
        return WatchdogRegistry.getTheRegistry().getManagementWatchdogsState();
    }

    public String[][] showWatchdogInfos() throws Exception {
        WatchdogInfo[] infos = WatchdogRegistry.getTheRegistry().getManagementWatchdogsInfo();

        String[][] wds = new String[infos.length][];
        for (int j = 0; j < infos.length; j++) {
            wds[j] = infos[j].getWatchdogInfos();
        }
        return wds;
    }


    public void restart() throws Exception {
        WatchdogRegistry.getTheRegistry().restart();
    }

}
