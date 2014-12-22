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


import org.csc.phynixx.common.cast.ImplementorUtils;
import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.exceptions.ExceptionUtils;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnectionFactory;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;


/**
 * keeps the XAresource's association to the transactional branch (given via XID).
 *
 * @author zf4iks2
 */
class PhynixxManagedXAConnection<C extends IPhynixxConnection> implements IPhynixxXAConnection<C> {

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(PhynixxManagedXAConnection.class);

    private final PhynixxXAResource<C> xaResource;

    private IPhynixxManagedConnectionFactory<C> managedConnectionFactory;

    private transient ITransactionBinding<C> transactionBinding = null;

    private TransactionManager transactionManager = null;

    private final IXATransactionalBranchRepository<C> xaTransactionalBranchDictionary;

    PhynixxManagedXAConnection(PhynixxXAResource<C> xaResource,
                               TransactionManager transactionManager,
                               IXATransactionalBranchRepository<C> xaTransactionalBranchDictionary,
                               IPhynixxManagedConnectionFactory<C> managedConnectionFactory) {
        this.xaResource = xaResource;
        this.xaTransactionalBranchDictionary = xaTransactionalBranchDictionary;
        this.managedConnectionFactory = managedConnectionFactory;
        this.transactionManager = transactionManager;
    }

    public XAResource getXAResource() {
        return xaResource;
    }


    /**
     * @return !=null if an if the XAConnection is bound to a global transaction
     */
    XATransactionalBranch<C> toGlobalTransactionBranch() {
        if (this.transactionBinding != null && this.transactionBinding.getTransactionBindingType() == TransactionBindingType.GlobalTransaction) {
            return ImplementorUtils.cast(this.transactionBinding, GlobalTransactionProxy.class).getGlobalTransactionalBranch();
        }
        return null;
    }


    /**
     * call by the XCAResource when {@link javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)}  is called.
     * A TransactionalBranch for the given XID ist expected to be created.
     *
     * @param xid
     */
    void startTransactionalBranch(Xid xid) {
        cleanupTransactionBinding();

        final TransactionBindingType transactionBindingType = getTransactionBindingType();


        // already associated to a local transaction
        if (transactionBindingType == TransactionBindingType.LocalTransaction) {
            LocalTransactionProxy<C> localTransactionProxy = ImplementorUtils.cast(this.transactionBinding, LocalTransactionProxy.class);
            if (localTransactionProxy.hasTransactionalData()) {
                throw new IllegalStateException("Connection ist associated to a local transaction and has uncommitted transactional data");
            }

            XATransactionalBranch<C> xaTransactionalBranch = this.xaTransactionalBranchDictionary.findTransactionalBranch(xid);
            /// xaTransactionalBranch!=null => joining a existing transactional branch
            if (xaTransactionalBranch == null) {
                xaTransactionalBranch= this.xaTransactionalBranchDictionary.instanciateTransactionalBranch(xid, localTransactionProxy.getConnection());
            }
            this.transactionBinding = new GlobalTransactionProxy<C>(xaTransactionalBranch);

            // no transaction binding
        } else if (transactionBindingType == TransactionBindingType.NoTransaction) {
            XATransactionalBranch<C> xaTransactionalBranch = this.xaTransactionalBranchDictionary.findTransactionalBranch(xid);

            /// xaTransactionalBranch!=null => joining a existing transactional branch
            if (xaTransactionalBranch == null) {
                IPhynixxManagedConnection<C> physicalConnection = this.createPhysicalConnection();
                xaTransactionalBranch =
                        this.xaTransactionalBranchDictionary.instanciateTransactionalBranch(xid, physicalConnection);
                this.xaTransactionalBranchDictionary.instanciateTransactionalBranch(xid, physicalConnection);
            }
            this.transactionBinding = new GlobalTransactionProxy<C>(xaTransactionalBranch);

            // if bound to a global TX it has to be the same XID
        } else if (transactionBindingType == TransactionBindingType.GlobalTransaction) {
            GlobalTransactionProxy<C> globalTransactionProxy = ImplementorUtils.cast(this.transactionBinding, GlobalTransactionProxy.class);
            if (!globalTransactionProxy.getXid().equals(xid)) {
                throw new IllegalStateException("XAConnection already associated to a global Transaction");
            }
        }


    }

    private void cleanupTransactionBinding() {
        // cleanup
        if (getTransactionBindingType() == TransactionBindingType.LocalTransaction) {
            LocalTransactionProxy<C> localTransactionProxy = ImplementorUtils.cast(this.transactionBinding, LocalTransactionProxy.class);
            if (localTransactionProxy.isClosed()) {
                this.transactionBinding.release();
                this.transactionBinding = null;
            }
        }
    }

    private TransactionBindingType getTransactionBindingType() {
        return (this.transactionBinding != null) ? this.transactionBinding.getTransactionBindingType() : TransactionBindingType.NoTransaction;
    }

    private IPhynixxManagedConnection<C> createPhysicalConnection() {
        return this.managedConnectionFactory.getManagedConnection();
    }


    void resumeTransactionalBranch(Xid xid) {

        this.cleanupTransactionBinding();

        TransactionBindingType transactionBindingType = getTransactionBindingType();
        if (transactionBindingType == TransactionBindingType.GlobalTransaction) {
            throw new IllegalStateException("XAConnection associated to a global transaction and can not be resumed");
        }
        XATransactionalBranch<C> transactionalBranch = this.xaTransactionalBranchDictionary.findTransactionalBranch(xid);
        if (transactionalBranch == null) {
            throw new IllegalStateException("No suspended branch for XID " + xid);
        }
        transactionalBranch.resume();
        this.transactionBinding = new GlobalTransactionProxy<C>(transactionalBranch);

    }

    void suspendTransactionalBranch(Xid xid) {

        this.cleanupTransactionBinding();

        TransactionBindingType transactionBindingType = getTransactionBindingType();
        if (transactionBindingType != TransactionBindingType.GlobalTransaction) {
            throw new IllegalStateException("XAConnection not associated to a global transaction and can not be suspended");
        }
        GlobalTransactionProxy<C> globalTransactionProxy = ImplementorUtils.cast(this.transactionBinding, GlobalTransactionProxy.class);
        if (!globalTransactionProxy.getXid().equals(xid)) {
            throw new IllegalStateException("XAConnection already associated to a global Transaction");
        }
        globalTransactionProxy.getGlobalTransactionalBranch().suspend();

        this.transactionBinding.release();
        this.transactionBinding = null;

    }

    void joinTransactionalBranch(Xid xid) {

        cleanupTransactionBinding();

        TransactionBindingType transactionBindingType = getTransactionBindingType();
        if (transactionBindingType == TransactionBindingType.GlobalTransaction) {
            throw new IllegalStateException("XAConnection already associated to a global transaction and can not be joined to XID " + xid);
        }


        this.transactionBinding.release();
        this.transactionBinding = null;

    }

    /**
     * call by the XCAResource when {@link javax.transaction.xa.XAResource#end(javax.transaction.xa.Xid, int)} is called
     *
     * @param xid
     */
    void closeTransactionalBranch(Xid xid) {

        if (this.getTransactionBindingType() != TransactionBindingType.GlobalTransaction) {
            throw new IllegalStateException("XAConnection not associated to a global transaction");
        }

        GlobalTransactionProxy<C> globalTransactionProxy = ImplementorUtils.cast(this.transactionBinding, GlobalTransactionProxy.class);

        XATransactionalBranch<C> transactionalBranch = globalTransactionProxy.getGlobalTransactionalBranch();
        transactionalBranch.close();

        delistTransaction();
    }


    @Override
    public void close() {
        if(this.transactionBinding!=null) {
            this.transactionBinding.close();
            this.transactionBinding=null;
        }
    }

    private void delistTransaction() {
        if (this.transactionBinding != null) {
            this.transactionBinding.release();
        }
        this.transactionBinding = null;
    }

    public IPhynixxManagedConnection<C> getManagedConnection() {

        // Connection wid in TX eingetragen ....
        this.enlistTransaction();

        return this.transactionBinding.getConnection();
    }

    public C getConnection() {
        return this.getManagedConnection().toConnection();
    }


    boolean isInGlobalTransaction() {
        try {
            return this.transactionManager.getTransaction() != null;
        } catch (SystemException e) {
            throw new DelegatedRuntimeException(e);
        }
    }


    /**
     * if necessary the current xa resource is enlisted in the current TX.
     * <p/>
     * The enlistment calls the{@link javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)}. This call associates the Xid with the current instance
     */
    private void enlistTransaction() {

        this.cleanupTransactionBinding();

        TransactionBindingType transactionBindingType = this.getTransactionBindingType();

        // not associated to global transaction, try to associate a global transaction
        if (this.isInGlobalTransaction() && transactionBindingType != TransactionBindingType.GlobalTransaction) {
            try {
                Transaction ntx = this.transactionManager.getTransaction();
                if (ntx != null) {

                    // enlisted makes startTransaactionalBranch calling
                    boolean enlisted = ntx.enlistResource(this.xaResource);
                    if (!enlisted) {
                        LOG.error("Enlisting " + xaResource + " failed");
                    } else {

                    }
                } else {
                    LOG.debug(
                            "SampleXAConnection:connectionRequiresTransaction (no globalTransaction found)");
                }
            } catch (RollbackException n) {
                LOG.error(
                        "SampleXAConnection:prevokeAction enlistResource exception : "
                                + n.toString());
            } catch (SystemException n) {
                LOG.error("SampleXAConnection:connectionRequiresTransaction " + n + "\n" + ExceptionUtils.getStackTrace(n));
                throw new DelegatedRuntimeException(n);
            }
        } else if (transactionBindingType == TransactionBindingType.NoTransaction) {
            this.transactionBinding = new LocalTransactionProxy<C>(this.managedConnectionFactory.getManagedConnection());
        } else     // In Global Transaction and associated to a global transaction => nothing to do
            if (this.isInGlobalTransaction() && transactionBindingType == TransactionBindingType.GlobalTransaction) {
            } else

                // Not in Global Transaction and associated to a local transaction => nothing to do
                if (!this.isInGlobalTransaction() && transactionBindingType == TransactionBindingType.LocalTransaction) {
                }

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhynixxManagedXAConnection that = (PhynixxManagedXAConnection) o;

        return xaResource.equals(that.xaResource);

    }

    @Override
    public int hashCode() {
        return xaResource.hashCode();
    }


    @Override
    public String toString() {
        return "PhynixxManagedXAConnection{" +
                "xaResource=" + xaResource +
                ", transactionBinding=" + this.transactionBinding +
                '}';
    }


}
