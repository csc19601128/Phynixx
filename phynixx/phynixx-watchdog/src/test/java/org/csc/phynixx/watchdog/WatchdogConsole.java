/**
 *
 */
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


import org.csc.phynixx.common.TestUtils;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zf4iks2
 */
public class WatchdogConsole {

    private WatchdogAware[] testers = new WatchdogAware[]{};

    private WatchdogAware startWatchDog() {
        // 2 timeouts are expected .....
        WatchdogAware tester = new WatchdogAware(10000, 2000);

        Thread testerThread = new Thread(tester);

        testerThread.start();

        return tester;
    }


    protected void renew(int count) {
        this.releaseAll();
        this.testers = new WatchdogAware[count];
        for (int i = 0; i < count; i++) {
            testers[i] = this.startWatchDog();
        }

    }


    /**
     * kills the master threads and expects the watchdogs to starve
     */
    private void releaseAll() {
        for (int i = 0; i < testers.length; i++) {
            testers[i].kill();
            WatchdogRegistry.getTheRegistry().shutdown(testers[i].getWatchdog().getId());
        }

        // release all references to release weakly references Watchdogs
        testers = new WatchdogAware[]{};


    }

    private void printWatchdogManagementInfo() {
        System.out.println("watchdog management interval =" + WatchdogRegistry.getWatchdogManagementInterval() + "\n");
        WatchdogInfo[] wdInfos = WatchdogRegistry.getTheRegistry().getManagementWatchdogsInfo();

        for (int i = 0; i < wdInfos.length; i++) {
            System.out.println(wdInfos[i]);
        }
    }


    private static void gc() {
        Long l = new Long(3);
        l = null;
        List byteArrays = new ArrayList();
        SoftReference sref = new SoftReference(new char[]{'1'});
        System.out.print("GC initialized ");
        while (sref.get() != null) {
            //System.out.println("Available Heap Size= "+Runtime.getRuntime().freeMemory()+" of "+Runtime.getRuntime().totalMemory());
            byte[] b = new byte[1024 * 1024];
            byteArrays.add(b);
            System.out.print(".");
            System.gc();
            try {
                Thread.currentThread().sleep(10);
            } catch (InterruptedException e) {
            }
        }

        System.out.print("finished");
    }

    private static String usage() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(" u   - usage\n");
        buffer.append(" gc - garbage collection \n");
        buffer.append(" q   - quit\n");
        buffer.append(" r   - restart the registry\n");
        buffer.append(" rl  - releases all tester threads\n");
        buffer.append(" st  - stops all watchdogs\n");
        buffer.append(" sh  - shutdown the registry\n");
        buffer.append(" i   - info \n");
        buffer.append(" mg  - shows the wachdog managemen info\n");
        buffer.append(" mg<number>  - sets the wachdog_management_interval\n");
        buffer.append(" r<number>   - restart watchdog with ID=<number>\n");
        buffer.append(" sh<number>  - shutdown watchdog with ID=<number>\n");
        buffer.append(" st<number>  - stops watchdog with ID=<number>\n");
        buffer.append(" i<number>   - info of watchdog with ID=<number>\n");
        buffer.append(" n<number>   - shutdown and starts <number> new processes\n");

        return buffer.toString();
    }

    public void printWatchdogInfos() {
        int count = WatchdogRegistry.getTheRegistry().getCountWatchdogs();
        System.out.println("WatchdogRegistry contains " + count + " Watchdogs");
        String[][] infos = WatchdogRegistry.getTheRegistry().showWatchdogInfos();
        for (int i = 0; i < infos.length; i++) {
            String[] info = infos[i];
            for (int j = 0; j < info.length; j++) {
                System.out.println(info[j]);
            }

        }

    }

    public void printWatchdogInfos(Long id) {
        IWatchdog wd = WatchdogRegistry.getTheRegistry().findWatchdog(id);
        if (wd == null) {
            throw new IllegalStateException("Watchdog " + id + " ist not registered");
        }

        System.out.println(wd.toString());

    }


    public void control() throws Exception {

        System.out.println(usage());
        while (true) {
            try {
                String cmd = this.readIn();
                if (cmd.equals("u")) {
                    System.out.println(usage());
                } else if (cmd.equals("q")) {
                    this.printWatchdogInfos();
                    System.exit(1);
                } else if (cmd.equals("rl")) {
                    this.releaseAll();
                } else if (cmd.equals("r")) {
                    WatchdogRegistry.getTheRegistry().restart();
                } else if (cmd.equals("sh")) {
                    WatchdogRegistry.getTheRegistry().shutdown();
                } else if (cmd.equals("st")) {
                    WatchdogRegistry.getTheRegistry().stop();
                } else if (cmd.equals("i")) {
                    this.printWatchdogInfos();
                } else if (cmd.equals("gc")) {
                    WatchdogConsole.gc();
                } else if (cmd.equals("mg")) {
                    printWatchdogManagementInfo();
                } else if (cmd.startsWith("sh")) {
                    WatchdogRegistry.getTheRegistry().shutdown(extractNumber(cmd));
                } else if (cmd.startsWith("r")) {
                    WatchdogRegistry.getTheRegistry().restart(extractNumber(cmd));
                } else if (cmd.startsWith("st")) {
                    WatchdogRegistry.getTheRegistry().stop(extractNumber(cmd));
                } else if (cmd.startsWith("i")) {
                    this.printWatchdogInfos(extractNumber(cmd));
                } else if (cmd.startsWith("n")) {
                    this.renew(extractNumber(cmd).intValue());
                } else if (cmd.startsWith("mg")) {
                    WatchdogRegistry.setWatchdogManagementInterval(extractNumber(cmd).longValue());
                } else {
                    System.out.println("Command " + cmd + " is not recognized");
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }

        }
    }

    private Long extractNumber(String cmd) {
        int i = 0;
        for (i = 0; i < cmd.length(); i++) {
            if (Character.isDigit(cmd.charAt(i))) {
                break;
            }
        }
        return new Long(Long.parseLong(cmd.substring(i, cmd.length())));
    }

    private String readIn() throws IOException {

        char c = (char) System.in.read();
        StringBuffer buffer = new StringBuffer();
        buffer.append(c);

        while (System.in.available() > 0) {
            c = (char) System.in.read();
            if (Character.isLetter(c) || Character.isDigit(c)) {
                buffer.append(c);
            }
        }
        // System.out.println("Read "+ buffer.toString());
        return buffer.toString();
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {


        try {

            System.setProperty("log4j_level", "INFO");
            TestUtils.configureLogging();

            WatchdogConsole bean = new WatchdogConsole();

            //bean.renew(5);

            bean.control();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
