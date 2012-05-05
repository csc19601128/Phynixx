package org.csc.phynixx.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;

public class TestUtils {
	
	public static void sleep(long msecs ) 
	{
		long start= System.currentTimeMillis();
		long waiting= msecs;
		while( waiting > 0) {
			try {
				Thread.currentThread().sleep(waiting);
			} catch (InterruptedException e) {
			} finally {
				waiting= msecs - (System.currentTimeMillis()-start); 
			}
		}
	}
	
	
	public static void configureLogging() 
	{			
		Set validLogLevels= new HashSet();
		validLogLevels.add("DEBUG");
		validLogLevels.add("INFO");
		validLogLevels.add("WARNING");
		validLogLevels.add("ERROR");
		validLogLevels.add("FATAL");

		Properties log4jProps= new Properties(); 
		InputStream io= Thread.currentThread().getContextClassLoader().getResourceAsStream("log4j.properties");
		if(io==null) {
			throw new IllegalStateException("log4j.properties not found in ClassPath for testing ");
		}
		try {
			log4jProps.load(io);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Reinitialize the logger and substitute any environment pros
		Properties props= System.getProperties();
		
		// copy all props beginning with log4j
		Iterator iter= props.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry entry= (Map.Entry) iter.next();
			String propName= (String)entry.getKey();
			if( propName.startsWith("log4j")) {
				log4jProps.put(entry.getKey(), entry.getValue());
			}
		}
		
		// check if log4j.level is set correctly ; else set the Default to INFO
		String level= log4jProps.getProperty("log4j_level");
		if(level==null || !validLogLevels.contains(level)) {
			level="ERROR";
			log4jProps.setProperty("log4j_level", level);
		}
		
		org.apache.log4j.LogManager.resetConfiguration();
		
		PropertyConfigurator.configure(log4jProps);
		
	}
	
}
