package org.csc.phynixx.watchdog.objectref;

public class ObjectReference implements IObjectReference {
	
	private Object objectRef= null; 
	

	public ObjectReference(Object objectRef) {
		super();
		this.objectRef = objectRef;
	}

	


	public String getObjectDescription() {

		return objectRef==null?"NULL":objectRef.toString(); 
	}

	

	public boolean isWeakReference() {
		return false;
	}


	public Object get() {
		return objectRef; 
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


	public boolean isStale() {
		return false;
	}

}
