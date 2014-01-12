package org.csc.phynixx.xa;

/*
 * #%L
 * phynixx-xa
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


import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.*;


/*
 * manages the state of the XAResource enlisted in Transactions.
 * 
 * @see  XAResoXAResourceTxState
 */
class XAResourceTxStateManager {

    private Map xidConnections = new HashMap();

    XAResourceTxState getXAResourceTxState(Xid xid) {
        synchronized (xidConnections) {
            return (XAResourceTxState) xidConnections.get(xid);
        }
    }

    void registerConnection(XAResourceTxState txState) {
        synchronized (xidConnections) {
            xidConnections.put(txState.getXid(), txState);
        }
    }

    XAResourceTxState deregisterConnection(Xid xid) {
        synchronized (xidConnections) {
            XAResourceTxState txState = (XAResourceTxState) xidConnections.get(xid);
            xidConnections.remove(xid);
            txState.close();
            return txState;
        }
    }

    List<XAResourceTxState> getXAResourceTxStates(XAResource xaresource) {
        List<XAResourceTxState> found;
        synchronized (xidConnections) {
            found = new ArrayList<XAResourceTxState>(xidConnections.size() / 2);
            for (Iterator<XAResourceTxState> iterator = this.xidConnections.values().iterator(); iterator.hasNext(); ) {
                XAResourceTxState txState = iterator.next();
                if (txState.getXAConnectionHandle().getXAResource().equals(xaresource)) {
                    found.add(txState);
                }
            }
        }
        return found;

    }


}
