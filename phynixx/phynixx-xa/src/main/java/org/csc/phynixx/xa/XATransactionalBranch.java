package org.csc.phynixx.xa;

import org.csc.phynixx.common.exceptions.SampleTransactionalException;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.*;

import javax.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * an XID represents a transaction branch. A transaction branch represents the context a rollback/commit affects.
 * <p/>
 * A transactional branch corresponds to an physical connection
 * <p/>
 * Created by zf4iks2 on 10.02.14.
 */
class XATransactionalBranch<C extends IPhynixxConnection> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxManagedConnectionListener<C> {

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(XATransactionalBranch.class);

    private final static int SUSPENDED = 0x02000000;
    private final static int ACTIVE = 0x04000000; // opposite of suspended

    private int progressState = Status.STATUS_ACTIVE;

    private boolean readOnly = true;


    private int activeState = ACTIVE;
    private volatile boolean rollbackOnly = false;

    private int heuristicState = 0;

    private Xid xid;

    private IPhynixxManagedConnection<C> managedConnection;

    XATransactionalBranch(Xid xid, IPhynixxManagedConnection<C> managedConnection) {
        this.xid = xid;
        this.managedConnection = managedConnection;
        this.managedConnection.addConnectionListener(this);
    }

    Xid getXid() {
        return xid;
    }

    IPhynixxManagedConnection<C> getManagedConnection() {
        return managedConnection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XATransactionalBranch that = (XATransactionalBranch) o;

        if (!xid.equals(that.xid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return xid.hashCode();
    }

    void suspend() {
        if (this.activeState == XATransactionalBranch.SUSPENDED) {
            return;
        }
        if (this.progressState != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("A already prepared or preparing TX can not be suspended");

        }
        this.activeState = SUSPENDED;
    }

    void resume() {
        if (this.activeState == XATransactionalBranch.ACTIVE) {
            return;
        }
        this.activeState = XATransactionalBranch.ACTIVE;
    }

    public boolean isActive() {
        return this.getActiveState() == XATransactionalBranch.ACTIVE;
    }

    boolean isSuspended() {
        return this.getActiveState() == XATransactionalBranch.SUSPENDED;
    }

    boolean isReadOnly() {
        return readOnly;
    }

    void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    private int getActiveState() {
        return activeState;
    }

    void setRollbackOnly(boolean rbOnly) {
        this.rollbackOnly = rbOnly;
        //new Exception("Set to " + rbOnly+" ObjectId"+super.toString()).printStackTrace();
    }

    boolean isRollbackOnly() {
        // new Exception("Read " + this.rollbackOnly+" ObjectId"+super.toString()).printStackTrace();
        return rollbackOnly;
    }

    int getProgressState() {
        return progressState;
    }

    private void setProgressState(int progressState) {
        this.progressState = progressState;
    }


    boolean hasHeuristicOutcome() {
        return (this.heuristicState > 0);
    }

    int getHeuristicState() {
        return this.heuristicState;
    }


    /**
     * maintains the state of the transactional branch if a commit is executed.
     * <p/>
     * The commit is executed
     *
     * @param onePhase
     * @throws XAException
     */
    void commit(boolean onePhase) throws XAException {
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
        this.getManagedConnection().commit();
        this.setProgressState(Status.STATUS_COMMITTED);

    }

    private void checkRollback() throws XAException {
        if (this.getManagedConnection().getCoreConnection() == null ||
                this.getManagedConnection().getCoreConnection().isClosed()) {
            this.setProgressState(Status.STATUS_ROLLING_BACK);
            throw new XAException(XAException.XA_RBROLLBACK);
        }

        if (this.isRollbackOnly()) {
            this.setProgressState(Status.STATUS_ROLLING_BACK);
            throw new XAException(XAException.XA_RBROLLBACK);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("RollbackOnly " + this.rollbackOnly);
        }
    }

    int prepare() throws XAException {
        if (this.progressState == Status.STATUS_PREPARED) {
            if (this.isReadOnly()) {
                return (XAResource.XA_RDONLY);
            } else {
                return XAResource.XA_OK;
            }
        }

        // check if resource is READONLY -> no prepare is required
        if (this.isReadOnly()) {
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
        this.getManagedConnection().prepare();
        this.setProgressState(Status.STATUS_PREPARED);

        // anything to commit?
        if (this.isReadOnly()) {
            return (XAResource.XA_RDONLY);
        } else {
            return XAResource.XA_OK;
        }
    }


    void rollback() throws XAException {
        if (!checkTXRequirements()) {
            LOG.error("State not transactional " + this);
            throw new XAException(XAException.XAER_PROTO);
        }

        LOG.debug("XAResourceTxState:rollback for xid=" + xid + " current Status=" + ConstantsPrinter.getStatusMessage(this.getProgressState()));
        switch (this.getProgressState()) {
            case Status.STATUS_PREPARED: // ready to do rollback
            case Status.STATUS_ACTIVE:
                try {
                    LOG.debug(
                            "XAResourceTxState:rollback try to perform the rollback operation");
                    this.setProgressState(Status.STATUS_ROLLING_BACK);

                    // do the rollback
                    if (this.getManagedConnection() != null &&
                            !this.getManagedConnection().isClosed()) {
                        this.getManagedConnection().rollback();
                    } else {
                        LOG.info("XAResourceTxState connection already closed -> no rollback");
                    }

                    this.setProgressState(Status.STATUS_ROLLEDBACK);
                    // perform the rollback operation
                    LOG.debug(
                            "XAResourceTxState:rollback performed the rollback");
                } catch (Exception e) {
                    LOG.error("Error in " + this + " \n" + e.getMessage());
                    throw new XAException(XAException.XAER_PROTO);
                    // rollback will have been performed
                }
                break;
            default:
                LOG.error("XAResourceTxState : rollback not permitted on TX state " + ConstantsPrinter.getStatusMessage(this.getProgressState()) + " XAResoureTXState=" + this);
                throw new XAException(XAException.XAER_RMFAIL);
        }


    }


    /**
     * checks, if the current state complies with the requirements of an active TX
     *
     * @return
     */
    private boolean checkTXRequirements() {
        if (this.getManagedConnection() == null) {
            return false;
        }
        if (!isActive()) {
            return false;
        }

        return true;
    }

    public void close() {
        this.getManagedConnection().close();
    }


    @Override
    public void connectionRequiresTransaction(IManagedConnectionProxyEvent<C> event) {
        this.setRollbackOnly(false);
    }

    @Override
    public void connectionClosed(IManagedConnectionProxyEvent<C> event) {
        event.getManagedConnection().removeConnectionListener(this);
    }
}
