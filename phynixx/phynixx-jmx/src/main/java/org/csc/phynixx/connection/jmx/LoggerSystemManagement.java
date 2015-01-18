package org.csc.phynixx.connection.jmx;

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


import org.csc.phynixx.loggersystem.logrecord.IXARecorderRepository;
import org.csc.phynixx.loggersystem.logrecord.IXARecorderResourceListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * to keep the management up to date register the current class as listener to the {@link Logger} */
public class LoggerSystemManagement implements LoggerSystemManagementMBean, IXARecorderResourceListener {

    private AtomicInteger openLoggerCounter = new AtomicInteger(0);

    /* (non-Javadoc)
     * @see org.csc.phynixx.connection.jmx.LoggerSystemManagementMBean#getOpenLoggers()
     */
    public int getOpenLoggers() {
        return this.openLoggerCounter.get();
    }


    @Override
    public synchronized void  recorderResourceClosed(IXARecorderRepository recorderResource) {
        this.openLoggerCounter=new AtomicInteger(this.openLoggerCounter.decrementAndGet());
    }

    @Override
    public synchronized  void recorderResourceOpened(IXARecorderRepository recorderResource) {
        this.openLoggerCounter=new AtomicInteger(this.openLoggerCounter.incrementAndGet());
    }
}
