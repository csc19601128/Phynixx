package org.csc.phynixx.loggersystem.howl;

/*
 * #%L
 * phynixx-howl
 * %%
 * Copyright (C) 2014 csc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.loggersystem.ILogger;
import org.csc.phynixx.loggersystem.ILoggerFactory;
import org.objectweb.howl.log.Configuration;
import org.objectweb.howl.log.HowlLogger;
import org.objectweb.howl.log.LogConfigurationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HowlLoggerFactory implements ILoggerFactory {

	private Properties howlConfigProps= null; 	
	
	private File directory= null;
	
	private String loggerSystemName; 
	
	public HowlLoggerFactory(String loggerSystemName, Properties howlConfigProps) {
		
		this.howlConfigProps = howlConfigProps;
		this.loggerSystemName= loggerSystemName;
		this.directory= new File(howlConfigProps.getProperty("logFileDir"));
		if( this.directory.exists() && ! this.directory.isDirectory()) {
			throw new IllegalArgumentException("Property logFileDir has to be a directory");
		}
	}

   public HowlLoggerFactory(String loggerSystemName) throws Exception {
	   this(loggerSystemName, loadHowlConfig(loggerSystemName) );
	}



	public ILogger instanciateLogger(String loggerName) throws IOException {
		Configuration cfg=null; 
		synchronized(this.howlConfigProps) {
			try {
				cfg= new Configuration(new Properties(this.howlConfigProps));
				cfg.setLogFileName(loggerName);
			} catch (LogConfigurationException e) {
			throw new DelegatedRuntimeException(e);
			}
		}
		return new HowlLogger(cfg);
	}



	public File getLoggingDirectory() {
		return this.directory; 
	}
	
	private static Properties loadHowlConfig(String loggerSystemName) throws Exception
	{
        Properties systEnv= new Properties(); 
        systEnv.putAll(System.getProperties());
        
        String propName= "howl."+loggerSystemName+".config";
	 
        if( systEnv.getProperty(propName) !=null && !systEnv.getProperty(propName).equals("")) {
	        Properties tmpProp = new Properties();
	        FileInputStream inStr = new FileInputStream(systEnv.getProperty(propName));	        
	        tmpProp.load(inStr);
	        systEnv.putAll(tmpProp);
        }
        
        Properties configProps= new Properties(); 
		String configParam = null;
        configParam = systEnv.getProperty ("howl.log.listConfig", "false");
        configProps.put("listConfig", configParam);
        configParam = systEnv.getProperty ("howl.log.bufferSize", "4");
        configProps.put("bufferSize", configParam);
        configParam = systEnv.getProperty ("howl.log.minBuffers", "16");
        configProps.put("minBuffers", configParam);
        configParam = systEnv.getProperty ("howl.log.maxBuffers", "16");
        configProps.put("maxBuffers", configParam);
        configParam = systEnv.getProperty ("howl.log.maxBlocksPerFile", "200");
        configProps.put("maxBlocksPerFile", configParam);
        configParam = systEnv.getProperty ("howl.log.logFileDir", systEnv.getProperty("java.io.tmpdir"));
        configProps.put("logFileDir", configParam);
        configParam = systEnv.getProperty ("howl.log.maxLogFiles", "2");
        configProps.put("maxLogFiles", configParam);
        
        return configProps;
	}


	
}
