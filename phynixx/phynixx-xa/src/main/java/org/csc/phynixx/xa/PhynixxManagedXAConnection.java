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


import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.exceptions.ExceptionUtils;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IPhynixxConnection;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;


/**keeps the XAresource's association to the transactional branch (given via XID).
 *
 *
 * @author zf4iks2
 */
class PhynixxManagedXAConnection<C extends IPhynixxConnection> implements IPhynixxXAConnection<C> {

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(PhynixxManagedXAConnection.class);

    private final PhynixxXAResource<C> xaResource;

    private TransactionManager tmMgr = null;

    private Transaction globalTransaction = null;

    private final IXATransactionalBranchDictionary<C> xaTransactionalBranchDictionary;

    private Xid xid;

    PhynixxManagedXAConnection(PhynixxXAResource<C> xaResource, TransactionManager tmMgr, IXATransactionalBranchDictionary<C> xaTransactionalBranchDictionary) {
        this.xaResource = xaResource;
        this.xaTransactionalBranchDictionary = xaTransactionalBranchDictionary;
        this.tmMgr = tmMgr;
    }

    public XAResource getXAResource() {
        return xaResource;
    }


    Transaction getGlobalTransaction() {
        return globalTransaction;
    }


    /**
     * call by the XCAResource when {@link javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)}  is called.
     * A TransactionalBranch for the given XID ist expected to be created.
     *
     * @param xid
     */
    void associateTransactionalBranch(Xid xid) {

        if (!isInTransaction()) {
            throw new IllegalStateException("Not in Transaction. No TransactionB ranch can be associated");
        }
        this.xid = xid;
    }

    /**
     * call by the XCAResource when {@link javax.transaction.xa.XAResource#end(javax.transaction.xa.Xid, int)} is called
     *
     * @param xid
     */
    void disassociateTransactionalBranch(Xid xid) {
        this.xid = null;
        delistTransaction();
    }

    private void delistTransaction() {
        this.globalTransaction=null;
    }

    /**
     *
     * @return XID of the currently associated transactional branch; may be null, if the XAConnection isnt't associated to an tb
     */
    Xid getCurrentTransactionalBranch() {
        return this.xid;
    }

    public C getConnection() {

        // Connection wid in TX eingetragen ....
        this.enlistTransaction();


        XATransactionalBranch<C> transactionalBranch = this.findTransactionalBranch();
        if (transactionalBranch == null) {
            throw new IllegalStateException("Not in Transaction. No TransactionBranch is assiociated");
        }
        return transactionalBranch.getManagedConnection().toConnection();
    }


    /**
     * finds the transactional branch associated to the given XID
     *
     * @return
     */
    private XATransactionalBranch<C> findTransactionalBranch() {
        if (xid == null) {
            return null;
        }
        return this.xaTransactionalBranchDictionary.findTransactionalBranch(this.xid);
    }



    public boolean isInTransaction() {
        synchronized (this) {
            return this.globalTransaction != null;
        }
    }

    void associateTransaction(Transaction currentTx) {
        if (!this.isInTransaction()) {
            if (this.globalTransaction != null) {
                this.globalTransaction = currentTx;
                // enlist the xaResource in the globalTransaction
            } else {
                LOG.error(
                        "PhynixxManagedXAConnection:associateTransaction (no globalTransaction bound to thread " + Thread.currentThread());
                LOG.error(
                        "PhynixxManagedXAConnection:associateTransaction (local Transaction is opened " + Thread.currentThread());
                throw new DelegatedRuntimeException("no globalTransaction bound to thread " + Thread.currentThread());
            }
            LOG.debug("PhynixxManagedXAConnection:associateTransaction tx==" + this.globalTransaction);
        } else {
            if (!this.globalTransaction.equals(this.getTransactionManagerTransaction())) {
                LOG.error("PhynixxManagedXAConnection.associateTransaction already assigned to a TX and expected to assigned to a different TX");
                throw new DelegatedRuntimeException("already assigned to a TX and expected to assigned to a different TX");
            }

        }

    }
    /**
     * if necessary the current xa resource is enlisted in the current TX.
     *
     * The enlistment calls the{@link javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)}. This call associates the Xid with the current instance
     */
    void enlistTransaction() {
        // if not already enlist, do now
        if (!this.isInTransaction()) {
            try {
                Transaction ntx = this.getTransactionManagerTransaction();
                if (ntx != null) {
                    boolean enlisted = ntx.enlistResource(this.xaResource);
                    if (!enlisted) {
                        LOG.error("Enlisting " + xaResource + " failed");
                    }
                    associateTransaction(ntx);

                    // enlist the xaResource in the globalTransaction
                } else {
                    LOG.debug(
                            "SampleXAConnection:connectionRequiresTransaction (no globalTransaction found)");
                }
                LOG.debug("SampleXAConnection:connectionRequiresTransaction tx==" + ntx);

            } catch (RollbackException n) {
                LOG.error(
                        "SampleXAConnection:prevokeAction enlistResource exception : "
                                + n.toString());
            } catch (SystemException n) {
                LOG.error("SampleXAConnection:connectionRequiresTransaction " + n + "\n" + ExceptionUtils.getStackTrace(n));
                throw new DelegatedRuntimeException(n);
            }

        } else {
            LOG.debug("SampleXAConnection.connectionRequiresTransaction already assigned to a TX");
        }
    }


    /**
     * @return aktuelle TX des TransactionManagers
     */
    private Transaction getTransactionManagerTransaction() {
        try {
            return this.tmMgr.getTransaction();
        } catch (SystemException e) {
            LOG.error("SampleXAConnection:getTransaction " + e + "\n" + ExceptionUtils.getStackTrace(e));

            throw new DelegatedRuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhynixxManagedXAConnection that = (PhynixxManagedXAConnection) o;

        if (!xaResource.equals(that.xaResource)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return xaResource.hashCode();
    }


    @Override
    public String toString() {
        return "PhynixxManagedXAConnection{" +
                "xaResource=" + xaResource +
                ", xid=" + xid +
                '}';
    }


}
