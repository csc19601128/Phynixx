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


import javax.transaction.xa.Xid;

import org.csc.phynixx.connection.IManagedConnectionEvent;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnectionListener;
import org.csc.phynixx.connection.PhynixxManagedConnectionListenerAdapter;


/**
 * This TransactionProxy shows, that the XAresource is enlisted in a Global Transaction. 
 * During enlisting, the transactional brnach isn't known. It is assigned during {@link javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)}
 */
class GlobalTransactionProxy<C extends IPhynixxConnection> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxManagedConnectionListener<C> {

    // private TransactionManager tmMgr = null;

    private XATransactionalBranch<C> transactionalBranch;

    GlobalTransactionProxy(XATransactionalBranch<C> transactionalBranch) {
        setTransactionalBranch(transactionalBranch);
    }

    XATransactionalBranch<C> getGlobalTransactionalBranch() {
        return transactionalBranch;
    }

    /**
     *
     * @param transactionalBranch
     * @return if the XATransactionalBranch has to be assigned at construction , return value can be used to assign      */
    private void setTransactionalBranch( XATransactionalBranch<C> transactionalBranch ) {
        if( this.transactionalBranch!=null) {
            throw new IllegalStateException("transactionalBranch already assigned");
        }
        this.transactionalBranch=transactionalBranch;
        this.transactionalBranch.getManagedConnection().addConnectionListener(this);

    }

    public TransactionBindingType getTransactionBindingType() {
        return TransactionBindingType.GlobalTransaction;
    }

    /**
     * releases and closes the associated TransactionalBranch, but do not close the associated resources
     */
    public void release() {
        if (transactionalBranch != null) {
            transactionalBranch.getManagedConnection().removeConnectionListener(this);
        }
        this.transactionalBranch = null;
    }

    /**
     * releases and closes the associated TransactionalBranch, and releases the associated resources
     */
    public void close() {
        if (transactionalBranch != null) {
            XATransactionalBranch<C> branch= this.transactionalBranch;
            this.release();
            branch.getManagedConnection().close();
        }

    }


    @Override
    public String toString() {
        return "GlobalTransactionProxy [transactionalBranch=" + this.transactionalBranch + "]";
    }

    Xid getXid() {
        if (transactionalBranch != null) {
            return this.transactionalBranch.getXid();
        }
        return null;
    }


    public IPhynixxManagedConnection<C> getConnection() {
        return this.transactionalBranch.getManagedConnection();
    }
    
    @Override
    public void connectionReleased(IManagedConnectionEvent<C> event) {
        event.getManagedConnection().removeConnectionListener(this);
    }

    @Override
    public void connectionFreed(IManagedConnectionEvent<C> event) {
        event.getManagedConnection().removeConnectionListener(this);
    }

}