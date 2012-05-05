package org.csc.phynixx.xa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;


/*
 * manages the state of the XAResource enlisted in Transactions.
 * 
 * @see  XAResoXAResourceTxState
 */
class XAResourceTxStateManager {
	
	private Map xidConnections= new HashMap();
	
	XAResourceTxState getXAResourceTxState(Xid xid) {
		synchronized(xidConnections) {
			return (XAResourceTxState)xidConnections.get(xid);
		}
	}
	
	void registerConnection(XAResourceTxState txState)
	{
		synchronized(xidConnections) {
			xidConnections.put(txState.getXid(), txState);
		}
	}
	
	XAResourceTxState deregisterConnection(Xid xid)
	{
		synchronized(xidConnections) {
			XAResourceTxState txState =(XAResourceTxState)xidConnections.get(xid);
			xidConnections.remove(xid);
			txState.close();
			return txState; 
		}
	}
	
	List getXAResourceTxStates(XAResource xaresource) 
	{	
		List found;
		synchronized (xidConnections) {
			found = new ArrayList(xidConnections.size() / 2);
			for (Iterator iterator = this.xidConnections.values().iterator(); iterator.hasNext();) {
				XAResourceTxState txState = (XAResourceTxState) iterator.next();
				if (txState.getXAConnectionHandle().getXAResource().equals(xaresource)) {
					found.add(txState);
				}
			}
		}
		return found; 
		
	}
	
	
}
