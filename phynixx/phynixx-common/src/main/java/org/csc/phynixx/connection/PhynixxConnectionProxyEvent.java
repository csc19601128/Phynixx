package org.csc.phynixx.connection;

import java.util.EventObject;


public class PhynixxConnectionProxyEvent extends EventObject implements	IPhynixxConnectionProxyEvent {

	private Exception exception= null; 

	/**
	 * 
	 */
	private static final long serialVersionUID = 2146374246818609618L;

	public PhynixxConnectionProxyEvent(IPhynixxConnectionHandle source) {
		super(source);
	}
	


	public PhynixxConnectionProxyEvent(IPhynixxConnectionHandle source, Exception exception) {
		super(source);
		this.exception = exception;
	}



	public Exception getException() {
		return exception;
	}

	public IPhynixxConnectionProxy getConnectionProxy() {
		return (IPhynixxConnectionProxy)this.getSource();
	}



	public String toString() {
		return this.getConnectionProxy()+( (getException()!=null)?" Exception:: "+getException():""); 
	}

	
	
	
	

}
