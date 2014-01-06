package org.csc.phynixx.evaluation.howl;

/*
 * #%L
 * phynixx-howl
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
import org.objectweb.howl.log.LogEventListener;

public class TestLogEventListener implements LogEventListener {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    public boolean isLoggable(int level) {
        return false;
    }

    public void log(int level, String message) {
        return;

    }

    public void log(int level, String message, Throwable thrown) {
        return;

    }

    public int count = 0;

    public void logOverflowNotification(long logkey) {
        log.info("logOverflowNotification logKey=" + logkey + " count=" + count);
        count++;
    }

}
