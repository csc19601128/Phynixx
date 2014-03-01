package org.csc.phynixx.watchdog.jmx;

import org.csc.phynixx.watchdog.WatchdogInfo;
import org.csc.phynixx.watchdog.WatchdogRegistry;

public class WatchTheWatchdogs implements WatchTheWatchdogsMBean{
	
	public String getState() {
		return WatchdogRegistry.getTheRegistry().getManagementWatchdogsState();
	}
	
	public String[][] showWatchdogInfos() throws Exception
	{
		WatchdogInfo[] infos= WatchdogRegistry.getTheRegistry().getManagementWatchdogsInfo();
			
		String[][] wds= new String[infos.length][];
		for (int j = 0; j < infos.length; j++) {
			wds[j]= infos[j].getWatchdogInfos();
		}
		return wds;
	}
	
	
	public void restart() throws Exception
	{
		WatchdogRegistry.getTheRegistry().restart();
	}

}
