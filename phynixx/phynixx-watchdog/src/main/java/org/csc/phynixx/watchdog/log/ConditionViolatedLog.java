package org.csc.phynixx.watchdog.log;

import org.csc.phynixx.watchdog.IWatchedCondition;

public class ConditionViolatedLog implements IWatchdogLog{
	
	private long timestamp= 0l;
	private String condition= null; 
	private String description=null; 

	public ConditionViolatedLog(IWatchedCondition condition) {
		super();
		this.timestamp= System.currentTimeMillis();
		this.condition= condition.toString();

		this.description= this.condition;
	}
	public ConditionViolatedLog(IWatchedCondition condition, String description) {
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
		return "ConditionViolated : "+this.getDescription();
	}
	
	
	
	
	

}
