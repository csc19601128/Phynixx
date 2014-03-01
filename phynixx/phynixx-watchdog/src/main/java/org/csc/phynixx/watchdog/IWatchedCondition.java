package org.csc.phynixx.watchdog;

public interface IWatchedCondition {
	
	public boolean checkCondition(); 
	
	public void conditionViolated() ;
	
	public void setActive(boolean active);
	
	public boolean isActive();
	
	
	/**
	 * indicates that the condition isn't needed any longer ...
	 * @return
	 */
	public boolean isUseless();
	
	

}
