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


import org.csc.phynixx.connection.IManagedConnectionProxyEvent;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnectionListener;
import org.csc.phynixx.connection.PhynixxManagedConnectionListenerAdapter;
import org.csc.phynixx.exceptions.SampleTransactionalException;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;

import javax.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * manages and stores the current state of an XAResource enlisted in a transaction.
 * <p/>
 * The working horse of the XAResource is the connection. The logical connection is represented by
 * PhynixxManagedXAConnection.
 * <p/>
 * <p/>
 * <h1>activation state</h1>
 * A XAResource can participate in several TXs. This is controlled by the TA-Manager
 * using the resume/suspend-protocol
 * A XAResource that hasn't already be prepared can be suspended.
 * <p/>
 * <h1>progress state</h1>
 * The progress of the 2PC runs from STARTED TO COMMITTED. The current state is
 * stored.
 * <p/>
 * <h1>Rollback-only mode</h1>
 * If nothing has to be committed a TX can move to RB-Only State
 * <p/>
 * <p/>
 * <h1>joined resources</h1>
 * <p/>
 * XAResources can join another XAResource. They share the connection and the XID. The joined resource is
 *
 * @author zf4iks2
 */
class XAResourceTxState<C extends IPhynixxConnection> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxManagedConnectionListener<C> {


    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private final static int SUSPENDED = 0x02000000;
    private final static int ACTIVE = 0x04000000;

    private int progressState = Status.STATUS_ACTIVE;

    private Xid xid = null;

    private PhynixxManagedXAConnection<C> xaConnectionHandle;

    private Set joinedXAConnections = new HashSet();

    private int activeState = ACTIVE;
    private volatile boolean rollbackOnly = false;

    private int heuristicState = 0;

    XAResourceTxState(Xid xid, PhynixxManagedXAConnection<C> xaConnectionHandle) {
        super();
        this.xid = xid;
        this.xaConnectionHandle = xaConnectionHandle;
        this.setRollbackOnly(false);

        // Observe the connection proxy
        this.xaConnectionHandle.getManagedConnectionHandle().addConnectionListener(this);
    }

    Xid getXid() {
        return xid;
    }

    PhynixxManagedXAConnection<C> getXAConnectionHandle() {
        return xaConnectionHandle;
    }

    boolean isJoined(XAResource resouce) {
        if (resouce == null) {
            return false;
        }
        return this.joinedXAConnections.contains(resouce);
    }

    void join(PhynixxManagedXAConnection<C> xaConnectionHandle) {

        // join the connections ...
        xaConnectionHandle.setConnection(this.getXAConnectionHandle().getManagedConnectionHandle());

        // save and ignore the joined connection
        if (!this.joinedXAConnections.contains(xaConnectionHandle)) {
            this.joinedXAConnections.add(xaConnectionHandle);
        }
    }


    void suspend() {
        if (this.activeState == SUSPENDED) {
            return;
        }
        if (this.progressState != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("A already prepared or preparing TX can not be suspended");

        }
        this.activeState = SUSPENDED;
    }

    void resume() {
        if (this.activeState == XAResourceTxState.ACTIVE) {
            return;
        }
        this.activeState = XAResourceTxState.ACTIVE;
    }

    public boolean isActive() {
        return this.getActiveState() == XAResourceTxState.ACTIVE;
    }

    boolean isSuspended() {
        return this.getActiveState() == XAResourceTxState.SUSPENDED;
    }

    private int getActiveState() {
        return activeState;
    }

    synchronized void setRollbackOnly(boolean rbOnly) {
        this.rollbackOnly = rbOnly;
        //new Exception("Set to " + rbOnly+" ObjectId"+super.toString()).printStackTrace();
    }

    synchronized boolean isRollbackOnly() {
        // new Exception("Read " + this.rollbackOnly+" ObjectId"+super.toString()).printStackTrace();
        return rollbackOnly;
    }

    synchronized int getProgressState() {
        return progressState;
    }

    private void setProgressState(int progressState) {
        this.progressState = progressState;
    }


    synchronized boolean hasHeuristicOutcome() {
        return (this.heuristicState > 0);
    }

    synchronized int getHeuristicState() {
        return this.heuristicState;
    }

    private IPhynixxConnection getCurrentConnection() {
        if (this.xaConnectionHandle == null || this.xaConnectionHandle.getConnection() == null) {
            return null;
        }
        return this.xaConnectionHandle.getConnection();
    }

    synchronized void commit(boolean onePhase) throws XAException {
        if (this.getProgressState() == Status.STATUS_COMMITTED) {
            return;
        }

        if (this.hasHeuristicOutcome()) {
            throw new XAException(this.heuristicState);
        }
        if (!checkTXRequirements()) {
            throw new SampleTransactionalException("State not transactional " + this);
        }

        this.checkRollback();

        if (this.getProgressState() != Status.STATUS_ACTIVE && onePhase) {
            throw new XAException(XAException.XAER_RMFAIL);
        } else if (this.getProgressState() != Status.STATUS_PREPARED && !onePhase) {
            throw new XAException(XAException.XAER_RMFAIL);
        } else if (this.getProgressState() != Status.STATUS_PREPARED && this.getProgressState() != Status.STATUS_ACTIVE) {
            throw new XAException(XAException.XAER_RMFAIL);
        }

        this.setProgressState(Status.STATUS_COMMITTING);
        this.getCurrentConnection().commit();
        this.setProgressState(Status.STATUS_COMMITTED);

    }

    private void checkRollback() throws XAException {
        if (this.getXAConnectionHandle().getConnection() == null ||
                this.getXAConnectionHandle().getConnection().isClosed()) {
            this.setProgressState(Status.STATUS_ROLLING_BACK);
            throw new XAException(XAException.XA_RBROLLBACK);
        }

        if (this.isRollbackOnly()) {
            this.setProgressState(Status.STATUS_ROLLING_BACK);
            throw new XAException(XAException.XA_RBROLLBACK);
        }

        if (log.isInfoEnabled()) {
            log.info("RollbackOnly " + this.rollbackOnly);
        }
    }

    synchronized int prepare() throws XAException {
        if (this.progressState == Status.STATUS_PREPARED) {
            if (this.getXAConnectionHandle().isReadOnly()) {
                return (XAResource.XA_RDONLY);
            } else {
                return XAResource.XA_OK;
            }
        }

        // check if resource is READONLY -> no prepare is required
        if (this.getXAConnectionHandle().isReadOnly()) {
            this.setProgressState(Status.STATUS_PREPARED);
            return (XAResource.XA_RDONLY);
        }


        if (this.progressState == Status.STATUS_PREPARING) {
            throw new IllegalStateException("XAResource already preparing");
        }
        if (!checkTXRequirements()) {
            throw new SampleTransactionalException("State not transactional " + this);
        }

        if (this.hasHeuristicOutcome()) {
            throw new XAException(this.heuristicState);
        }
        this.checkRollback();
        this.setProgressState(Status.STATUS_PREPARING);
        this.getCurrentConnection().prepare();
        this.setProgressState(Status.STATUS_PREPARED);

        // anything to commit?
        if (this.getXAConnectionHandle().isReadOnly()) {
            return (XAResource.XA_RDONLY);
        } else {
            return XAResource.XA_OK;
        }
    }


    synchronized void rollback() throws XAException {
        if (!checkTXRequirements()) {
            log.error("State not transactional " + this);
            throw new XAException(XAException.XAER_PROTO);
        }

        log.debug("XAResourceTxState:rollback for xid=" + xid + " current Status=" + ConstantsPrinter.getStatusMessage(this.getProgressState()));
        switch (this.getProgressState()) {
            case Status.STATUS_PREPARED: // ready to do rollback
            case Status.STATUS_ACTIVE:
                try {
                    log.debug(
                            "XAResourceTxState:rollback try to perform the rollback operation");
                    this.setProgressState(Status.STATUS_ROLLING_BACK);

                    // do the rollback
                    if (this.getXAConnectionHandle().getConnection() != null &&
                            !this.getXAConnectionHandle().getConnection().isClosed()) {
                        this.getCurrentConnection().rollback();
                    } else {
                        log.info("XAResourceTxState connection already closed -> no rollback");
                    }

                    this.setProgressState(Status.STATUS_ROLLEDBACK);
                    // perform the rollback operation
                    log.debug(
                            "XAResourceTxState:rollback performed the rollback");
                } catch (Exception e) {
                    log.error("Error in " + this + " \n" + e.getMessage());
                    throw new XAException(XAException.XAER_PROTO);
                    // rollback will have been performed
                }
                break;
            default:
                log.error("XAResourceTxState : rollback not permitted on TX state " + ConstantsPrinter.getStatusMessage(this.getProgressState()) + " XAResoureTXState=" + this);
                throw new XAException(XAException.XAER_RMFAIL);
        }


    }


    /**
     * checks, if the current state complies with the requirements of an active TX
     *
     * @return
     */
    private boolean checkTXRequirements() {
        if (this.getCurrentConnection() == null) {
            return false;
        }
        if (!isActive()) {
            return false;
        }

        return true;
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer("XAResourceTxState ");
        buffer.append(" connected to " + xaConnectionHandle + " enlisted in TX " + this.xid + " Active=" + this.isActive() + " ProgressState=" + ConstantsPrinter.getStatusMessage(this.progressState));
        return buffer.toString();
    }


    synchronized void close() {
        this.xaConnectionHandle.getManagedConnectionHandle().removeConnectionListener(this);
        this.xaConnectionHandle.close();
        if (joinedXAConnections != null && joinedXAConnections.size() > 0) {
            for (Iterator iterator = joinedXAConnections.iterator(); iterator.hasNext(); ) {
                PhynixxManagedXAConnection handle = (PhynixxManagedXAConnection) iterator.next();
                handle.close();
            }
        }
    }


    public synchronized void connectionRequiresTransaction(IManagedConnectionProxyEvent event) {
    }

    /**
     * callback if the associoated connection has been closed
     *
     * @see IConnectionProxyListener.#connectionClosed(IConnectionProxyEvent)
     */
    public synchronized void connectionClosed(IManagedConnectionProxyEvent event) {
        heuristicState = XAException.XA_HEURMIX;
        this.xaConnectionHandle.getManagedConnectionHandle().removeConnectionListener(this);
    }

    /**
     * callback if the associated connection has been committed outside the scope of the XAResource
     * <p/>
     * The current State is set to 'HEURISTIC COMMIT'
     *
     * @see IConnectionProxyListener.#connectionCommitted(IConnectionProxyEvent)
     */
    public synchronized void connectionCommitted(IManagedConnectionProxyEvent event) {
        if (this.getProgressState() != Status.STATUS_COMMITTING &&
                this.getProgressState() != Status.STATUS_COMMITTED) {
            heuristicState = XAException.XA_HEURCOM;
        }
    }


    /**
     * callback if the associated connection is rollbacked outside the scope of the XAResource
     * <p/>
     * The current State is set to 'HEURISTIC ROLLBACK'
     *
     * @see IConnectionProxyListener.#connectionRolledback(IConnectionProxyEvent)
     */

    public synchronized void connectionRolledback(IManagedConnectionProxyEvent event) {
        if (this.getProgressState() != Status.STATUS_ROLLING_BACK &&
                this.getProgressState() != Status.STATUS_ROLLEDBACK) {
            heuristicState = XAException.XA_HEURRB;
        }

    }


}
