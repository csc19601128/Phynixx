package org.csc.phynixx.watchdog;

import java.util.Set;

public class WatchdogReference implements IWatchdog{
	
	private Long id= null;

	public WatchdogReference(Long id) {
		super();
		this.id = id;
	} 
	

	public WatchdogReference(IWatchdog wd) {
		this(wd.getId());
	} 
	
	public Long getId() {
		return id;
	}


	Watchdog getWatchdog() {
		Watchdog wd= 
			WatchdogRegistry.getTheRegistry().findWatchdog(id);
		if( wd==null) {
			throw new IllegalStateException("Watchdog is stale and does not exist any longer");
		}	
		return wd;
	}

	public boolean isStale() {
		return WatchdogRegistry.getTheRegistry().findWatchdog(id) ==null;
	}


	public void activate() 
	{			
		this.getWatchdog().activate();
		
	}


	public void deactivate() {
		this.getWatchdog().deactivate();
		
	}


	public Set getAliveConditions() {
		return this.getWatchdog().getAliveConditions();
	}


	public long getCheckInterval() {
		return this.getWatchdog().getCheckInterval();
	}


	public String[] getConditionInfos() {
		return this.getWatchdog().getConditionInfos();
	}


	public int getCountRegisteredConditions() {
		return this.getWatchdog().getCountRegisteredConditions();
	}


	public String getWatchdogInfo() {
		return this.getWatchdog().getWatchdogInfo();
	}


	public boolean isAlive() {
		return this.getWatchdog().isAlive();
	}


	public boolean isKilled() {
		return this.getWatchdog().isKilled();
	}


	public boolean isUseless() {
		return this.getWatchdog().isUseless();
	}


	public void registerCondition(IWatchedCondition cond) {
		this.getWatchdog().registerCondition(cond);
		
	}


	public void unregisterCondition(IWatchedCondition cond) {
		this.getWatchdog().unregisterCondition(cond);
	}	
	
}
