package org.csc.phynixx.evaluation.howl;

import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.objectweb.howl.log.LogEventListener;

public class TestLogEventListener implements LogEventListener {

	private IPhynixxLogger log= PhynixxLogManager.getLogger(this.getClass());

	public boolean isLoggable(int level) {
		return false;
	}

	public void log(int level, String message) {
		return;

	}

	public void log(int level, String message, Throwable thrown) {return;

	}

	public int count= 0; 
	public void logOverflowNotification(long logkey) {
		log.info("logOverflowNotification logKey="+logkey+" count="+count);
		count++;
	}

}
