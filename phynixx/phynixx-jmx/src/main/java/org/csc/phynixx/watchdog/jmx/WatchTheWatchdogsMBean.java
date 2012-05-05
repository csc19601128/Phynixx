package org.csc.phynixx.watchdog.jmx;


public interface WatchTheWatchdogsMBean {

	public String getState();

	public String[][] showWatchdogInfos() throws Exception;

	public void restart() throws Exception;

}
