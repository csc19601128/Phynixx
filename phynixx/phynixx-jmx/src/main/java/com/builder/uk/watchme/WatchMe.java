/**
 * Pure Java JFC (Swing 1.1) application.
 * This application realizes a windowing application.
 *
 * This file was automatically generated by
 * Omnicore CodeGuide.
 */

package com.builder.uk.watchme;

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


import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class WatchMe {

    public static void main(String[] args) {

        WatchMe watchMe = new WatchMe();
    }

    WatchMeBean smb;

    public WatchMe() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        smb = new WatchMeBean();
        Thread t = new Thread(smb);
        t.start();

        try {
            ObjectName myname = new ObjectName("WatchMeBean:name=watchme");
            mbs.registerMBean(smb, myname);
        } catch (Exception e) {
            e.printStackTrace();
        }

        WatchMeFrame watchMeFrame = new WatchMeFrame();
        watchMeFrame.setWatchMeBean(smb);

        watchMeFrame.pack();
        watchMeFrame.setVisible(true);
    }

}

