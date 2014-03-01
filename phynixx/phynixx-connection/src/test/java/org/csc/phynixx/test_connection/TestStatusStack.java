package org.csc.phynixx.test_connection;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class TestStatusStack {
	
	private Object id= null;
	
	private Set statusStack= new TreeSet(); 
	
	public TestStatusStack(Object id) {
		super();
		this.id = id;
	} 
	
	public Object getId() {
		return id;
	}

	public boolean isCommitted() {
		return this.statusStack.contains(TestConnectionStatus.COMMITTED);
	}
	
	public boolean isClosed() {
		return this.statusStack.contains(TestConnectionStatus.CLOSED);
	}
	
	public boolean isRollbacked() {
		return this.statusStack.contains(TestConnectionStatus.ROLLBACKED);
	}
	public boolean isAct() {
		return this.statusStack.contains(TestConnectionStatus.ACT);
	}
	public boolean isPrepared() {
		return this.statusStack.contains(TestConnectionStatus.PREPARED);
	}
	
	public boolean isRecoverd() {
		return this.statusStack.contains(TestConnectionStatus.RECOVERED);
	}
	
	public void addStatus(Integer status) {
		this.statusStack.add(status);
	}

	public boolean equals(Object obj) {
		if( obj==null || !(obj instanceof TestStatusStack)) {
			return false;
		}
		
		return ( (TestStatusStack)obj).id.equals(this.id);
	}

	public int hashCode() {
		return this.id.hashCode();
	}

	public String toString() {
		StringBuffer buffer= new StringBuffer("TestStatusStack ").append(id);
		for (Iterator iterator = this.statusStack.iterator(); iterator.hasNext();) {
			Long status = (Long) iterator.next();
			buffer.append(status).append(',');			
		}
		return buffer.toString();
	}

}
