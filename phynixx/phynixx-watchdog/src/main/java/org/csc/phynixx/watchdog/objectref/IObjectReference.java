package org.csc.phynixx.watchdog.objectref;

public interface IObjectReference {

	/**
	 * 
	 * @return the referenced Object
	 */
	Object get(); 
	
	
	boolean isWeakReference();
	
	
	String getObjectDescription();

	public boolean isStale();


	
}
