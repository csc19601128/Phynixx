package org.csc.phynixx.watchdog.log;

import org.csc.phynixx.watchdog.IWatchedCondition;

public class CheckConditionFailedLog implements IWatchdogLog{
	
	private long timestamp= 0l;
	private String condition= null; 
	private String description=null; 

	public CheckConditionFailedLog(IWatchedCondition condition) {
		super();
		this.timestamp= System.currentTimeMillis();
		this.condition= condition.toString();

		this.description= condition!=null?condition.toString():"?";
	}
	public CheckConditionFailedLog(IWatchedCondition condition, String description) {
		super();
		this.timestamp= System.currentTimeMillis();
		this.condition= condition.toString();
		this.description= this.condition+" "+description;
		
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getCondition() {
		return condition;
	}

	public String getDescription() {
		return this.description;
	}
	public String toString() {
		return "CheckConditionFailed : "+this.getDescription();
	}
	

}
