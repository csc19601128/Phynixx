package org.csc.phynixx.watchdog.jmx;




public interface WatchdogManagementMBean {
	
	int getCountWatchDogs() throws Exception; 
	
	void restart() throws Exception;

	public void stop() throws Exception;

	public String[] getWatchdogInfos() throws Exception;
	
	

	public String[][] showWatchdogInfos() throws Exception;


	public String[] showWatchdogInfo(long id) throws Exception;


	public void restart(long id) throws Exception;

	public void stop(long id) throws Exception;
	
	public void shutdown(long id) throws Exception;

	public void activate(long id) throws Exception;

	public void deactivate(long id) throws Exception;


	
}
