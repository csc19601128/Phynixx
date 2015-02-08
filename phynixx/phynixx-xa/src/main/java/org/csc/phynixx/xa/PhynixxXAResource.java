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

import java.util.ArrayList;
import java.util.List;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.exceptions.ExceptionUtils;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.watchdog.ITimeoutCondition;
import org.csc.phynixx.watchdog.TimeoutCondition;
import org.csc.phynixx.watchdog.log.ConditionViolatedLog;
import org.csc.phynixx.xa.IPhynixxXAResourceListener.IPhynixxXAResourceEvent;

/**
 * A transactional branch may be associated with differnt XAResources (TMJOIN)
 * or a XAResource may be shared with different transaction branches
 * <p/>
 * Therefore a transactional branch might be associated to many XAResources or
 * to many transactions (==XIDs)
 * <p/>
 * A XAresourec can be initialized with a connection. This connection is used,
 * the first time an TX is opened on the XAResource.
 * <p/>
 * Any following TX get an new connection
 *
 * @author christoph
 */
public class PhynixxXAResource<C extends IPhynixxConnection> implements IPhynixxXAResource<C> {

    private static final long DEFAULT_TIMEOUT = Long.MAX_VALUE; // msecs - no
                                                                // time out at
                                                                // all

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(PhynixxXAResource.class);

    private Object xaId = null;

    private PhynixxXAResourceFactory<C> xaResourceFactory = null;

    /**
     * @supplierCardinality 0..1
     */
    private PhynixxManagedXAConnection<C> xaConnection = null;

    private volatile boolean closed = false;

    private ITimeoutCondition timeoutCondition = null;

    private boolean supportsTimeOut = false;

    /**
     * TODO timeOut ueber einen Listener steuern und konfigierbar machen
     */
    public PhynixxXAResource(String xaId, TransactionManager transactionManager,
            PhynixxXAResourceFactory<C> xaResourceFactory) {
        this.xaId = xaId;
        this.xaResourceFactory = xaResourceFactory;
        this.xaConnection = new PhynixxManagedXAConnection<C>(this, transactionManager,
                xaResourceFactory.getXATransactionalBranchRepository(), xaResourceFactory.getManagedConnectionFactory());

        // start a watchdog to watch the timeout ...
        // this condition is not active
        if (this.isSupportsTimeOut()) {
            this.timeoutCondition = new TimeoutCondition(DEFAULT_TIMEOUT) {
                @Override
                public void conditionViolated() {
                    PhynixxXAResource.this.conditionViolated();
                }
            };
            // it is registered at the resource xaResourceFactory's watchdog
            // ....
            xaResourceFactory.registerWatchCondition(this.timeoutCondition);
        }

    }

    /**
     * called when the current XAresource is expired (time out occurred) The
     * current Impl. does nothing but marked the associated TX as rollback only
     * The underlying core connection are not treated ....
     * <p/>
     * There are two different situation that have to be handled If the
     * connection expires because of a long running method call the connection
     * isn't treated and the XAResource is marked as rollback only. The rollback
     * is done by the Transactionmanager. If the TX expires because the
     * Transactionmanagers doesn't answer, the underlying connection is
     * rolledback and the XAResource is marked as rollback only (N.B. rolling
     * back the connection marks the XAResource as heuristically rolled back)
     */
    public void conditionViolated() {
        try {
            if (this.xaConnection != null) {
                XATransactionalBranch<C> transactionalBranch = this.xaConnection.toGlobalTransactionBranch();
                if (transactionalBranch != null) {
                    transactionalBranch.setRollbackOnly(true);
                }
            }
            if (LOG.isInfoEnabled()) {
                String logString = "PhynixxXAResource.expired :: XAResource " + this.getId()
                        + " is expired (time out occurred) and all associated TX are rollbacked ";
                LOG.info(new ConditionViolatedLog(this.timeoutCondition, logString).toString());
            }

        } finally {
            // no monitoring anymore
            setTimeOutActive(false);
        }

    }

    private void setTimeOutActive(boolean active) {
        if (this.timeoutCondition != null) {
            this.timeoutCondition.setActive(false);
        }
    }

    public boolean isSupportsTimeOut() {
        return supportsTimeOut;
    }

    public void setSupportsTimeOut(boolean supportsTimeOut) {
        throw new IllegalStateException("Timeout isn't yet supported. Will be in version 2.1");
    }

    /**
     * accessing the XAConnection forces the XAResource to enlist to the
     * Transaction. {@link #getXAConnection()} must be called in the appropriate
     * TransactionContext
     *
     * @return
     */
    @Override
    public IPhynixxXAConnection<C> getXAConnection() {
        return this.xaConnection;
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * transaction branch is interchangeably with transaction context
     * <p/>
     * <p>
     * situation : XAResource is associtaed with XID(1) flags=TMNOFLAGS with
     * XID(1) result : XAResource creates a new transactional context
     * </p>
     * <p/>
     * <p>
     * situation : XAResource is associtaed with XID(1) flags=TMJOIN/TNRESUME
     * with XID(1) (may be different in branch) result : XAResource joins the
     * transactional context with the XAResource already associated with XID(1)
     * If TMRESUME is specified, start is to resume a suspended transaction
     * branch specified in xid.
     * </p>
     * <p/>
     * <p/>
     * <p/>
     * <p/>
     * This method starts work on behalf of a transaction branch.
     * <p/>
     * <p/>
     * <p/>
     * If TMJOIN is specified, start is for joining an exisiting transaction
     * branch xid.
     * <p/>
     * If neither TMJOIN nor TMRESUME is specified and the transaction branch
     * specified in xid already exists, the resource manager throw the
     * XAException with XAER_DUPID error code.
     * <p/>
     * If the XAResource has a current connection and flags ==TMJOIN/TMRESUME,
     * the current connection is substituted by the connection of the existing
     * TX.
     *
     * @param xid
     *            A global transaction identifier to be associated with the
     *            resource.
     * @param flags
     *            One of TMNOFLAGS, TMJOIN, or TMRESUME.
     * @throws: XAException An error has occurred. Possible exceptions are
     *          XA_RB*, XAER_RMERR, XAER_RMFAIL, XAER_DUPID, XAER_OUTSIDE,
     *          XAER_NOTA, XAER_INVAL, or XAER_PROTO.
     */
    @Override
    public void start(Xid xid, int flags) throws XAException {
        try {

            if (xid == null) {
                throw new XAException(XAException.XAER_INVAL);
            }

            // if resuming or joining an existing transaction
            if (flags == TMRESUME) {
                this.xaConnection.resumeTransactionalBranch(xid);
            } else if (flags == TMJOIN || flags == TMNOFLAGS) {
                this.xaConnection.startTransactionalBranch(xid);
            }
            LOG.debug("PhynixxXAResource[" + this.getId() + "]:start xid='" + xid + "' flags='"
                    + ConstantsPrinter.getXAResourceMessage(flags) + "'" + " Connected to " + this.xaConnection);

            // start monitoring the timeout
            this.setTimeOutActive(true);

        } catch (XAException xaExc) {
            LOG.error("PhynixxXAResource[" + this.getId() + "]:start xid='"
                    + xid // maybe null, but doesn't matter for logging
                    + "' flags='" + ConstantsPrinter.getXAResourceMessage(flags) + "'" + " ERROR  "
                    + ConstantsPrinter.getXAErrorCode(xaExc.errorCode));
            throw xaExc;
        } catch (Exception ex) {
            LOG.error("PhynixxXAResource.start(" + xid + "," + flags + ") on XAResourceProgressState " + this.xaId
                    + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
            throw new DelegatedRuntimeException("start(" + xid + "," + flags + ") on XAResourceProgressState "
                    + this.xaId, ex);

        }
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {

        XATransactionalBranch<C> transactionalBranch = this.xaConnection.toGlobalTransactionBranch();
        if (xid == null) {
            LOG.error("No XID");
            throw new XAException(XAException.XAER_INVAL);
        }

        if (transactionalBranch == null) {
            LOG.error("XAConnection is not associated to a global Transaction (expected to XID=" + xid + ")");
            throw new XAException(XAException.XAER_PROTO);
        }

        // assert that the current xaConnection is associated to this XID
        if (!transactionalBranch.getXid().equals(xid)) {
            LOG.error("XAResource " + this + " isnt't active for XID=" + xid);
            throw new XAException(XAException.XAER_PROTO);
        }

        // Find the current Branch

        try {
            transactionalBranch.commit(onePhase);
        } catch (XAException xaExc) {
            LOG.error("PhynixxXAResource[" + this.getId() + "]:end xid='" + xid + "' onePhase='" + onePhase
                    + "            ERROR  " + ConstantsPrinter.getXAErrorCode(xaExc.errorCode));
            throw xaExc;

        } catch (Exception ex) {
            LOG.error("PhynixxXAResource.commit(" + xid + "," + onePhase + ") on XAResourceProgressState " + this.xaId
                    + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
            throw new DelegatedRuntimeException("commit(" + xid + "," + onePhase + ") on XAResourceProgressState "
                    + this.xaId, ex);
        } finally {
            try {
                // Branch isn't active for the current XAresource
                this.xaConnection.closeTransactionalBranch(xid);
                this.xaConnection.close();
            } finally {
                // stop monitoring the timeout
                this.setTimeOutActive(false);

            }
        }

    }

    /**
     * finds the transactional branch of the current XAResource associated with
     * die XID
     * <p/>
     * Prepares to perform a commit. May actually perform a commit in the flag
     * commitOnPrepare is set to true.
     * <p/>
     * This method is called to ask the resource manager to prepare for a
     * transaction commit of the transaction specified in xid.
     *
     * @param xid
     *            A global transaction identifier.
     * @return A value indicating the resource manager's vote on the outcome of
     *         the transaction. The possible values are: XA_RDONLY or XA_OK. If
     *         the resource manager wants to roll back the transaction, it
     *         should do so by throwing an appropriate XAException in the
     *         prepare method.
     * @throws XAException
     *             An error has occurred. Possible exception values are: XA_RB*,
     *             XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or
     *             XAER_PROTO.
     */
    @Override
    public int prepare(Xid xid) throws XAException {
        try {
            LOG.debug("PhynixxXAResource[" + this.getId() + "]:prepare prepare to perform a commit for XID=" + xid);

            XATransactionalBranch<C> transactionalBranch = this.xaConnection.toGlobalTransactionBranch();
            if (xid == null) {
                LOG.error("No XID");
                throw new XAException(XAException.XAER_INVAL);
            }

            if (transactionalBranch == null) {
                LOG.error("XAConnection is not associated to a global Transaction");
                throw new XAException(XAException.XAER_PROTO);
            }

            // assert that the current xaConnection is associated to this XID
            if (!transactionalBranch.getXid().equals(xid)) {
                LOG.error("XAResource " + this + " isnt't active for XID=" + xid);
                throw new XAException(XAException.XAER_PROTO);
            }

            // must find connection for this transaction
            int retVal = transactionalBranch.prepare();

            if (retVal == XAResource.XA_RDONLY) {
                this.xaConnection.closeTransactionalBranch(xid);
            }
            return retVal;

        } catch (XAException xaExc) {
            LOG.error("PhynixxXAResource[" + this.getId() + "]:prepare xid='" + xid + " ERROR  "
                    + ConstantsPrinter.getXAErrorCode(xaExc.errorCode));
            throw xaExc;
        } catch (Exception ex) {
            LOG.error("PhynixxXAResource.prepare(" + xid + ") on XAResourceProgressState " + this.xaId + " :: " + ex
                    + "\n" + ExceptionUtils.getStackTrace(ex));
            throw new DelegatedRuntimeException("prepare(" + xid + ") on XAResourceProgressState " + this.xaId, ex);

        }
    }

    /*
     * finds the transactional branch of the current XAResource associated with
     * die XID
     * 
     * This method informs the resource manager to roll back work done on behalf
     * of a transaction branch. Upon return, the resource manager has rolled
     * back the branch's work and has released all held resources.
     * 
     * @param xid A global transaction identifier.
     * 
     * @throws XAException An error has occurred. Possible XAExceptions are
     * XA_HEURHAZ, XA_HEURCOM,XA_HEURRB, XA_HEURMIX, XAER_RMERR, XAER_RMFAIL,
     * XAER_NOTA,XAER_INVAL, or XAER_PROTO.
     * 
     * 
     * @see javax.transaction.xa.XAResource#rollback(javax.transaction.xa.Xid)
     */
    @Override
    public void rollback(Xid xid) throws XAException {
        {
            try {
                LOG.debug("PhynixxXAResource[" + this.getId() + "]:rollback started xid=" + xid);

                XATransactionalBranch<C> transactionalBranch = this.xaConnection.toGlobalTransactionBranch();
                if (xid == null) {
                    LOG.error("No XID");
                    throw new XAException(XAException.XAER_INVAL);
                }

                if (transactionalBranch == null) {
                    LOG.error("XAConnection is not associated to a global Transaction");
                    throw new XAException(XAException.XAER_PROTO);
                }

                // assert that the current xaConnection is associated to this
                // XID
                if (!transactionalBranch.getXid().equals(xid)) {
                    LOG.error("XAResource " + this + " isnt't active for XID=" + xid);
                    throw new XAException(XAException.XAER_PROTO);
                }

                // Find the current Branch

                if (transactionalBranch.isInActive()) {
                    throw new XAException(XAException.XAER_PROTO);
                }

                try {
                    transactionalBranch.rollback();
                } finally {
                    // Branch isn't active for the current XAResource
                    this.xaConnection.closeTransactionalBranch(xid);
                    this.xaConnection.close();
                }

            } catch (XAException xaExc) {
                LOG.error("PhynixxXAResource[" + this.getId() + "]:rollback xid='" + xid + " ERROR  "
                        + ConstantsPrinter.getXAErrorCode(xaExc.errorCode));
                throw xaExc;

            } catch (Exception ex) {
                LOG.error("PhynixxXAResource.rollback(" + xid + ") on XAResourceProgressState " + this.xaId + " :: "
                        + ex + "\n" + ExceptionUtils.getStackTrace(ex));
                throw new DelegatedRuntimeException("rollback(" + xid + ") on XAResourceProgressState " + this.xaId, ex);

            } finally {
                // stop monitoring the timeout
                this.setTimeOutActive(false);

            }
        }

    }

    /**
     * This method ends the work performed on behalf of a transaction branch.
     * <p/>
     * The resource manager dissociates the XA resource from the transaction
     * branch specified and let the transaction be completed. If TMSUSPEND is
     * specified in flags, the transaction branch is temporarily suspended in
     * incomplete state. The transaction context is in suspended state and must
     * be resumed via start with TMRESUME specified.
     * <p/>
     * If TMFAIL is specified, the portion of work has failed. The resource
     * manager may mark the transaction as rollback only.
     * <p/>
     * <p>
     * the spec doesn't no fix the order of commit/rollback and end and we have
     * to take of both orders. If end comes before rollback/commit, the
     * transactional branch has to be freeze. If rollback/commit comes before
     * end, the transactional branch has to be closed
     * </p>
     * If TMSUCCESS is specified, the portion of work has completed
     * successfully. end is called in Transaction.delistResource
     *
     * @param xid
     *            A global transaction identifier.
     * @param flags
     *            If true, the resource manager should use a one-phase commit
     *            protocol to commit the work done on behalf of xid.
     * @throws: XAException An error has occurred. Possible XAException values
     *          are XAER_RMERR, XAER_RMFAIL,XAER_NOTA, XAER_INVAL,
     *          XAER_PROTO,XA_RB*.
     */
    @Override
    public void end(Xid xid, int flags) throws XAException {
        try {
            // not tested XS
            LOG.debug("PhynixxXAResource:end");
            LOG.debug("PhynixxXAResource[" + this.getId() + "]:end xid='" + xid + "' flags='"
                    + ConstantsPrinter.getXAResourceMessage(flags) + "'");

            if (xid == null) {
                LOG.error("No XID");
                throw new XAException(XAException.XAER_INVAL);
            }

            XATransactionalBranch<C> transactionalBranch = this.xaConnection.toGlobalTransactionBranch();
            if (transactionalBranch == null) {
                LOG.error("XAConnection is not associated to a global Transaction");
                throw new XAException(XAException.XAER_PROTO);
            }

            // assert that the current xaConnection is associated to this XID
            if (!transactionalBranch.getXid().equals(xid)) {
                LOG.error("XAResource " + this + " isnt't active for XID=" + xid);
                throw new XAException(XAException.XAER_PROTO);
            }

            if (flags == TMSUSPEND) {
                this.xaConnection.suspendTransactionalBranch(xid);
            } else if (flags == TMSUCCESS) {

                // XAProtocol is finsihed an the branch isn't needed any longer
                if (transactionalBranch.isXAProtocolFinished()) {
                    transactionalBranch.close();
                    LOG.debug("XAResource " + this + " closed gracefully ");
                }
            } else if (flags == TMFAIL) {
                transactionalBranch.setRollbackOnly(true);
            }

        } catch (XAException xaExc) {
            LOG.error("PhynixxXAResource[" + this.getId() + "]:end xid='" + xid + "' flags='"
                    + ConstantsPrinter.getXAResourceMessage(flags) + "'" + " ERROR  "
                    + ConstantsPrinter.getXAErrorCode(xaExc.errorCode));
            throw xaExc;
        } catch (Exception ex) {
            LOG.error("PhynixxXAResource.end(" + xid + "," + flags + ") on XAResourceProgressState " + this.xaId
                    + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
            throw new DelegatedRuntimeException("end(" + xid + "," + flags + ") on XAResourceProgressState "
                    + this.xaId, ex);
        }
    }

    /**
     * finds the transactional branch of the current XAResource associated with
     * die XID and closes it without commit or explicit rollback
     *
     * @param xid
     * @throws XAException
     */
    @Override
    public void forget(Xid xid) throws XAException {
        try {
            LOG.debug("PhynixxXAResource[" + this.getId() + "]:forget forget with Xid");
            if (xid == null)
                throw new XAException(XAException.XAER_INVAL);

            XATransactionalBranch<C> transactionalBranch = this.xaConnection.toGlobalTransactionBranch();
            // must find connection for this transaction
            if (transactionalBranch == null) {
                return; //
            }
            this.xaConnection.closeTransactionalBranch(xid);

            this.xaConnection.close();

        } finally {
            // stop monitoring the timeout
            this.setTimeOutActive(false);

        }
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return (int) (this.timeoutCondition.getTimeout()) * 1000;
    }

    /**
     * This method is called to determine if the resource manager instance
     * represented by the target object is the same as the resource manager
     * instance represented by the parameter xares .
     * <p/>
     * The resource manager is reresented by the ResourceFactory.
     *
     * @param xaResource
     *            An XAResource object.
     * @return true if same RM instance; otherwise false.
     */

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        if (this.equals(xaResource)) { // if the same object
            LOG.debug("PhynixxXAResource[" + this.getId() + "]:isSameRM isSameRM");
            return true; // then definitely the same RM
        }
        if (!(xaResource instanceof PhynixxXAResource)) {
            // if it's not one of our wrappers
            LOG.debug("PhynixxXAResource[" + this.getId() + "]:isSameRM not isSameRM");
            return false; // then it's definitely not the same RM
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        PhynixxXAResource<C> sampleXARes = (PhynixxXAResource) xaResource;

        try {
            // cast to something more convenient
            if (xaResourceFactory.equals(sampleXARes.xaResourceFactory)) {
                // if they originate from same data source
                LOG.debug("PhynixxXAResource[" + this.getId() + "]:isSameRM isSameRM (equal XAResourceFactory)");
                return true; // then they're the same RM
            } else {
                LOG.debug("PhynixxXAResource[" + this.getId() + "]:isSameRM not isSameRM (not equal XAResourceFactory)");
                return false;
            }
        } catch (Exception ex) {
            LOG.error("PhynixxXAResource.isSameRM(" + sampleXARes.xaId + ") on XAResourceProgressState " + this.xaId
                    + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
            throw new DelegatedRuntimeException("isSameRM(" + sampleXARes.xaId + ") on XAResourceProgressState "
                    + this.xaId, ex);

        }
    }

    /**
     * finds the transactional branch of the current XAResource associated with
     * die XID
     * <p/>
     * Close this XA XAResourceProgressState. All depending Connection are
     * closed
     */
    public void close() {

        if (this.xaConnection != null) {
            this.xaConnection.doClose();
        }
        this.closed = true;
        this.notifyClosed();

        if (LOG.isDebugEnabled()) {
            LOG.debug("PhynixxXAResource[" + this.getId() + "]:closed ");
        }
    }

    /**
     * the system is recovered by the xaResourceFactory representing the
     * persistence management system,
     */
    @Override
    public Xid[] recover(int flags) throws XAException {
        LOG.info("PhynixxXAResource[" + this.getId() + "]:recover recover flags="
                + ConstantsPrinter.getXAResourceMessage(flags));
        if (flags != TMSTARTRSCAN && flags != TMENDRSCAN && flags != TMNOFLAGS) {
            throw new XAException(XAException.XAER_INVAL);
        }

        Xid[] retval = null;
        retval = this.xaResourceFactory.recover(); // get all valid Xids
        return retval;
    }

    /**
     * This method sets the transaction timeout value for this XAResource
     * instance. Once set, this timeout value is effective until
     * setTransactionTimeout is invoked again with a different value. To reset
     * the timeout value to the default value used by the resource manager, set
     * the value to zero. If the timeout operation is performed successfully,
     * the method returns true; otherwise false. If a resource manager does not
     * support transaction timeout value to be set explicitly, this method
     * returns false.
     *
     * @param seconds
     *            An positive integer specifying the timout value in seconds.
     *            Zero resets the transaction timeout value to the default one
     *            used by the resource manager. A negative value results in
     *            XAExceptio to be thrown with XAER_INVAL error code.
     * @throws XAException
     *             An error has occurred. Possible exception values are:
     *             XAER_RMERR, XAER_RMFAIL, or XAER_INVAL.
     * @returns true if transaction timeout value is set successfully; otherwise
     *          false.
     */
    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {

        return false;
        /**
         * if(!this.isSupportsTimeOut()) { throw new IllegalStateException(
         * "TimeOut is not supported  --> call setSupportsTimeOut");
         * 
         * }
         * 
         * if (seconds < 0) { throw new XAException(XAException.XAER_INVAL); }
         * long msecs = seconds * 1000; if (seconds == 0) { msecs =
         * DEFAULT_TIMEOUT; } this.timeoutCondition.resetCondition(msecs);
         * 
         * return true;
         **/

    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    public Object getId() {
        return this.xaId;
    }

    /*
     * public String toString() { StringBuffer sb = new StringBuffer();
     * sb.append("PhynixxXAResource "+this.xaId+" :\n");
     * sb.append("     is closed =<"+this.isClosed() + ">\n");
     * sb.append("     next timeOut =<"+this.nextTimeout + ">\n");
     * sb.append("     timeOut period =<"+this.timeoutPeriod + ">\n");
     * sb.append("     timeOut secs =<"+this.timeoutSecs + ">\n");
     * sb.append("     transaction manager=<"+this.transactionManager + ">\n");
     * return sb.toString(); }
     */

    @Override
    public String toString() {
        return this.xaResourceFactory.getId().toString() + "." + this.xaId.toString();
    }

    private List<IPhynixxXAResourceListener<C>> listeners = new ArrayList<IPhynixxXAResourceListener<C>>();

    public void addXAResourceListener(IPhynixxXAResourceListener<C> listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeXAResourceListener(IPhynixxXAResourceListener<C> listener) {

        this.listeners.remove(listener);
    }

    private void notifyClosed() {
        IPhynixxXAResourceEvent<C> event = new PhynixxXAResourceEvent<C>(this);
        for (int i = 0; i < listeners.size(); i++) {
            IPhynixxXAResourceListener<C> listener = listeners.get(i);
            listener.closed(event);
        }

    }

}
