package org.csc.phynixx.loggersystem;

import java.io.File;

class LogFileCollector {
	
	public interface ICollectorCallback {		
		void match(File file, LogFilenameMatcher.LogFilenameParts parts);		
	}
	
	private LogFilenameMatcher logFileMatcher= null;
	
	
	public LogFileCollector(LogFilenameMatcher logFileMatcher, File startDirectory, ICollectorCallback cb) {
		super();
		this.logFileMatcher= logFileMatcher;
		if( startDirectory.exists() && startDirectory.isDirectory()) {
			this.iterateDirectory(startDirectory,cb);
		}
	} 	
	

	private void iterateDirectory(File startDirectory, ICollectorCallback cb) 
	{
		File[] files= startDirectory.listFiles();
		if( files==null || files.length==0) {
			return ;
		}
		for(int i=0;i< files.length;i++) {
			if( files[i].isDirectory()) {
				iterateDirectory(files[i],cb);
			} else {
				LogFilenameMatcher.LogFilenameParts parts= 
					      this.logFileMatcher.matches(files[i].getName());
				if (parts !=null) {
					cb.match(files[i], parts);
				}
			}
		}
		
	}
	
	
	
	

}
