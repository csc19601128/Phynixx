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


import org.csc.phynixx.connection.*;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.exceptions.ExceptionUtils;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;


/**
 * keeps the XAResource's relation to the (logical) connection.
 * <p/>
 * association is handle in the current class.
 * The XAResource observes the connection via the IF ISampleConnectionListener to be notified of the
 * state changes.
 *
 * @author zf4iks2
 */
class PhynixxXAConnection extends PhynixxConnectionProxyListenerAdapter implements IPhynixxXAConnection, IPhynixxConnectionProxyListener {

    private TransactionManager tmMgr = null;
    private volatile boolean readOnly = true;

    private PhynixxXAResource xaresource = null;
    private IPhynixxConnectionProxy connectionProxy = null;
    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private Transaction transaction = null;

    PhynixxXAConnection(
            PhynixxXAResource xaresource,
            TransactionManager tmMgr,
            IPhynixxConnectionProxy connectionProxy) {
        super();
        this.xaresource = xaresource;
        this.tmMgr = tmMgr;
        this.connectionProxy = connectionProxy;
        this.setConnection(this.connectionProxy.getConnection());

        // the current handle observes the connection proxy
        connectionProxy.addConnectionListener(this);
    }


    public XAResource getXAResource() {
        return xaresource;
    }

    public IPhynixxConnectionProxy getConnectionHandle() {
        return connectionProxy;
    }

    public IPhynixxConnection getConnection() {
        return this.connectionProxy;
    }

    /**
     * sets the new Connection.
     * the previous connection is returned.
     * this connection is not closed so it can be reused.
     * <p/>
     * The State of the previous connection is not checked.
     *
     * @param con
     */
    void setConnection(IPhynixxConnection con) {
        this.connectionProxy.setConnection(con);
    }


    public boolean isInTransaction() {
        synchronized (this) {
            return this.transaction != null;
        }
    }

    public void associateTransaction() {
        if (!this.isInTransaction()) {
            Transaction ntx = this.getTransactionmanagerTransaction();
            if (ntx != null) {
                this.transaction = ntx;
                // enlist the xaResource in the transaction
            } else {
                log.error(
                        "SampleXAConnection:associateTransaction (no transaction bound to thread " + Thread.currentThread());
                throw new DelegatedRuntimeException("no transaction bound to thread " + Thread.currentThread());
            }
            log.debug("SampleXAConnection:associateTransaction tx==" + ntx);
        } else {
            if (!this.transaction.equals(this.getTransactionmanagerTransaction())) {
                log.error("SampleXAConnection.associateTransaction already assigned to a TX and expected to assigned to a different TX");
                throw new DelegatedRuntimeException("already assigned to a TX and expected to assigned to a different TX");
            }

        }

    }

    public boolean isReadOnly() {
        return this.readOnly;
    }


    /**
     * if necessary the current xa resource is enlisted in the current TX.
     * <p/>
     * the current callback method is called, if a connection's method indicates, that its
     * execution has to be protected by a TX.
     * If th resource was enlisted in a TX without any indication it could happen, that the resource
     * is enlisted twice. We rely on the transaction manger to handle this situation correctly
     */
    public synchronized void connectionRequiresTransaction(IPhynixxConnectionProxyEvent event) {
        // if not already enlist, do now
        if (!this.isInTransaction()) {
            try {
                Transaction ntx = this.getTransactionmanagerTransaction();
                if (ntx != null) {
                    associateTransaction();
                    ntx.enlistResource(this.xaresource);

                    // enlist the xaResource in the transaction
                } else {
                    log.debug(
                            "SampleXAConnection:connectionRequiresTransaction (no transaction found)");
                }
                log.debug("SampleXAConnection:connectionRequiresTransaction tx==" + ntx);

            } catch (RollbackException n) {
                log.error(
                        "SampleXAConnection:prevokeAction enlistResource exception : "
                                + n.toString());
            } catch (SystemException n) {
                log.error("SampleXAConnection:connectionRequiresTransaction " + n + "\n" + ExceptionUtils.getStackTrace(n));
                throw new DelegatedRuntimeException(n);
            }

        } else {
            log.debug("SampleXAConnection.connectionRequiresTransaction already assigned to a TX");
        }

        // indicates that the resource may change
        this.readOnly = false;

    }

    /*
     * releases the connection
     * The connection can be reused
     */
    void close() {
        if (this.connectionProxy != null) {
            this.connectionProxy.removeConnectionListener(this);
        }
        this.connectionProxy.setConnection(null);
    }


    /**
     *
     */
    public void connectionClosed(IPhynixxConnectionProxyEvent event) {
        event.getConnectionProxy().removeConnectionListener(this);
    }

    /**
     * @return aktuelle TX des TransactionManagers
     */
    private Transaction getTransactionmanagerTransaction() {
        try {
            return this.tmMgr.getTransaction();
        } catch (SystemException e) {
            log.error("SampleXAConnection:getTransaction " + e + "\n" + ExceptionUtils.getStackTrace(e));

            throw new DelegatedRuntimeException(e);
        }
    }


    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PhynixxXAConnection)) {
            return false;
        }
        return ((PhynixxXAConnection) obj).connectionProxy.equals(this.connectionProxy);
    }


    public int hashCode() {
        return this.connectionProxy.hashCode();
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer("SampleXAConnection");
        buffer.append("\n   connected to " + connectionProxy.toString()).
                append("\n   enlisted in TX ").append(this.transaction != null).
                append("\n   readOnly ").append(this.readOnly).
                append("\n   relates to XAResource ").append(this.xaresource.getId());
        return buffer.toString();
    }


}
