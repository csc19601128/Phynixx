/**
 * SimpleMonitorableMBean.java
 *
 * @author Created by Omnicore CodeGuide
 */


package com.builder.uk.watchme;

public interface WatchMeBeanMBean
{
	public int getCount();
	public String getMsg();
	public void setMsg(String msg);
	public void reset();
}

