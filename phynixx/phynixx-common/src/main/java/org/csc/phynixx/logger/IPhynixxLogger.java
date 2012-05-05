package org.csc.phynixx.logger;

public interface IPhynixxLogger {

	public  void debug(Object o);

	public  void debug(Object o, Throwable t);

	public  void info(Object o);

	public  void info(Object o, Throwable t);

	public  void warn(Object o);

	public  void warn(Object o, Throwable t);

	public  void error(Object o);

	public  void error(Object o, Throwable t);

	public  void fatal(Object o);

	public  void fatal(Object o, Throwable t);
	
	public boolean isInfoEnabled();
	
	public boolean isDebugEnabled(); 

}