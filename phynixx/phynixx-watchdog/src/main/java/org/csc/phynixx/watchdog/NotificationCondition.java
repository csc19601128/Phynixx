package org.csc.phynixx.watchdog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationCondition implements IWatchedCondition {

	
   public static interface IConditionNotifier {
	   
	   void notifyCondition(IWatchedCondition cond);
   }
   
   private List notifiers= new ArrayList();
   
   private boolean active= false; 
   
   private boolean useless= false; 
	
	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}

	public boolean checkCondition() {
		return true;
	}

	public void conditionViolated() {
		return; 
	}
	
	
	public void addNotifier(IConditionNotifier notifier)
	{
		if( !this.notifiers.contains(notifier)) {
			this.notifiers.add(notifier);
		}
	}
	
	
	protected void notifyCondition() {
		
		for (Iterator iterator = notifiers.iterator(); iterator.hasNext();) {
			IConditionNotifier notifier = (IConditionNotifier) iterator.next();
			notifier.notifyCondition(this);
		}
	}
	
	
	
	public String toString() {
		return "NotificationCondition ; has "+ this.notifiers.size()+" notifier (active="+this.isActive()+")"; 
	}

	protected void finalize() throws Throwable {
		System.out.println("Condition "+this.toString()+" is finalized");
		super.finalize();
	}

	public synchronized boolean isUseless() {
		return this.useless;
	}
	
	public synchronized void setUseless(boolean mode) {
		this.useless=mode;
	}


}
