package org.csc.phynixx.loggersystem.logger.channellogger;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;

public class FileChannelDataLoggerStatistics {
	
	private static final IPhynixxLogger LOG = PhynixxLogManager
			.getLogger(FileChannelDataLoggerStatistics.class);

	static class Statistics {
		
		final Date createTime;
		Date closeTime;
		Date destroyTime=null;
		public Statistics(Date createTime) {
			super();
			this.createTime = createTime;
		}
		Date getDestroyTime() {
			return destroyTime;
		}
		void setDestroyTime(Date destroyTime) {
			this.destroyTime = destroyTime;
		}
		
		
		Date getCloseTime() {
			return closeTime;
		}
		void setCloseTime(Date closeTime) {
			this.closeTime = closeTime;
		}
		Date getCreateTime() {
			return createTime;
		}
		
		boolean isDestroyed() {
			return destroyTime!=null;
		}		
		boolean isClosed() {
			return closeTime!=null;
		}
		
		@Override
		public String toString() {
			StringBuffer buff = new StringBuffer("[createTime=").append(DateFormat.getDateTimeInstance(2, 2, Locale.GERMAN).format(createTime));

			if(closeTime!=null) {
				buff.append(", closeTime=").append(DateFormat.getDateTimeInstance(2, 2, Locale.GERMAN).format(closeTime));
			} 
			
			if(destroyTime!=null) {
				buff.append(", destroyTime=").append(DateFormat.getDateTimeInstance(2, 2, Locale.GERMAN).format(destroyTime));
			} 
			buff.append("]");
			return buff.toString();
		}
		
	}
	
	private static Map<String, List<Statistics>> statistics= new HashMap<String, List<Statistics>>();
	
	
	static synchronized void notifyCreate(String filename) {
		List<Statistics> stats=statistics.get(filename);
		if( !statistics.containsKey(filename)) {
			stats = new ArrayList<Statistics>();
			statistics.put(filename, stats);
		}		
		Statistics stat= new Statistics(new Date());
		stats.add(stat);
	}
	
	static synchronized void notifyDestroy(String filename) {		
		List<Statistics> stats=statistics.get(filename);
		if(stats==null) {
			notifyCreate(filename);		
			stats=statistics.get(filename);
		}
		
		Statistics stat = stats.get(stats.size()-1);
		if(stat.isDestroyed() ) {
			LOG.warn("Logger "+filename+" already destroyed");
			return;
		}
		
		// hole letzten Eintrag .....
		stat.setDestroyTime(new Date());
		
	}
	static synchronized void notifyClose(String filename) {		
		List<Statistics> stats=statistics.get(filename);
		if(stats==null) {
			notifyCreate(filename);		
			stats=statistics.get(filename);
		}
		
		Statistics stat = stats.get(stats.size()-1);
		if(stat.isDestroyed() ) {
			LOG.warn("Logger "+filename+" already destroyed");
			return;
		}
		
		if(stat.isClosed()) {
			LOG.warn("Logger "+filename+" already closed");
			return;
		}
		// hole letzten Eintrag .....
		stat.setCloseTime(new Date());
		
	}
	
	
	public static synchronized String printStatistics() {
		StringBuffer buff= new StringBuffer("FileChannelLoggerStatistics [");
		for (Map.Entry<String,List<Statistics>> entry : statistics.entrySet()) {
			buff.append("\n    ").append(entry.getKey()).append(" ").append(entry.getValue());			
		}
		
		return buff.toString();
	}
	
	
}
