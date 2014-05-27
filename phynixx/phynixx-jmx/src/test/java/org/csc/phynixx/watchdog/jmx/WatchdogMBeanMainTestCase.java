/**
 *
 */
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


import org.csc.phynixx.watchdog.WatchdogConsole;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * @author zf4iks2
 */
public class WatchdogMBeanMainTestCase extends WatchdogConsole {

    protected static void registerMBeans() throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("org.csc.phynixx.watchdog.jmx:type=WatchDogManagement");
        WatchdogManagement wdManagementBean = new WatchdogManagement();
        mbs.registerMBean(wdManagementBean, name);

        name = new ObjectName("org.csc.phynixx.watchdog.jmx:type=WatchTheWatchdogs");
        WatchTheWatchdogs mbean = new WatchTheWatchdogs();
        mbs.registerMBean(mbean, name);


    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {

        try {


            WatchdogMBeanMainTestCase bean = new WatchdogMBeanMainTestCase();

            bean.registerMBeans();

            //bean.renew(5);

            bean.control();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
