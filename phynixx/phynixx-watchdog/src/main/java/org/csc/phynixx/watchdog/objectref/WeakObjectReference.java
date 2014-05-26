package org.csc.phynixx.watchdog.objectref;

import java.lang.ref.WeakReference;
	

	/**
	 * the watchdog references the conditions weakly. If any condition is not referenced 
	 * by anybody but the watchdog it shut be handed to gc.
	 * 
	 * @author zf4iks2
	 *
	 */

public class WeakObjectReference extends WeakReference implements IObjectReference
{

	private String description= null; 

	public WeakObjectReference(Object objectRef) {
		super(objectRef);
		this.description= ( objectRef==null)?"NULL":objectRef.toString();
	}

	
	
	public String getObjectDescription() {
		return this.description;
	}


	public boolean isWeakReference() {
		return true;
	}

	
	public boolean isStale() {
		return this.get()==null; 
	}

	public boolean equals(Object obj) {
		Object objRef= this.get();
		if( objRef==null) {
			return obj==null; 
		}			
		return objRef.equals(obj);
	}

	public int hashCode() {
		Object objRef= this.get();
		if( objRef==null) {
			return "".hashCode(); 
		}			
		return objRef.hashCode();
	}

	public String toString() {
		Object objRef= this.get();
		if( objRef==null) {
			return ""; 
		}			
		return objRef.toString();
	}
	
}