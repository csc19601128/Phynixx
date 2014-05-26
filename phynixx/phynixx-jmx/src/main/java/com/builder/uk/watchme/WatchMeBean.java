/**
 * SimpleMonitorableBean.java
 *
 * @author Created by Omnicore CodeGuide
 */

package com.builder.uk.watchme;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class WatchMeBean implements Runnable,WatchMeBeanMBean
{
	private PropertyChangeSupport changes=new PropertyChangeSupport(this);
	
	int count;
	String msg;
	
	public WatchMeBean()
	{
		count=0;
		setMsg("Initialised");
	}
	
	public int getCount()
	{
		return count;
	}
	
	public void incCount()
	{
		int oldcount=count;
		count=count+1;
		changes.firePropertyChange("count",oldcount,count);
	}
	
	public void setMsg(String msg)
	{
		String oldmsg=this.msg;
		
		this.msg = msg;
		
		changes.firePropertyChange("msg",oldmsg,msg);
	}
	
	public String getMsg()
	{
		return msg;
	}

	public void run()
	{
		while(true)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {}
			
			incCount();
		}
	}
			
	public void reset()
	{
		int oldcount=count;
		count=0;
		changes.firePropertyChange("count",oldcount,count);
	}
		
	public void addPropertyChangeListener(PropertyChangeListener l)
	{
		changes.addPropertyChangeListener(l);
	}
	
    public void removePropertyChangeListener(PropertyChangeListener l)
	{
		changes.removePropertyChangeListener(l);
	}
}

