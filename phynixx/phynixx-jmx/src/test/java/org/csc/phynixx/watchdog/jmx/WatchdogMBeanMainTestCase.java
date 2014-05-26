/**
 * 
 */
package org.csc.phynixx.watchdog.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.csc.phynixx.watchdog.WatchdogConsole;

/**
 * @author zf4iks2
 *
 */
public class WatchdogMBeanMainTestCase extends WatchdogConsole
{
	
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
	public static void main(String[] args)  {
		
		try {

			
			WatchdogMBeanMainTestCase bean= new WatchdogMBeanMainTestCase();

			bean.registerMBeans();
			
			//bean.renew(5);

			bean.control();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}


	

}
