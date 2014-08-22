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


import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnectionListener;
import org.csc.phynixx.connection.PhynixxManagedConnectionListenerAdapter;

import javax.transaction.xa.Xid;


/**
 * This TransactionProxy shows, that the XAresource is enölisted in a Global Transaction. During enlisting, the transactional brnach isn't known. It is assigned during {@link javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)}
 */
class GlobalTransactionProxy<C extends IPhynixxConnection> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxManagedConnectionListener<C>, ITransactionBinding<C> {

    // private TransactionManager tmMgr = null;

    private XATransactionalBranch<C> transactionalBranch;

    GlobalTransactionProxy() {
    }

    XATransactionalBranch<C> getGlobalTransactionalBranch() {
        return transactionalBranch;
    }

    /**
     *
     * @param transactionalBranch
     * @return if the XATransactionalBranch has to be assigned at construction
, return value cann be used to assign      */
    GlobalTransactionProxy<C> assignTransactionalBranch( XATransactionalBranch<C> transactionalBranch ) {
        if( this.transactionalBranch!=null) {
            throw new IllegalStateException("transactionalBranch already assigned");
        }
        this.transactionalBranch=transactionalBranch;

        return this;
    }

    @Override
    public TransactionBindingType getTransactionBindingType() {
        return TransactionBindingType.GlobalTransaction;
    }

    /**
     * releases and closes the associated TransactionalBranch, but do not close the associated resources
     */
    @Override
    public void release() {
        if (transactionalBranch != null) {
            transactionalBranch.getManagedConnection().removeConnectionListener(this);
            /**
             transactionalBranch.getManagedConnection().close();
             transactionalBranchRepository.releaseTransactionalBranch(this.getXid());
             **/
        }
        this.transactionalBranch = null;

    }

    /**
     * releases and closes the associated TransactionalBranch, and releases the associated resources
     */
    @Override
    public void close() {
        if (transactionalBranch != null) {
            XATransactionalBranch<C> branch= this.transactionalBranch;
            this.release();
            branch.getManagedConnection().close();
        }

    }


    Xid getXid() {
        if (transactionalBranch != null) {
            return this.transactionalBranch.getXid();
        }
        return null;

    }


    @Override
    public IPhynixxManagedConnection<C> getConnection() {
        return this.transactionalBranch.getManagedConnection();
    }

}