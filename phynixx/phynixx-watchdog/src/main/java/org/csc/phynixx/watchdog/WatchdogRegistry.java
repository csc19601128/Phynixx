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


import org.csc.phynixx.common.generator.IDGenerator;
import org.csc.phynixx.common.generator.IDGenerators;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;

import java.util.*;


/**
 * @author
 * @version $Revision: 1.6 $
 * @see ThreadGroup
 */
public class WatchdogRegistry {


    private static final IDGenerator<Long> ID_GENERATOR = IDGenerators.createLongGenerator(1, true);

    private static final String WATCHDOG_MANAGEMENT_INTERVAL_PROP = "org.csc.phynixx.watchdog.management_interval";

    public static final String OK = "ok";

    private static long WATCHDOG_MANAGEMENT_INTERVAL = 1100;


    private static WatchdogRegistry theRegistry = null;

    public interface IWatchdogVisitor {
        void visit(IWatchdog th);
    }

    private Watchdog watchTheWatchdogs = null;
    private Watchdog watchTheWatchdogWatcher = null;


    public static long getWatchdogManagementInterval() {
        return WATCHDOG_MANAGEMENT_INTERVAL;
    }

    public static void setWatchdogManagementInterval(long watchdogManagementInterval) {
        WatchdogRegistry.WATCHDOG_MANAGEMENT_INTERVAL = watchdogManagementInterval;

        // lock all restart conditions and assign a new check interval ...
        Set conditions = WatchdogRegistry.theRegistry.watchTheWatchdogs.getAliveConditions();
        conditions.addAll(WatchdogRegistry.theRegistry.watchTheWatchdogWatcher.getAliveConditions());
        for (Iterator iterator = conditions.iterator(); iterator.hasNext(); ) {
            IWatchedCondition cond = (IWatchedCondition) iterator.next();
            if (cond instanceof TimeoutCondition) {
                TimeoutCondition toCond = (TimeoutCondition) cond;
                synchronized (toCond) {
                    toCond.resetCondition(WatchdogRegistry.WATCHDOG_MANAGEMENT_INTERVAL);
                }

            }

        }
    }

    public static long getWatchTheWatchdogInterval() {
        return WATCHDOG_MANAGEMENT_INTERVAL;
    }


    static {
        WatchdogRegistry.WATCHDOG_MANAGEMENT_INTERVAL = Long.getLong(WATCHDOG_MANAGEMENT_INTERVAL_PROP, 5000).longValue();

        WatchdogRegistry.theRegistry = new WatchdogRegistry();
    }

    private WatchdogRegistry() {
        // System.out.println("Start the WatchdogRegistry");
        this.watchTheWatchdogs = new Watchdog(ID_GENERATOR.generate(), WatchdogRegistry.getWatchdogManagementInterval() / 2, "Watches The Watchdogs");
        this.watchTheWatchdogWatcher = new Watchdog(ID_GENERATOR.generate(), WatchdogRegistry.getWatchdogManagementInterval() / 2, "Watches The WatchdogWatcher");

        IWatchedCondition watchesTheWatcherCond =
                new RestartCondition(WatchdogRegistry.getWatchdogManagementInterval(), watchTheWatchdogs) {
                    public String toString() {
                        return new StringBuffer("watches the watchdog watcher ").
                                append("isActive=").append(this.isActive()).
                                append(" Watched WD.isAlive=").append(watchTheWatchdogs.isAlive()).
                                append(" Watched WD.isKilled=").append(watchTheWatchdogs.isKilled()).
                                toString();
                    }
                };


        watchesTheWatcherCond.setActive(true);
        this.watchTheWatchdogWatcher.registerCondition(watchesTheWatcherCond, false);

        /**
         * checks if the
         */
        IWatchedCondition managementActivity = new TimeoutCondition(WATCHDOG_MANAGEMENT_INTERVAL) {

            public void conditionViolated() {
                this.resetCondition();
                WatchdogRegistry.getTheRegistry().clearOut();
            }

        };
        managementActivity.setActive(true);
        this.watchTheWatchdogWatcher.registerCondition(managementActivity, false);

        this.restartManagementWatchdogs();

    }


    /**
     * Logger
     */
    protected IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    /**
     * @associates WorkerThread
     */
    private Map registeredWachdogs = new HashMap();

    private void checkManagementWatchdogs() {
        if (watchTheWatchdogs == null || !watchTheWatchdogs.isAlive()) {
            throw new IllegalStateException("WatchTheWatchdogs is not started -> call WatchdogRegistry.restart()");
        }
        if (watchTheWatchdogWatcher == null || !watchTheWatchdogWatcher.isAlive()) {
            throw new IllegalStateException("WatchTheWatchdogWatcher is not started -> call WatchdogRegistry.restart()");
        }
    }

    private void restartManagementWatchdogs() {

        if (watchTheWatchdogs != null && (!watchTheWatchdogs.isAlive())) {
            watchTheWatchdogs.restart();
            watchTheWatchdogs.activate();
        }
        if (watchTheWatchdogWatcher != null && (!watchTheWatchdogWatcher.isAlive())) {
            watchTheWatchdogWatcher.restart();
            watchTheWatchdogWatcher.activate();
        }
    }

    private void shutdownManagementWatchdogs() {


        if (watchTheWatchdogs != null && (watchTheWatchdogs.isAlive())) {
            watchTheWatchdogs.stop();
        }
        if (watchTheWatchdogWatcher != null && (watchTheWatchdogWatcher.isAlive())) {
            watchTheWatchdogWatcher.stop();
        }
    }

    public WatchdogInfo[] getManagementWatchdogsInfo() {

        WatchdogInfo[] wds = new WatchdogInfo[2];
        wds[0] = new WatchdogInfo(watchTheWatchdogs);
        wds[1] = new WatchdogInfo(watchTheWatchdogWatcher);

        return wds;
    }

    public synchronized String getManagementWatchdogsState() {
        if (!watchTheWatchdogs.isAlive() || !watchTheWatchdogWatcher.isAlive()) {

            return "Management Watchdogs aren't alive  -> restart it";
        }

        return OK;
    }

    public synchronized IWatchdog createWatchdog(final long checkInterval) {

        checkManagementWatchdogs();

        Watchdog wd = new Watchdog(ID_GENERATOR.generate(), checkInterval);


        // the watchdigWatcher registered the new Watchdog ....

        IWatchedCondition restartCondition = wd.getRestartCondition();
        restartCondition.setActive(true);

        watchTheWatchdogs.registerCondition(restartCondition, true);

        WatchdogRegistry.getTheRegistry().registerWatchdog(wd);

        if (log.isDebugEnabled()) {
            log.debug("Watchdog created \n" + wd);
            log.debug(watchTheWatchdogs.toString());
        }

        checkManagementWatchdogs();

        return wd;
    }


    public static WatchdogRegistry getTheRegistry() {
        return theRegistry;
    }

    /**
     * Fuegt einen Thread hinzu
     *
     * @param key String Schluessel unter dem der Thread gespeichert wird
     * @param wd  Watchdog
     * @throws IllegalStateException falls Thread NICHT zur aktuellen ThreadGroup( ==this) geh�rt;
     */
    private synchronized void registerWatchdog(Long key, Watchdog wd) {
        if (wd == null) {
            throw new NullPointerException("Thread");
        }
        if (wd.getThread() == null) {
            wd.restart();
        }
        registeredWachdogs.put(key, wd);
    }

    /**
     * Thread wird unter seinem Namen verwaltet (t.getName())
     *
     * @param wd Watchdog
     * @throws IllegalStateException falls Thread NICHT zur aktuellen ThreadGroup( ==this) geh�rt;
     */
    public void registerWatchdog(Watchdog wd) {
        registerWatchdog(wd.getId(), wd);
    }

    public synchronized void deregisterWatchdog(Long key) {
        if (registeredWachdogs.containsKey(key)) {
            registeredWachdogs.remove(key);
        }
    }

    synchronized Watchdog findWatchdog(Long id) {
        Watchdog wd = (Watchdog) this.registeredWachdogs.get(id);
        if (wd == null) {
            return null;
        }
        return wd;
    }

    public synchronized IWatchdog resolveWatchdogId(Long id) {
        Watchdog wd = (Watchdog) this.registeredWachdogs.get(id);
        if (wd == null) {
            return null;
        }
        return new WatchdogReference(wd);
    }


    public void deregisterWatchdog(IWatchdog wd) {
        deregisterWatchdog(wd.getId());
    }


    public synchronized void clearOut() {

        // shutdown all watchdogs
        Set copy = new HashSet(registeredWachdogs.values());
        Iterator iter = copy.iterator();
        while (iter.hasNext()) {
            Watchdog wd = (Watchdog) iter.next();
            if (wd.isUseless()) {
                wd.kill();
                this.deregisterWatchdog(wd);
                if (log.isInfoEnabled()) {
                    log.info("WatchdogRegistry.restart() : Watchdog " + wd.getId() + " closed");
                }
            }
        }

    }

    /**
     * restarts all Watchdogs
     */
    public synchronized void restart() {

        restartManagementWatchdogs();
        // shutdown all watchdogs
        Set copy = new HashSet(registeredWachdogs.values());
        Iterator iter = copy.iterator();
        while (iter.hasNext()) {
            Watchdog wd = (Watchdog) iter.next();
            if (wd.isUseless()) {
                wd.kill();
                this.deregisterWatchdog(wd);
                if (log.isInfoEnabled()) {
                    log.info("WatchdogRegistry.restart() : Watchdog " + wd.getId() + " closed");
                }
            } else if (!wd.isAlive() && !wd.isKilled()) {
                wd.restart();
                if (log.isInfoEnabled()) {
                    log.info("WatchdogRegistry.restart() : Watchdog " + wd.getId() + " restarted");
                }
            }
        }

    }

    /**
     * restarts all Watchdogs
     */
    public synchronized void stop() {
        // stopps all watchdogs
        Iterator iter = registeredWachdogs.values().iterator();
        while (iter.hasNext()) {
            Watchdog wd = (Watchdog) iter.next();
            wd.stop();
            if (log.isInfoEnabled()) {
                log.info("WatchdogRegistry.stop() : Watchdog " + wd.getId() + " is stopped");
            }
        }


    }

    /**
     * activates all Watchdogs
     */
    public synchronized void activate() {
        // activate all Watchdogs ....
        Iterator iter = registeredWachdogs.values().iterator();

        while (iter.hasNext()) {
            IWatchdog th = (IWatchdog) iter.next();
            th.activate();
            if (log.isInfoEnabled()) {
                log.info(". . . Activating Watchdog " + th.getId());
            }
        }
    }

    /**
     * restarts all Watchdogs
     */
    public synchronized void deactivate() {
        Iterator iter = registeredWachdogs.values().iterator();
        while (iter.hasNext()) {
            IWatchdog th = (IWatchdog) iter.next();
            th.deactivate();
            if (log.isInfoEnabled()) {
                log.info(". . . deactivating Watchdog " + th.getId());
            }
        }
    }


    /**
     * killt alle Threads der Gruppe
     */
    private void kill() {
        Set copy = new HashSet(registeredWachdogs.values());
        Iterator iter = copy.iterator();
        while (iter.hasNext()) {
            Watchdog th = (Watchdog) iter.next();
            if (th.isAlive()) {

                if (log.isInfoEnabled()) {
                    log.info(". . . Killing Watchdog " + th.getId());
                }
                th.kill();
            }
        }

        this.joinAllThreads();

        // remove all Watchdogs frim the registry ....
        iter = copy.iterator();
        while (iter.hasNext()) {
            this.deregisterWatchdog((IWatchdog) iter.next());
        }
    }


    /**
     * killt alle Threads der Gruppe und wartet bis auch der letzte beendet ist.
     * Es wird der evtl. Exceptionhandler geschlossen.
     */
    public synchronized void shutdown() {
        this.shutdownManagementWatchdogs();

        // deactivate all Watchdogs ....
        Iterator iter = registeredWachdogs.values().iterator();
        while (iter.hasNext()) {
            IWatchdog th = (IWatchdog) iter.next();
            th.deactivate();
            if (log.isInfoEnabled()) {
                log.info(". . . Deactivating Watchdog " + th.getId());
            }
        }

        this.kill();

    }

    /**
     * wartet bis auch der letzte Thread beendet ist
     */
    private void joinAllThreads() {
        Iterator iter = registeredWachdogs.values().iterator();
        while (iter.hasNext()) {
            Watchdog th = (Watchdog) iter.next();
            boolean isJoining = true;
            if (!th.isAlive() && log.isDebugEnabled()) {
                log.debug("Thread " + th + " finished");
            }
            while (th.isAlive() && isJoining) {
                try {
                    th.getThread().join();
                    isJoining = false;
                    if (log.isDebugEnabled()) {
                        log.debug("Thread " + th + " joined and finished");
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }


    public synchronized int getCountWatchdogs() {
        return this.registeredWachdogs.size();
    }


    public WatchdogInfo[] getWatchdogInfos() {

        final List watchdogs = new ArrayList();
        WatchdogRegistry.IWatchdogVisitor visitor = new WatchdogRegistry.IWatchdogVisitor() {
            public void visit(IWatchdog wd) {
                watchdogs.add(new WatchdogInfo(wd));
            }
        };
        this.visitWatchdogRegistry(visitor);

        WatchdogInfo[] wds = new WatchdogInfo[watchdogs.size()];
        int i = 0;
        for (Iterator iterator = watchdogs.iterator(); iterator.hasNext(); i++) {
            WatchdogInfo info = (WatchdogInfo) iterator.next();
            wds[i] = info;
        }
        return wds;
    }


    public String[][] showWatchdogInfos() {

        WatchdogInfo[] infos = this.getWatchdogInfos();
        String[][] wds = new String[infos.length][];
        for (int j = 0; j < infos.length; j++) {
            wds[j] = infos[j].getWatchdogInfos();
        }
        return wds;

    }


    /**
     *
     **/
    public synchronized void visitWatchdogRegistry(IWatchdogVisitor visitor) {
        Iterator iter = registeredWachdogs.values().iterator();
        while (iter.hasNext()) {
            IWatchdog th = (IWatchdog) iter.next();
            visitor.visit(th);
        }

    }

    /**
     * stops the the Watchdog with the given id
     * The executing thread of the watchdog is stopped, but the watchdog is not
     * removed from the registry.
     * It can be restarted
     *
     * @throws IllegalStateException Watchdog does not exist,
     *                               check existence with {@link #findWatchdog(Long)}
     * @see #restart(Long)
     * @see #shutdown(Long)
     */
    public void stop(Long id) {

        Watchdog wd = (Watchdog) this.registeredWachdogs.get(id);
        if (wd == null) {
            throw new IllegalStateException("Watchdog " + id + " ist not registered");
        }
        wd.stop();
    }


    /**
     * stops the the Watchdog with the given id
     * The executing thread of the watchdog is stopped and the watchdog is removed from the registry.
     * It can nor be restarted
     *
     * @throws IllegalStateException Watchdog does not exist,
     *                               check existence with {@link #findWatchdog(Long)}
     * @see #restart(Long)
     * @see #stop(Long)
     */
    public void shutdown(Long id) {

        Watchdog wd = (Watchdog) this.registeredWachdogs.get(id);
        if (wd == null) {
            throw new IllegalStateException("Watchdog " + id + " ist not registered");
        }
        wd.kill();
        this.deregisterWatchdog(id);
    }


    /**
     * restart the Watchdog with the given id
     *
     * @throws IllegalStateException Watchdog does not exist,
     *                               check existence with {@link #findWatchdog(Long)}
     */
    public void restart(Long id) {

        Watchdog wd = (Watchdog) this.registeredWachdogs.get(id);
        if (wd == null) {
            throw new IllegalStateException("Watchdog " + id + " ist not registered");
        }
        wd.restart();
    }

    /**
     * deactivates all Conditions of the Watchdog with the given id
     *
     * @throws IllegalStateException Watchdog does not exist,
     *                               check existence with {@link #findWatchdog(Long)}
     */
    public void deactivate(Long id) {

        Watchdog wd = (Watchdog) this.registeredWachdogs.get(id);
        if (wd == null) {
            throw new IllegalStateException("Watchdog " + id + " ist not registered");
        }
        wd.deactivate();
    }

    /**
     * activates all Conditions of the Watchdog with the given id
     *
     * @throws IllegalStateException Watchdog does not exist,
     *                               check existence with {@link #findWatchdog(Long)}
     */
    public void activate(Long id) {

        Watchdog wd = (Watchdog) this.registeredWachdogs.get(id);
        if (wd == null) {
            throw new IllegalStateException("Watchdog " + id + " ist not registered");
        }
        wd.activate();
    }


}
