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
import org.csc.phynixx.watchdog.objectref.IObjectReference;
import org.csc.phynixx.watchdog.objectref.ObjectReference;
import org.csc.phynixx.watchdog.objectref.WeakObjectReference;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


class Watchdog implements Runnable, IWatchdog {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private RestartCondition restartCondition = null;

    /**
     * to be able to restart the watchdog thread it is kept as a handle
     */
    private class ThreadHandle {
        private Thread thread = null;

        private ThreadHandle() {
            super();
        }

        private void start() {
            if (this.thread != null && this.thread.isAlive()) {
                throw new IllegalStateException("Thread " + this.thread + " is alive and can't be restartet");
            }
            String title = "Watchdog::" + Watchdog.this.getId().toString();
            this.thread = new Thread(Watchdog.this, title);
            this.thread.start();
        }

        private Thread getThread() {
            return thread;
        }

        public String toString() {
            return this.thread != null ? this.thread.toString() : "null";
        }

        private void shutdown() {
            this.thread = null;
        }
    }

    ;


    private Set conditions = new HashSet();

    private String description = "";

    private long checkInterval = 100L;
    private volatile boolean killed = false;
    private ThreadHandle threadHandle = new ThreadHandle();


    private Long id = new Long(-1L);


    /**
     * instanciate Watchdog via WatchdogFactory
     *
     * @param id            TODO
     * @param checkInterval
     */
    Watchdog(Long id, long checkInterval) {
        this(id, checkInterval, "");
    }

    Watchdog(Long id, long checkInterval, String description) {
        super();
        this.checkInterval = checkInterval;
        this.description = description;
        this.id = id;
        this.threadHandle.start(); // start the watchdog thread
        this.restartCondition = new RestartCondition(WatchdogRegistry.getWatchdogManagementInterval(), this);
    }


    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#getId()
     */
    public Long getId() {
        return id;
    }

    ThreadHandle getThreadHandle() {
        return threadHandle;
    }


    /**
     * @return
     */
    RestartCondition getRestartCondition() {
        return restartCondition;
    }

    /**
     * @return thread executing the background watch
     * my be null (the watchdog needs to be restarted)
     */
    public Thread getThread() {
        ThreadHandle th = this.getThreadHandle();
        return th.thread;
    }

    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#getCheckInterval()
     */
    public long getCheckInterval() {
        return checkInterval;
    }


    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#registerCondition(org.csc.phynixx.watchdog.IWatchedCondition)
     */
    public void registerCondition(IWatchedCondition cond) {
        this.registerCondition(cond, true);
    }

    /**
     * @param cond
     * @param weakReferenced indicates if the condition ist weak referenced
     */
    void registerCondition(IWatchedCondition cond, boolean weakReferenced) {
        synchronized (conditions) {
            if (!this.conditions.contains(cond)) {
                IObjectReference objRef = null;
                if (weakReferenced) {
                    objRef = new WeakObjectReference(cond);
                } else {
                    objRef = new ObjectReference(cond);
                }
                // keep a weak reference to the condition
                this.conditions.add(objRef);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#getCountRegisteredConditions()
     */
    public int getCountRegisteredConditions() {
        return this.conditions.size();
    }

    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#getAliveConditions()
     */
    public Set getAliveConditions() {
        return this.copyConditions();
    }


    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#unregisterCondition(org.csc.phynixx.watchdog.IWatchedCondition)
     */
    public void unregisterCondition(IWatchedCondition cond) {
        synchronized (conditions) {
            // cond and Objectrefrence can be used interchngeably
            if (!this.conditions.contains(cond)) {
                this.conditions.remove(cond);
            }
        }
    }

    void copyConditions(IWatchdog wd) {
        Set copiedConditions = this.copyConditions();

        synchronized (wd) {
            for (Iterator iterator = copiedConditions.iterator(); iterator.hasNext(); ) {
                wd.registerCondition((IWatchedCondition) iterator.next());
            }
        }
    }

    /**
     * copies the current set of conditions
     *
     * @return
     */
    private Set copyConditions() {
        Set copiedConditions = null;
        // Copy the current Conditions ...
        synchronized (conditions) {

            Set copiedObjref = new HashSet(this.conditions);
            copiedConditions = new HashSet(this.conditions.size());

            this.conditions.clear();
            IWatchedCondition cond = null;
            for (Iterator iterator = copiedObjref.iterator(); iterator.hasNext(); ) {
                IObjectReference objRef = (IObjectReference) iterator.next();
                cond = (IWatchedCondition) objRef.get();
                if (!objRef.isStale() && cond != null && !cond.isUseless()) {
                    copiedConditions.add(cond);
                    this.conditions.add(objRef);
                }
            }

        }

        return copiedConditions;
    }


    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#isKilled()
     */
    public boolean isKilled() {
        return killed;
    }

    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#activate()
     */
    public synchronized void activate() {

        synchronized (this.conditions) {
            IWatchedCondition cond = null;
            for (Iterator iterator = this.conditions.iterator(); iterator.hasNext(); ) {
                IObjectReference objRef = (IObjectReference) iterator.next();
                if (objRef.get() != null) {
                    cond = (IWatchedCondition) objRef.get();
                    cond.setActive(true);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#deactivate()
     */
    public synchronized void deactivate() {

        synchronized (this.conditions) {
            IWatchedCondition cond = null;
            for (Iterator iterator = this.conditions.iterator(); iterator.hasNext(); ) {
                IObjectReference objRef = (IObjectReference) iterator.next();
                if (objRef.get() != null) {
                    cond = (IWatchedCondition) objRef.get();
                    cond.setActive(false);
                }
            }
        }
    }

    /**
     * stops the executing thread but does not kill the thread.
     * It can be restarted
     */
    public synchronized void stop() {
        try {
            this.kill();
        } finally {
            this.killed = false;
            this.restartCondition.setUseless(false);
            this.restartCondition.setActive(false);
        }

    }


    public synchronized void kill() {
        this.restartCondition.setUseless(true);

        if (this.isKilled() || this.threadHandle.getThread() == null || !this.threadHandle.getThread().isAlive()) {
            this.killed = true;
            this.threadHandle.shutdown();
            this.acknowledgeKilled();
            return;
        }

        // Interrupt the excuting thread an waiting for shutdown
        if (!this.isKilled() && this.threadHandle.getThread() != null && this.threadHandle.getThread().isAlive()) {
            this.killed = true;
            this.threadHandle.getThread().interrupt();
        }

        // wait until the thread is stopped ....
        int limit = 10;
        int count = 0;
        while (this.threadHandle.getThread().isAlive() && count < limit) {
            if (log.isInfoEnabled()) {
                log.info("Watchdog.kill: Waiting to notification " + this.getId());
            }
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
            }
            count++;
        }
        if (log.isInfoEnabled()) {
            log.info("Watchdog.kill: Watchdog " + this.getId() + " killed");
        }

        this.threadHandle.shutdown();
    }

    public synchronized void restart() {

        this.kill();

        this.killed = false;
        this.threadHandle.start();

        if (this.log.isInfoEnabled()) {
            this.log.info(" Watchdog " + getId() + " restarted");
        }

        this.restartCondition.setUseless(false);
        this.restartCondition.setActive(true);

    }

    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#isUseless()
     */
    public boolean isUseless() {

        boolean useless = true;
        synchronized (conditions) {
            for (Iterator iterator = this.conditions.iterator(); iterator.hasNext(); ) {
                IObjectReference objRef = (IObjectReference) iterator.next();
                if (!objRef.isStale() && !((IWatchedCondition) (objRef.get())).isUseless()) {
                    useless = false;
                }
            }
        }

        return useless;
    }


    public boolean isStale() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#isAlive()
     */
    public boolean isAlive() {
        if (this.threadHandle.getThread() != null) {
            return this.threadHandle.getThread().isAlive();
        }
        return false;
    }

    public void run() {
        while (!this.isKilled()) {
            // wait until it time to check the conditions
            long start = System.currentTimeMillis();
            long waiting = this.checkInterval;
            while (waiting > 0) {
                try {
                    Thread.currentThread().sleep(waiting);
                } catch (InterruptedException e) {
                } finally {
                    if (this.isKilled()) {
                        break;
                    }
                    waiting = this.checkInterval - (System.currentTimeMillis() - start);
                }
            }

            // recvheck if killed
            if (this.isKilled()) {
                break;
            }

            evaluateConditions();

            // yielding ...
            try {
                Thread.currentThread().sleep(10);
            } catch (InterruptedException e) {
                ;
                continue; // interrupted and stopped ...
            }
        }
        // acknowledge the thread starvation for all waiting for the dead of the thread
        this.acknowledgeKilled();
    }

    /**
     * evaluate the conditions
     * <p/>
     * it's synchronized to prevent any other thread from changing the state of
     * the watchdog or of one of its conditions
     */
    private synchronized void evaluateConditions() {
        // Copy the current Conditions ...
        Set copiedConditions = this.copyConditions();

        // check the conditions ....
        for (Iterator iterator = copiedConditions.iterator(); iterator.hasNext(); ) {
            IWatchedCondition cond = (IWatchedCondition) iterator.next();

            synchronized (cond) {
                // the conditions are not accessed in a synchronized way. This has to be  make sure by the Implementations
                if (cond.isActive() && !cond.checkCondition()) {
                    cond.conditionViolated();
                }
            }
        }
        return;
    }

    private void acknowledgeKilled() {
        synchronized (this) {
            this.notifyAll();
        }

    }

    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#getConditionInfos()
	 */
    public String[] getConditionInfos() {
        String[] conds = new String[conditions.size()];
        synchronized (conditions) {
            int i = 0;
            for (Iterator iterator = this.conditions.iterator(); iterator.hasNext(); i++) {
                IObjectReference objRef = (IObjectReference) iterator.next();
                StringBuffer buffer = new StringBuffer(" -- ");
                if (objRef.isStale()) {
                    buffer.append("primarily description :: " + objRef.getObjectDescription());
                } else {
                    buffer.append(objRef.get());
                }
                buffer.append("; isStale=").append(objRef.isStale()).
                        append(" ; isWeakReference=").append(objRef.isWeakReference());
                conds[i] = buffer.toString();
            }
        }

        return conds;
    }

    /* (non-Javadoc)
     * @see org.csc.phynixx.watchdog.IWatchog#getWatchdogInfo()
     */
    public String getWatchdogInfo() {
        StringBuffer buffer = new StringBuffer("Watchdog [").
                append("ID=").append(this.id).
                append("; CheckInterval=").append(this.checkInterval).append(" msecs");
        if (this.description != null && !this.description.equals("")) {
            buffer.append("; ").append(this.description);
        }
        buffer.append("]");
        buffer.append(" isAlive=").append(this.isAlive()).append("; ");
        if (this.getThread() != null) {
            buffer.append(this.getThread());
        } else {
            buffer.append("NO THREAD");
        }
        buffer.append(" #watched conditions=").append(this.getAliveConditions().size());

        return buffer.toString();
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer(this.getWatchdogInfo()).append('\n');

        String[] conditionInfos = this.getConditionInfos();
        for (int i = 0; i < conditionInfos.length; i++) {
            buffer.append(" . . . ").append(conditionInfos[i]).append("\n");
        }

        return buffer.toString();

    }


}
