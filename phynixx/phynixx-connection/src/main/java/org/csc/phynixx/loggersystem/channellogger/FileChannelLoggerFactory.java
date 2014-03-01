package org.csc.phynixx.loggersystem.channellogger;

import java.io.File;
import java.io.IOException;

import org.csc.phynixx.loggersystem.ILogger;
import org.csc.phynixx.loggersystem.ILoggerFactory;

public class FileChannelLoggerFactory implements ILoggerFactory {

	private File directory= null; 
	private String loggerSystemName= null; 
	
	public FileChannelLoggerFactory(String loggerSystemName, String directoryName) {
		super();
		this.loggerSystemName = loggerSystemName;
		this.directory = new File(directoryName);
		if( this.directory.exists() && ! this.directory.isDirectory()) {
			throw new IllegalArgumentException("Argument 'directoryname' has to referece an existing directory");
		};
	}
	
	public FileChannelLoggerFactory(String loggerSystemName, File directory) {
		super();
		this.loggerSystemName = loggerSystemName;
		this.directory = directory;
		if( this.directory.exists() && ! this.directory.isDirectory()) {
			throw new IllegalArgumentException("Argument 'directory' has to be an existing directory");
		};
	}

	public File getLoggingDirectory() {
		return directory;
	}

	public ILogger instanciateLogger(String loggerName) throws IOException {
	    return new FileChannelLogger(loggerName,this.directory);		
	}

}
