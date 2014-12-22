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


import junit.framework.TestCase;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;

import java.util.Set;

public class WatchdogTest extends TestCase {

    private final IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    protected void setUp() throws Exception {
        super.setUp();

        WatchdogRegistry.setWatchdogManagementInterval(1000);

        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        WatchdogRegistry.getTheRegistry().shutdown();
        WatchdogRegistry.getTheRegistry().restart();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        WatchdogRegistry.getTheRegistry().shutdown();
    }

    /**
     * TestCasees
     * 1) watchdog is explicitly killed and deregistered
     * 2) the condition is set to 'useless' and the watchdog becomes useless to. Restarting the Watchdogregistry
     * deregisters this watchdog ...
     *
     * @throws Exception
     */
    public void testWatchdog() throws Exception {
        long idleTime = 1000;
        long timeout = 200;

        // 2 timeouts are expected .....
        WatchdogAware tester = new WatchdogAware(idleTime, timeout);

        Thread testerThread = new Thread(tester);

        testerThread.start();
        // Wait until the theard is up ...
        sleep(10);

        IWatchdog watchdog = tester.getWatchdog();
        Set aliveConditions = watchdog.getAliveConditions();
        TestCase.assertEquals(1, watchdog.getCountRegisteredConditions());

        //System.out.println(watchdog);

        sleep(tester.getIdleTime() + 100);
        TestCase.assertEquals(1, watchdog.getCountRegisteredConditions());
        TestCase.assertTrue(tester.getConditionFailedCounter() >= 4);
        // System.out.println("#Timeouts =" +tester.getConditionFailedCounter());

        TestCase.assertTrue(WatchdogRegistry.getTheRegistry().resolveWatchdogId(watchdog.getId()) != null);
        tester.kill();
        // wait to the killed thread ...
        int counter = 0;
        // kill the watchdog ..
        WatchdogRegistry.getTheRegistry().shutdown(watchdog.getId());

        while (counter < 10 && testerThread.isAlive()) {
            sleep(tester.getIdleTime());
            counter++;
        }
        // check that the watchdog is killed too
        TestCase.assertTrue(WatchdogRegistry.getTheRegistry().resolveWatchdogId(watchdog.getId()) == null);

        /**
         * testcase 2 condition is marked as useless
         */
        tester = new WatchdogAware(idleTime, timeout);
        testerThread = new Thread(tester);
        testerThread.start();
        // Wait until the thread is up ...
        sleep(10);

        watchdog = tester.getWatchdog();
        TestCase.assertTrue(WatchdogRegistry.getTheRegistry().resolveWatchdogId(watchdog.getId()) != null);

        // mark the condition as useless
        tester.setUseless(true);
        //wait to the clear out of the watchdog registry
        sleep(10000);

        System.out.println("WD=" + WatchdogRegistry.getTheRegistry().resolveWatchdogId(watchdog.getId()));
        TestCase.assertTrue(WatchdogRegistry.getTheRegistry().resolveWatchdogId(watchdog.getId()) == null);
    }

    /**
     * TestCasees
     * 1) watchdog is explicitly killed and deregistered
     * 2) the condition is set to 'useless' and the watchdog becomes useless to. Restarting the Watchdogregistry
     * deregisters this watchdog ...
     *
     * @throws Exception
     */
    public void testLoadOnWatchdogs() throws Exception {
        long idleTime = 1000;
        long timeout = 200;

        int LOAD = 30;

        WatchdogAware[] testers = new WatchdogAware[LOAD];

        for (int i = 0; i < LOAD; i++) {
            testers[i] = new WatchdogAware(idleTime, timeout);
            new Thread(testers[i]).start();
        }
        // Wait until the threads are up ...
        sleep(10);


        TestCase.assertEquals(LOAD, WatchdogRegistry.getTheRegistry().getCountWatchdogs());


        sleep(idleTime + 100);
        for (int i = 0; i < LOAD; i++) {
            testers[i].kill();
            testers[i].setUseless(true);
        }
        sleep(idleTime + 100);

        //wait to the clear out of the watchdog registry
        sleep(10000);

        TestCase.assertEquals(0, WatchdogRegistry.getTheRegistry().getCountWatchdogs());


    }

    public void testWatchTheWatchdog() throws Exception {
        // 2 timeouts are expected .....
        WatchdogAware tester = new WatchdogAware(100, 50);
        Thread testerThread = new Thread(tester);
        testerThread.start();
        // Wait until the thread is up ...
        sleep(10);

        IWatchdog wd = WatchdogRegistry.getTheRegistry().resolveWatchdogId(tester.getWatchdog().getId());

        TestCase.assertEquals(WatchdogRegistry.OK, WatchdogRegistry.getTheRegistry().getManagementWatchdogsState());

        WatchdogRegistry.getTheRegistry().shutdown();

        TestCase.assertFalse(WatchdogRegistry.OK.equals(WatchdogRegistry.getTheRegistry().getManagementWatchdogsState()));

        // Registry is shutdown and WD is stale (not existent anymore)
        TestCase.assertTrue(wd.isStale());
        tester.kill();


        // restart the system ....
        WatchdogRegistry.getTheRegistry().restart();

        // create new tester s...
        tester = new WatchdogAware(100, 50);
        testerThread = new Thread(tester);
        testerThread.start();
        // Wait until the thread is up ...
        sleep(10);

        wd = WatchdogRegistry.getTheRegistry().resolveWatchdogId(tester.getWatchdog().getId());
        TestCase.assertTrue(wd.isAlive());
        TestCase.assertEquals(WatchdogRegistry.OK, WatchdogRegistry.getTheRegistry().getManagementWatchdogsState());


        long mgmtInterval = WatchdogRegistry.getWatchdogManagementInterval();
        WatchdogRegistry.setWatchdogManagementInterval(mgmtInterval / 4);

        TestCase.assertEquals(mgmtInterval / 4, WatchdogRegistry.getWatchdogManagementInterval());


    }


    public static void sleep(long msecs) {
        long start = System.currentTimeMillis();
        long waiting = msecs;
        while (waiting > 0) {
            try {
                Thread.currentThread().sleep(waiting);
            } catch (InterruptedException e) {
            } finally {
                waiting = msecs - (System.currentTimeMillis() - start);
            }
        }
    }


}
