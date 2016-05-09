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

import java.util.Arrays;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.lang.Validate;
import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.exceptions.ExceptionUtils;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnectionFactory;

/**
 * keeps the XAresource's association to the transactional branch(es) (given via
 * XID) or manages the local transaction
 *
 * @author Christoph Schmidt-Casdorff
 */
class PhynixxManagedXAConnection<C extends IPhynixxConnection> implements IPhynixxXAConnection<C> {

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(PhynixxManagedXAConnection.class);

    /**
     * parent XAResource
     */
    private final PhynixxXAResource<C> xaResource;

    private IPhynixxManagedConnectionFactory<C> managedConnectionFactory;

    private final ITransactionBinding<C> transactionBinding = new TransactionBinding<C>();

    private TransactionManager transactionManager = null;

    /**
     * Global repository for XA-Branches associated with XAResource of the
     * current Resource
     */
    private final IXATransactionalBranchRepository<C> xaTransactionalBranchDictionary;

    private boolean enlisted;

    PhynixxManagedXAConnection(PhynixxXAResource<C> xaResource, TransactionManager transactionManager,
            IXATransactionalBranchRepository<C> xaTransactionalBranchDictionary,
            IPhynixxManagedConnectionFactory<C> managedConnectionFactory) {
        this.xaResource = xaResource;
        this.xaTransactionalBranchDictionary = xaTransactionalBranchDictionary;
        this.managedConnectionFactory = managedConnectionFactory;
        this.transactionManager = transactionManager;
    }

    @Override
    public XAResource getXAResource() {
        return xaResource;
    }

    /**
     * Ermittelt, ob zu zugehoeriger Connection bereits ein TX exitiert. This
     * tranbsaction binding is reused
     */
    @Override
    public IPhynixxManagedConnection<C> getManagedConnection() {

        // Connection wid in TX eingetragen ....
        try {
			this.checkTransactionBinding();
		} catch (XAException e) {
			throw new DelegatedRuntimeException(e);
		}

        return transactionBinding.getManagedConnection();
    }

    @Override
    public C getConnection() {
        return this.getManagedConnection().toConnection();
    }

    /**
     * @return !=null if an if the XAConnection is bound to a global transaction
     */
    XATransactionalBranch<C> toGlobalTransactionBranch() {
        if (this.transactionBinding != null && this.transactionBinding.isGlobalTransaction()) {
            GlobalTransactionProxy<C> activeXid = this.transactionBinding.getEnlistedGlobalTransaction();
            if (activeXid == null) {
                throw new IllegalStateException("No active XABranch");
            }
            // suche XABranch
            return activeXid.getGlobalTransactionalBranch();
        }
        return null;
    }

    /**
     * @return !=null if an if the XAConnection is bound to a global transaction
     */
    LocalTransactionProxy<C> toLocalTransaction() {
        if (this.transactionBinding != null && this.transactionBinding.isLocalTransaction()) {
            LocalTransactionProxy<C> lid = this.transactionBinding.getEnlistedLocalTransaction();
            if (lid == null) {
                throw new IllegalStateException("No active LocalTransaction");
            }
            // suche XABranch
            return lid;
        }
        return null;
    }

    /**
     * call by the XAResource when
     * {@link javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)}
     * is called. A TransactionalBranch for the given XID ist expected to be
     * created.
     *
     * @param xid
     */
    void startTransactionalBranch(Xid xid) throws Exception{

        cleanupTransactionBinding();

        final TransactionBindingType transactionBindingType = this.transactionBinding.getTransactionBindingType();

        // already associated to a local transaction
        if (transactionBindingType == TransactionBindingType.LocalTransaction) {
            LocalTransactionProxy<C> localTransactionProxy = this.toLocalTransaction();
            Validate.isTrue(!localTransactionProxy.hasTransactionalData(),
                    "Connection is associated to a local transaction and has uncommitted transactional data");
            if (localTransactionProxy.hasTransactionalData()) {
                throw new IllegalStateException(
                        "Connection is associated to a local transaction and has uncommitted transactional data");
            }

            XATransactionalBranch<C> xaTransactionalBranch = 
                    this.xaTransactionalBranchDictionary.findTransactionalBranch(xid);
            // xaTransactionalBranch!=null => joining a existing transactional branch
            if (xaTransactionalBranch == null) {
                IPhynixxManagedConnection<C> connection = localTransactionProxy.getConnection();
                if( connection==null || connection.isClosed()) {
                    connection= this.createPhysicalConnection();
                }
                xaTransactionalBranch = this.instanciateTransactionalBranch(xid, connection);
            } else {
                localTransactionProxy.close();
            }
            // forget the Local transaction
            this.transactionBinding.release();

            XATransactionalBranch<C> transactionalBranch = this.xaTransactionalBranchDictionary
                    .findTransactionalBranch(xid);
            Validate.isTrue(xaTransactionalBranch != null, "Expect a transactional branch to be created");

            this.transactionBinding.activateGlobalTransaction(new GlobalTransactionProxy<C>(transactionalBranch));

            // no transaction binding
        } else if (transactionBindingType == TransactionBindingType.NoTransaction) {
            XATransactionalBranch<C> transactionalBranch = this.xaTransactionalBranchDictionary
                    .findTransactionalBranch(xid);

            // / xaTransactionalBranch!=null => joining a existing transactional
            // branch
            if (transactionalBranch == null) {
                IPhynixxManagedConnection<C> physicalConnection = this.createPhysicalConnection();
                transactionalBranch = this.instanciateTransactionalBranch(xid,physicalConnection);
                // this.xaTransactionalBranchDictionary.instanciateTransactionalBranch(xid, physicalConnection,transactionManager.getTransaction());
            }
            this.transactionBinding.activateGlobalTransaction(new GlobalTransactionProxy<C>(transactionalBranch));

            // if bound to a global TX , check if same XID
        } else if (transactionBindingType == TransactionBindingType.GlobalTransaction) {

            XATransactionalBranch<C> transactionalBranch = this.xaTransactionalBranchDictionary
                    .findTransactionalBranch(xid);

            // xaTransactionalBranch!=null => joining a existing transactional
            // branch
            if (transactionalBranch == null) {
                IPhynixxManagedConnection<C> physicalConnection = this.createPhysicalConnection();
                transactionalBranch = instanciateTransactionalBranch(xid, physicalConnection);
                //this.xaTransactionalBranchDictionary.instanciateTransactionalBranch(xid, physicalConnection,transactionManager.getTransaction());
            }

            // Check if previous XID are compatible to the current
            GlobalTransactionProxy<C> previousGlobalTransactionProxy = this.transactionBinding
                    .getEnlistedGlobalTransaction();
            if (previousGlobalTransactionProxy != null) {
                Xid previousXid = previousGlobalTransactionProxy.getXid();
                byte[] currentTransactionId = previousXid.getGlobalTransactionId();
                if (!Arrays.equals(xid.getGlobalTransactionId(), currentTransactionId)) {
                    LOG.warn("TransactionId of the new Transaction doenn't corresponds to the transactionId of the previous Ids");
                }
                previousGlobalTransactionProxy.release();
            }

            this.transactionBinding.activateGlobalTransaction(new GlobalTransactionProxy<C>(transactionalBranch));

        }

    }

	private XATransactionalBranch<C> instanciateTransactionalBranch(Xid xid,
			IPhynixxManagedConnection<C> physicalConnection)
			throws SystemException {
		return this.xaTransactionalBranchDictionary.instanciateTransactionalBranch(xid,physicalConnection,this.getXAResource(),transactionManager.getTransaction());
	}

    private void cleanupTransactionBinding() {
        // cleanup
        if (this.transactionBinding.getTransactionBindingType() == TransactionBindingType.LocalTransaction) {
            LocalTransactionProxy<C> localTransactionProxy = this.transactionBinding.getEnlistedLocalTransaction();
            if (localTransactionProxy != null) {
                if (localTransactionProxy.isClosed()) {
                    localTransactionProxy.release();
                }
            }
        }
    }

    private IPhynixxManagedConnection<C> createPhysicalConnection() {
        return this.managedConnectionFactory.getManagedConnection();
    }

    void resumeTransactionalBranch(Xid xid) {

        this.cleanupTransactionBinding();

        TransactionBindingType transactionBindingType = this.transactionBinding.getTransactionBindingType();
        if (transactionBindingType == TransactionBindingType.GlobalTransaction) {
            throw new IllegalStateException("XAConnection associated to a global transaction and can not be resumed");
        }
        XATransactionalBranch<C> transactionalBranch = findTransactionalBranch(xid);
        transactionalBranch.resume();

        // Check if previous XID are compatible to the current
        if (transactionBinding.isGlobalTransaction()) {
            GlobalTransactionProxy<C> previousGlobalTransactionProxy = this.transactionBinding.getEnlistedGlobalTransaction();
            LOG.warn("Resume meets an unrelease Transactional branch. It is released");
            previousGlobalTransactionProxy.close();
        }
        this.transactionBinding.activateGlobalTransaction(new GlobalTransactionProxy<C>(transactionalBranch));
    }

    private XATransactionalBranch<C> findTransactionalBranch(Xid xid) {
        XATransactionalBranch<C> transactionalBranch = this.xaTransactionalBranchDictionary
                .findTransactionalBranch(xid);
        if (transactionalBranch == null) {
            throw new IllegalStateException("No suspended branch for XID " + xid);
        }
        return transactionalBranch;
    }

    void suspendTransactionalBranch(Xid xid) {

        this.cleanupTransactionBinding();

        GlobalTransactionProxy<C> previousGlobalTransactionProxy = this.transactionBinding
                .getEnlistedGlobalTransaction();
        if (previousGlobalTransactionProxy == null) {
            LOG.warn("suspends meets not active Transactional branch.");
        } else {
            previousGlobalTransactionProxy.release();
        }

        XATransactionalBranch<C> transactionalBranch = findTransactionalBranch(xid);
        transactionalBranch.suspend();
        delistTransaction();

    }

    void joinTransactionalBranch(Xid xid) {

        cleanupTransactionBinding();

        TransactionBindingType transactionBindingType = this.transactionBinding.getTransactionBindingType();
        if (transactionBindingType == TransactionBindingType.GlobalTransaction) {
            throw new IllegalStateException(
                    "XAConnection already associated to a global transaction and can not be joined to XID " + xid);
        }

        this.transactionBinding.release();

    }

    /**
     * call by the XCAResource when
     * {@link javax.transaction.xa.XAResource#end(javax.transaction.xa.Xid, int)}
     * is called
     *
     * @param xid
     */
    void closeTransactionalBranch(Xid xid) {

        if (this.transactionBinding.getTransactionBindingType() != TransactionBindingType.GlobalTransaction) {
            throw new IllegalStateException("XAConnection not associated to a global transaction");
        }

        GlobalTransactionProxy<C> globalTransactionProxy = this.transactionBinding.getEnlistedGlobalTransaction();
        globalTransactionProxy.close();

        delistTransaction();
    }

    @Override
    public void close() {
       this.xaResource.close();
    }
    
    void doClose() {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Closing "+this);
        }
        if (this.transactionBinding != null) {
            this.transactionBinding.close();
        }
        this.enlisted=false;
    }
    private void delistTransaction() {
        if (this.transactionBinding != null) {
            this.transactionBinding.release();
        }
        this.enlisted=false;
        this.transactionBinding.release();
    }

    boolean isInGlobalTransaction() {
        try {
            return transactionManager != null && this.transactionManager.getTransaction() != null;
        } catch (SystemException e) {
            throw new DelegatedRuntimeException(e);
        }
    }

    /**
     * if necessary the current xa resource is enlisted in the current TX.
     * <p/>
     * The enlistment calls the
     * {@link javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)}
     * . This call associates the Xid with the current instance
     * @throws XAException 
     */
    private void enlistTransaction() throws XAException {

        this.cleanupTransactionBinding();

        TransactionBindingType transactionBindingType = this.transactionBinding.getTransactionBindingType();

        // not associated to global transaction, try to associate a global
        // transaction
        if (this.isInGlobalTransaction() && transactionBindingType != TransactionBindingType.GlobalTransaction) {
            try {
                Transaction ntx = this.transactionManager.getTransaction();
                
                XATransactionalBranch<C> xaResourceEnlistment = findXAResourceEnlistment();
                if( xaResourceEnlistment!=null ) {
                	this.transactionBinding.activateGlobalTransaction(new GlobalTransactionProxy<C>(xaResourceEnlistment));
                	this.enlisted = true;
                } else 	if ( !enlisted && ntx != null) {
                	// Bitronix calls start on reaction of enlist --- check if cycle
                    this.enlisted = true;
                    // enlisted makes startTransaactionalBranch calling
                    this.enlisted = ntx.enlistResource(this.xaResource);
                    if (!enlisted) {
                        LOG.error("Enlisting " + xaResource + " failed");
                    } else {

                    }
                } else {
                    LOG.debug("SampleXAConnection:connectionRequiresTransaction (no globalTransaction found)");
                }
            } catch (RollbackException n) {
                LOG.error("SampleXAConnection:prevokeAction enlistResource exception : " + n.toString());
            } catch (SystemException n) {
                LOG.error("SampleXAConnection:connectionRequiresTransaction " + n + "\n"
                        + ExceptionUtils.getStackTrace(n));
                throw new DelegatedRuntimeException(n);
            }
        } else if (transactionBindingType == TransactionBindingType.NoTransaction) {
            this.transactionBinding.activateLocalTransaction(new LocalTransactionProxy<C>(this.managedConnectionFactory
                    .getManagedConnection()));
        } else { // In Global Transaction and associated to a global transaction
                 // => nothing to do
            if (this.isInGlobalTransaction() && transactionBindingType == TransactionBindingType.GlobalTransaction) {
                // Not in Global Transaction and associated to a local
                // transaction => nothing to do
            } else if (!this.isInGlobalTransaction()
                    && transactionBindingType == TransactionBindingType.LocalTransaction) {
            }
        }

    }

    private XATransactionalBranch<C> findXAResourceEnlistment() throws XAException, SystemException 
    {
		Transaction tx= this.transactionManager.getTransaction();
		XAResource xaResource = this.getXAResource();
		
		
		// check, if this resource is already enlisted 		
		return this.xaTransactionalBranchDictionary.findTransactionalBranch(tx,xaResource);
	}

	@Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PhynixxManagedXAConnection<C> that = (PhynixxManagedXAConnection) o;

        return xaResource.equals(that.xaResource);

    }

    @Override
    public int hashCode() {
        return xaResource.hashCode();
    }

    @Override
    public String toString() {
        return "PhynixxManagedXAConnection{" + "xaResource=" + xaResource + ", transactionBinding="
                + this.transactionBinding + '}';
    }

    void checkTransactionBinding() throws XAException {

        if (this.isInGlobalTransaction()) {
            this.enlistTransaction();
        }
        if (this.transactionBinding == null
                || this.transactionBinding.getTransactionBindingType() == TransactionBindingType.NoTransaction) {
            this.transactionBinding.activateLocalTransaction(new LocalTransactionProxy<C>(this.createPhysicalConnection()) );
       }
    }

}
