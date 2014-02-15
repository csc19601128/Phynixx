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
import org.csc.phynixx.connection.IManagedConnectionProxyEvent;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnectionListener;
import org.csc.phynixx.connection.PhynixxManagedConnectionListenerAdapter;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;


/**
 * keeps the XAResource's relation to the (logical) connection.
 * <p/>
 * association is handle in the current class.
 * The XAResource observes the connection via the IF ISampleConnectionListener to be notified of the
 * state changes.
 *
 * @author zf4iks2
 */
class GlobalTransactionListener<C extends IPhynixxConnection> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxXAConnection, IPhynixxManagedConnectionListener<C> {

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(GlobalTransactionListener.class);


    private PhynixxXAResource xaresource;

    private Transaction transaction = null;

    GlobalTransactionListener(
            PhynixxXAResource<C> xaResource,
            TransactionManager tmMgr) {
        super();
        this.xaresource = xaResource;
        this.tmMgr = tmMgr;
    }

    public boolean isInTransaction() {
        synchronized (this) {
            return this.transaction != null;
        }
    }

    public void associateTransaction() {
        if (!this.isInTransaction()) {
            Transaction ntx = this.getTransactionManagerTransaction();
            if (ntx != null) {
                this.transaction = ntx;
                // enlist the xaResource in the transaction
            } else {
                LOG.error(
                        "SampleXAConnection:associateTransaction (no transaction bound to thread " + Thread.currentThread());
                throw new DelegatedRuntimeException("no transaction bound to thread " + Thread.currentThread());
            }
            LOG.debug("SampleXAConnection:associateTransaction tx==" + ntx);
        } else {
            if (!this.transaction.equals(this.getTransactionManagerTransaction())) {
                LOG.error("SampleXAConnection.associateTransaction already assigned to a TX and expected to assigned to a different TX");
                throw new DelegatedRuntimeException("already assigned to a TX and expected to assigned to a different TX");
            }

        }

    }

    /**
     * if necessary the current xa resource is enlisted in the current TX.
     * <p/>
     * the current callback method is called, if a connection's method indicates, that its
     * execution has to be protected by a TX.
     * If th resource was enlisted in a TX without any indication it could happen, that the resource
     * is enlisted twice. We rely on the transaction manger to handle this situation correctly
     */
    public void connectionRequiresTransaction(IManagedConnectionProxyEvent<C> event) {
        // if not already enlist, do now
        if (!this.isInTransaction()) {
            try {
                Transaction ntx = this.getTransactionManagerTransaction();
                if (ntx != null) {
                    associateTransaction();
                    ntx.enlistResource(this.xaresource);

                    // enlist the xaResource in the transaction
                } else {
                    LOG.debug(
                            "SampleXAConnection:connectionRequiresTransaction (no transaction found)");
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

    /*
     * releases the connection
     * The connection can be reused
     */
    void close() {
        this.managedConnection.fireConnectionDereferenced();
        if (this.managedConnection != null) {
            this.managedConnection.removeConnectionListener(this);
        }
        this.managedConnection = null;
    }


    /**
     * current listener has to be removed ...
     */
    public void connectionClosed(IManagedConnectionProxyEvent<C> event) {
        event.getManagedConnection().removeConnectionListener(this);
    }

    /**
     * @return aktuelle TX des TransactionManagers
     */
    private Transaction getTransactionManagerTransaction() {
        try {
            return this.tmMgr.getTransaction();
        } catch (SystemException e) {
            log.error("SampleXAConnection:getTransaction " + e + "\n" + ExceptionUtils.getStackTrace(e));

            throw new DelegatedRuntimeException(e);
        }
    }


    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GlobalTransactionListener)) {
            return false;
        }
        return ((GlobalTransactionListener) obj).managedConnection.equals(this.managedConnection);
    }


    public int hashCode() {
        return this.managedConnection.hashCode();
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer("SampleXAConnection");
        buffer.append("\n   connected to " + managedConnection.toString()).
                append("\n   enlisted in TX ").append(this.transaction != null).
                append("\n   readOnly ").append(this.readOnly).
                append("\n   relates to XAResource ").append(this.xaresource.getId());
        return buffer.toString();
    }


}
