package org.csc.phynixx.loggersystem.messages;

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


import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.ILoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * the current class is responsible for instanciating new
 * {@link PhynixxXARecorderResource}. A new Logger ist requested by the ILoggerFactory an
 * assigned to the {@link PhynixxXARecorderResource} The RecordLoggerFactory manages the opened
 * PhynixxXARecorderResource
 *
 * @author christoph
 */
class RecordLoggerSystem implements IXARecorderResourceListener {

    private static final String GLOBAL_FORMAT_PATTERN = "({0}_[a-z,A-Z,0-9]*[^_])_([0-9]*[^\\.])\\.[\\w]*";

    private static final String LOGGER_FORMAT_PATTERN = "({0})_([0-9]*[^\\.])\\.[\\w]*";

    private ILoggerFactory loggerFactory = null;

    private Set openLoggers = new HashSet();

    private long idGenerator = System.currentTimeMillis();

    /**
     * ILoggereListeners watching the lifecycle of this logger
     */
    private List listeners = new ArrayList();

    private static final IPhynixxLogger LOGGER = PhynixxLogManager.getLogger(RecordLoggerSystem.class);

    private String loggerSystemName = null;

    public RecordLoggerSystem(String loggerSystemName, ILoggerFactory loggerFactory) {
        super();
        this.loggerSystemName = loggerSystemName;
        this.loggerFactory = loggerFactory;
        this.addListener(this);
    }

    public String getLoggerSystemName() {
        return this.loggerSystemName;
    }

    public PhynixxXARecorderResource instanciateLogger() throws IOException,
            InterruptedException {
        return this.instanciateLogger(true);
    }

    public synchronized PhynixxXARecorderResource instanciateLogger(boolean open) throws IOException, InterruptedException {

        final String myLoggerName = this.getLoggerSystemName() + "_" + Long.toString(idGenerator);
        idGenerator++;

        return instanciateLogger(myLoggerName, open);
    }

    private PhynixxXARecorderResource instanciateLogger(String loggerName, boolean open) throws IOException, InterruptedException {

        PhynixxXARecorderResource logger = new PhynixxXARecorderResource(this.loggerFactory.instanciateLogger(loggerName));

        for (int i = 0; i < this.listeners.size(); i++) {
            logger.addListener((IXARecorderResourceListener) listeners.get(i));
        }

        if (open) {
            logger.open();
        }
        this.openLoggers.add(logger);

        return logger;
    }

    public void recorderResourceClosed(IXARecorderResource recorderResource) {
        synchronized (this) {
            this.openLoggers.remove(recorderResource);
        }
    }

    public synchronized void recorderResourceOpened(IXARecorderResource recorderResource) {
        if (!this.openLoggers.contains(recorderResource)) {
            this.openLoggers.add(recorderResource);
        }
    }

    public synchronized void destroy(IXARecorderResource logger) {

        try {
            logger.destroy();
        } catch (Exception e) {
            LOGGER.error("Error destroying logger " + this + " :: " + e.getMessage());
        } finally {
            String pattern = MessageFormat.format(LOGGER_FORMAT_PATTERN,
                    new String[]{logger.getLoggerName()});
            this.loggerFactory.cleanup(pattern);
        }

    }

    /**
     * recovers all {@link PhynixxXARecorderResource} having LOGGER files
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public synchronized Set<IXARecorderResource> recover() throws Exception {
        Set<IXARecorderResource> xaResourceLoggers = new HashSet<IXARecorderResource>();

        // delete all LOGGER files .....
        final String pattern = MessageFormat.format(GLOBAL_FORMAT_PATTERN, new String[]{this.getLoggerSystemName()});

        Set<String> loggerNames = this.loggerFactory.findLoggerNames(pattern);
        for (Iterator<String> iterator = loggerNames.iterator(); iterator.hasNext(); ) {
            xaResourceLoggers.add(this.instanciateLogger(iterator.next(), true));
        }
        return xaResourceLoggers;
    }

    /**
     * closes all open loggers
     */
    public synchronized void close() {
        HashSet copiedLoggers = new HashSet(this.openLoggers);
        for (Iterator iterator = copiedLoggers.iterator(); iterator.hasNext(); ) {
            PhynixxXARecorderResource logger = (PhynixxXARecorderResource) iterator.next();
            try {
                logger.close();
            } catch (Exception e) {
            }
            openLoggers.remove(logger);
        }

    }

    public synchronized void addListener(IXARecorderResourceListener listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

}
