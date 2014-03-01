package org.csc.phynixx.watchdog;

public interface ITimeoutCondition extends IWatchedCondition{

	public abstract long getTimeout();

	public abstract void resetCondition();

	public abstract void resetCondition(long timeout);

}