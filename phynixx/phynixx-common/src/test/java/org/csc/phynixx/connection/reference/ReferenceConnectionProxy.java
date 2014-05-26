package org.csc.phynixx.connection.reference;

import org.csc.phynixx.connection.PhynixxConnectionProxyAdapter;
import org.csc.phynixx.connection.IPhynixxConnectionProxy;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;


public class ReferenceConnectionProxy extends PhynixxConnectionProxyAdapter implements 
																	IPhynixxConnectionProxy,IReferenceConnection {


	
	protected IPhynixxConnectionProxy getObservableProxy() {
		return this;
	}



	public Object getId() {
		if( this.getConnection()!=null) {
			return ((IReferenceConnection)this.getConnection()).getId();
		}
		return null; 
	}

	public boolean equals(Object obj) 
	{
		if( obj ==null || !(obj instanceof ReferenceConnectionProxy)) {
			return false; 
		}
		
		return super.equals(obj);
		
	}

	public int hashCode() {
		if( this.getConnection()!=null) {
			return this.getConnection().hashCode();
		}
		return 0; 
	}

	public String toString() {
		if( this.getConnection()!=null) {
			return this.getConnection().toString();
		}
		return "unbound"; 
	}
	

	public int getCounter() {
		if( this.getConnection()!=null) {
			return ((IReferenceConnection)this.getConnection()).getCounter();
		}
		return -1; 
	}

	public void incCounter(int inc) {
		if( this.getConnection()!=null) {
			this.fireConnectionRequiresTransaction();
			((IReferenceConnection)this.getConnection()).incCounter(inc);
		}
		
	}


	public void setInitialCounter(int value) {
		if( this.getConnection()!=null) {
			this.fireConnectionRequiresTransaction();
			((IReferenceConnection)this.getConnection()).setInitialCounter(value);
		}
		
	}
	
}
