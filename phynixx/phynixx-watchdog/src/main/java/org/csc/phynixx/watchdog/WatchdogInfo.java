package org.csc.phynixx.watchdog;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;


public class WatchdogInfo implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4208724399286983526L;

	private String info= null; 
	
	private String id= null; 
	
	private String[] conditions; 
	
	public WatchdogInfo(IWatchdog wd) {
		this.id= wd.getId().toString(); 
		this.setConditions(wd);
		this.setInfo(wd);
	}


	private void setInfo(IWatchdog wd) 
	{
		
		this.info= wd.getWatchdogInfo();
		
	}
	
	private void setConditions(IWatchdog wd) {
		
		
		this.conditions= wd.getConditionInfos(); 
			
	}


	public String getWatchdogInfo() {
		return info;
	}


	public String[] getConditions() {
		return conditions;
	}

	

	public String[] getWatchdogInfos() 
	{
		String[] conds= this.getConditions();
		String[] infos= new String[conds.length+1];
		
		infos[0]=this.getWatchdogInfo(); 
	
		for (int i = 0; i < conds.length; i++) {
			infos[i+1]="     "+conds[i];
		}
		return infos;
		
	}
	public String toString() 
	{
		StringBuffer buffer= new StringBuffer();
		buffer.append("Watchdog - executing Thread="+this.getWatchdogInfo()).append("\n");
		String[] conds= this.getConditions();
		for (int i = 0; i < conds.length; i++) {
			buffer.append("     ").append(conds[i]).append("\n");
		}
		return buffer.toString();
	}
	
	
	
	

}
