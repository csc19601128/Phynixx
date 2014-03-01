package org.csc.phynixx.watchdog.jmx;

import org.csc.phynixx.watchdog.IWatchdog;
import org.csc.phynixx.watchdog.WatchdogInfo;
import org.csc.phynixx.watchdog.WatchdogRegistry;

public class WatchdogManagement implements WatchdogManagementMBean{

	public int getCountWatchDogs() throws Exception {		
		return WatchdogRegistry.getTheRegistry().getCountWatchdogs();
	}

	public void restart() throws Exception {

		WatchdogRegistry.getTheRegistry().restart();
	}
	
	public void stop() throws Exception {
		WatchdogRegistry.getTheRegistry().stop();
	}
	
	
	
	public String[] getWatchdogInfos() throws Exception {
		WatchdogInfo[] infos= WatchdogRegistry.getTheRegistry().getWatchdogInfos();
		String[] wds= new String[infos.length];
		for (int j = 0; j < infos.length; j++) {
			wds[j]= infos[j].getWatchdogInfo();
		}
		return wds;
	}

	

	public String[][] showWatchdogInfos() {
		return WatchdogRegistry.getTheRegistry().showWatchdogInfos();
	}
	
	public String[] showWatchdogInfo(long id) throws Exception
	{
		IWatchdog wd= WatchdogRegistry.getTheRegistry().resolveWatchdogId(new Long(id));
		if( wd==null) {
			throw new IllegalArgumentException("ID="+id+" could not be resolved -> no valid ID");
		}	
		return new WatchdogInfo(wd).getWatchdogInfos();
	}
	
	
	public void restart(long id) throws Exception {
		
		IWatchdog wd= WatchdogRegistry.getTheRegistry().resolveWatchdogId(new Long(id));
		if( wd==null) {
			throw new IllegalArgumentException("ID="+id+" could not be resolved -> no valid ID");
		}		
		WatchdogRegistry.getTheRegistry().restart(wd.getId());
		
		
	}
	
    public void stop(long id) throws Exception {
		
    	IWatchdog wd= WatchdogRegistry.getTheRegistry().resolveWatchdogId(new Long(id));
		if( wd==null) {
			throw new IllegalArgumentException("ID="+id+" could not be resolved -> no valid ID");
		}

		WatchdogRegistry.getTheRegistry().stop(wd.getId());
		
	}
    
    public void shutdown(long id) throws Exception {
		
    	IWatchdog wd= WatchdogRegistry.getTheRegistry().resolveWatchdogId(new Long(id));
		if( wd==null) {
			throw new IllegalArgumentException("ID="+id+" could not be resolved -> no valid ID");
		}

		WatchdogRegistry.getTheRegistry().shutdown(wd.getId());
		
	}
    
    public void activate(long id) throws Exception {
		
    	IWatchdog wd= WatchdogRegistry.getTheRegistry().resolveWatchdogId(new Long(id));
		if( wd==null) {
			throw new IllegalArgumentException("ID="+id+" could not be resolved -> no valid ID");
		}

		WatchdogRegistry.getTheRegistry().activate(wd.getId());
		
	}
	
   public void deactivate(long id) throws Exception {
		
    	IWatchdog wd= WatchdogRegistry.getTheRegistry().resolveWatchdogId(new Long(id));
		if( wd==null) {
			throw new IllegalArgumentException("ID="+id+" could not be resolved -> no valid ID");
		}

		WatchdogRegistry.getTheRegistry().deactivate(wd.getId());
		
	}
	
	
	

	

}
