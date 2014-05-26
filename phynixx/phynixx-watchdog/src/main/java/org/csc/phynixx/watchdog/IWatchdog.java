package org.csc.phynixx.watchdog;

import java.util.Set;

public interface IWatchdog {

	Long getId();

	long getCheckInterval();

	void registerCondition(IWatchedCondition cond);

	int getCountRegisteredConditions();

	Set getAliveConditions();

	void unregisterCondition(IWatchedCondition cond);

	boolean isKilled();
	
	boolean isStale(); 

	void activate();

	void deactivate();

	/**
	 * 
	 * if the watchdog watches only conditions that are irrelevant it is marked as useles
	 * and it can be shut down.
	 * @return
	 */
	boolean isUseless();

	boolean isAlive();

	String[] getConditionInfos();

	String getWatchdogInfo();
	
	

}