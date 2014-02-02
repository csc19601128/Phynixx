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


import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.exceptions.ExceptionUtils;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.watchdog.ITimeoutCondition;
import org.csc.phynixx.watchdog.TimeoutCondition;
import org.csc.phynixx.watchdog.log.ConditionViolatedLog;
import org.csc.phynixx.xa.IPhynixxXAResourceListener.IPhynixxXAResourceEvent;

import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A XAresourec can be initialized with a connection. This connection is used,
 * the first time an TX is opened on the XAResource.
 * <p/>
 * Any following TX get an new connection
 *
 * @author christoph
 */
public class PhynixxXAResource implements XAResource {


    private static final long DEFAULT_TIMEOUT = Long.MAX_VALUE; // msecs - no time out at all


    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private Object xaId = null;

    private TransactionManager tmMgr = null;

    private PhynixxResourceFactory factory = null;

    /**
     * @supplierCardinality 0..1
     */
    private PhynixxManagedXAConnection currentXAConnectionHandle = null;

    private volatile boolean closed = false;

    private ITimeoutCondition timeoutCondition = null;

    public PhynixxXAResource(
            String xaId,
            TransactionManager tmMgr,
            PhynixxResourceFactory factory) {
        this(xaId, tmMgr, factory, null);
    }

    public PhynixxXAResource(
            Object xaId,
            TransactionManager tmMgr,
            PhynixxResourceFactory factory,
            IPhynixxManagedConnection connectionProxy) {
        this.xaId = xaId;
        this.tmMgr = tmMgr;
        this.factory = factory;
        if (connectionProxy != null) {
            this.currentXAConnectionHandle = new PhynixxManagedXAConnection(this, tmMgr, connectionProxy);
        } else {
            this.currentXAConnectionHandle = null;
        }

        // start a watchdog to watch the timeout ...
        // this condition is not active
        this.timeoutCondition = new TimeoutCondition(DEFAULT_TIMEOUT) {
            public void conditionViolated() {
                PhynixxXAResource.this.conditionViolated();
            }
        };
        // synchronize the watched condition
        this.timeoutCondition = this.timeoutCondition;

        // it is registered at the resource factory's watchdog  ....
        factory.registerWatchCondition(this.timeoutCondition);
    }


    /**
     * called when the current XAresource is expired (time out occurred)
     * The current Impl. does nothing but marked the associated TX as rollback only
     * The underlying core connection are not treated ....
     * <p/>
     * There are two different situation that have to be handled
     * If the connection expires because of a long running method call the connection isn't
     * treated and the XAResource is marked as rollback only. The rollback is done by the Transactionmanager.
     * If the TX expires because the Transactionmanagers doesn't answer, the underlying connection is rolledback
     * and the XAResource is marked as rollback only (N.B. rolling back the connection
     * marks the XAResource as heuristically rolled back)
     */
    public void conditionViolated() {
        try {
            List statecons =
                    factory.getXAResourceTxStateManager().getXAResourceTxStates(this);
            if (statecons == null || statecons.size() == 0) {
                return;
            }

            // all associated TX are marked as rollback ...
            for (int i = 0; i < statecons.size(); i++) {
                XAResourceTxState statecon = (XAResourceTxState) statecons.get(i);
                statecon.setRollbackOnly(true);
                IPhynixxManagedConnection ch =
                        statecon.getXAConnectionHandle().getConnectionHandle();

            }

            if (log.isInfoEnabled()) {
                String logString = "SampleXAResource.expired :: XAResource " + this.getId() + " is expired (time out occurred) and all associated TX are rollbacked ";
                log.info(new ConditionViolatedLog(this.timeoutCondition, logString).toString());
            }

        } finally {
            // no monitoring anymore
            this.timeoutCondition.setActive(false);
        }

    }

    public IPhynixxXAConnection getXAConnection() {
        if (this.currentXAConnectionHandle != null) {
            return currentXAConnectionHandle;
        } else {
            List xaResTates = this.factory.getXAResourceTxStateManager().getXAResourceTxStates(this);
            // check if there is any active TX associated with the current XAREsource
            for (Iterator iterator = xaResTates.iterator(); iterator.hasNext(); ) {
                XAResourceTxState state = (XAResourceTxState) iterator.next();
                if (state.isActive()) {
                    return state.getXAConnectionHandle();
                }
            }
        }
        return null;
    }

    public boolean isClosed() {
        return closed;
    }


    public void commit(Xid xid, boolean onePhase) throws XAException {
        if (xid == null)
            throw new XAException(XAException.XAER_INVAL);

        try {

            XAResourceTxState statecon =
                    factory.getXAResourceTxStateManager().getXAResourceTxState(xid);
            if (statecon == null) {
                throw new XAException(XAException.XAER_INVAL);
            }
            // Check if the XAresource is suspended
            if (!statecon.isActive()) {
                throw new XAException(XAException.XAER_INVAL);
            }

            statecon.commit(onePhase);
        } catch (XAException xaExc) {
            log.error("SampleXAResource[" + this.getId() + "]:end xid='"
                    + xid
                    + "' onePhase='" + onePhase
                    + " ERROR  " + ConstantsPrinter.getXAErrorCode(xaExc.errorCode));
            throw xaExc;

        } catch (Exception ex) {
            log.error("SampleXAResource.commit(" + xid + "," + onePhase + ") on Resource " + this.xaId + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
            throw new DelegatedRuntimeException("commit(" + xid + "," + onePhase + ") on Resource " + this.xaId, ex);
        } finally {
            try {
                factory.getXAResourceTxStateManager().deregisterConnection(xid);
            } finally {
                // stop monitoring the timeout
                this.timeoutCondition.setActive(false);

            }
        }

    }

    /**
     * This method ends the work performed on behalf of a transaction branch.
     * The resource manager dissociates
     * the XA resource from the transaction branch specified and let the transaction
     * be completed.
     * If TMSUSPEND is specified in flags, the transaction branch is temporarily suspended
     * in incomplete state.
     * The transaction context is in suspended state and must be resumed via start
     * with TMRESUME specified.
     * <p/>
     * If TMFAIL is specified, the portion of work has failed. The resource manager
     * may mark the transaction as rollback only.
     * <p/>
     * If TMSUCCESS is specified, the portion of work has completed successfully.
     * end is called in Transaction.delistResource
     *
     * @param xid      A global transaction identifier.
     * @param onePhase If true, the resource manager should use a one-phase commit protocol
     *                 to commit the work done on behalf of xid.
     * @throws: XAException An error has occurred. Possible XAException values
     * are XAER_RMERR, XAER_RMFAIL,XAER_NOTA, XAER_INVAL, XAER_PROTO,XA_RB*.
     */
    public void end(Xid xid, int flags) throws XAException {
        try {
            //not tested XS
            log.debug("SampleXAResource:end");
            log.debug(
                    "SampleXAResource[" + this.getId() + "]:end xid='" + xid + "' flags='" + ConstantsPrinter.getXAResourceMessage(flags) + "'");

            if (xid == null) {
                throw new XAException(XAException.XAER_INVAL);
            }

            XAResourceTxState statecon =
                    factory.getXAResourceTxStateManager().getXAResourceTxState(xid);

            // must find connection for this transaction
            if (statecon == null || statecon.getProgressState() != Status.STATUS_ACTIVE) // must have had start() called
                throw new XAException(XAException.XAER_PROTO);

            if (flags == TMSUSPEND) {
                statecon.suspend();

            }

			/*else if ( flags==TMSUCCESS) {
                factory.getXAResourceTxStateManager().deregisterConnection(xid);
			}*/

        } catch (XAException xaExc) {
            log.error("SampleXAResource[" + this.getId() + "]:end xid='"
                    + xid
                    + "' flags='"
                    + ConstantsPrinter.getXAResourceMessage(flags)
                    + "'"
                    + " ERROR  " + ConstantsPrinter.getXAErrorCode(xaExc.errorCode));
            throw xaExc;
        } catch (Exception ex) {
            log.error("SampleXAResource.end(" + xid + "," + flags + ") on Resource " + this.xaId + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
            throw new DelegatedRuntimeException("end(" + xid + "," + flags + ") on Resource " + this.xaId, ex);

        }
    }

    public void forget(Xid xid) throws XAException {
        try {
            log.debug("SampleXAResource[" + this.getId() + "]:forget forget with Xid");
            if (xid == null)
                throw new XAException(XAException.XAER_INVAL);

            factory.getXAResourceTxStateManager().deregisterConnection(xid);
            // finished with this transaction

        } finally {
            // stop monitoring the timeout
            this.timeoutCondition.setActive(false);

        }
    }

    public int getTransactionTimeout() throws XAException {
        return (int) (this.timeoutCondition.getTimeout()) * 1000;
    }

    /**
     * This method is called to determine if the resource
     * manager instance represented by the target object is the same
     * as the resource manager instance represented by the parameter xares .
     * <p/>
     * The resource manager is reresented by the Resourcefactory
     *
     * @param xares An XAResource object.
     * @return true if same RM instance; otherwise false.
     */

    public boolean isSameRM(XAResource xares) throws XAException {
        if (this.equals(xares)) { // if the same object
            log.debug("SampleXAResource[" + this.getId() + "]:isSameRM isSameRM");
            return true; // then definitely the same RM
        }
        if (!(xares instanceof PhynixxXAResource)) {
            // if it's not one of our wrappers
            log.debug("SampleXAResource[" + this.getId() + "]:isSameRM not isSameRM");
            return false; // then it's definitely not the same RM
        }
        PhynixxXAResource sampleXARes = (PhynixxXAResource) xares;

        try {
            // cast to something more convenient
            if (factory.equals(sampleXARes.factory)) {
                // if they originate from same data source
                log.debug(
                        "SampleXAResource[" + this.getId() + "]:isSameRM isSameRM (equal XAResourceFactory)");
                return true; // then they're the same RM
            } else {
                log.debug(
                        "SampleXAResource[" + this.getId() + "]:isSameRM not isSameRM (not equal XAResourceFactory)");
                return false;
            }
        } catch (Exception ex) {
            log.error("SampleXAResource.isSameRM(" + sampleXARes.xaId + ") on Resource " + this.xaId + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
            throw new DelegatedRuntimeException("isSameRM(" + sampleXARes.xaId + ") on Resource " + this.xaId, ex);

        }
    }

    /**
     * Prepares to perform a commit. May actually perform a commit
     * in the flag commitOnPrepare is set to true.
     * <p/>
     * This method is called to ask the resource manager to prepare for a
     * transaction commit of the transaction specified in xid.
     *
     * @param xid A global transaction identifier.
     * @return A value indicating the resource manager�s vote on the outcome of
     * the transaction. The possible values are: XA_RDONLY or XA_OK.
     * If the resource manager wants to roll back the transaction, it should do so by
     * throwing an appropriate XAException in the prepare method.
     * @throws XAException An error has occurred.
     *                     Possible exception values are:
     *                     XA_RB*, XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or XAER_PROTO.
     */
    public int prepare(Xid xid) throws XAException {
        try {
            log.debug(
                    "SampleXAResource[" + this.getId() + "]:prepare prepare to perform a commit for XID=" + xid);

            if (xid == null)
                throw new XAException(XAException.XAER_INVAL);

            XAResourceTxState statecon =
                    factory.getXAResourceTxStateManager().getXAResourceTxState(xid);
            // must find connection for this transaction
            int retVal = statecon.prepare();
            return retVal;

        } catch (XAException xaExc) {
            log.error("SampleXAResource[" + this.getId() + "]:prepare xid='"
                    + xid
                    + " ERROR  " + ConstantsPrinter.getXAErrorCode(xaExc.errorCode));
            throw xaExc;
        } catch (Exception ex) {
            log.error("SampleXAResource.prepare(" + xid + ") on Resource " + this.xaId + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
            throw new DelegatedRuntimeException("prepare(" + xid + ") on Resource " + this.xaId, ex);

        }
    }

    /**
     * Close this XA Resource.
     * All depending Connection are closed
     */
    public void close() {
        // check if the XAresource is assigned to the TX
        List statecons =
                factory.getXAResourceTxStateManager().getXAResourceTxStates(this);
        if (statecons != null && statecons.size() > 0) {
            for (Iterator iterator = statecons.iterator(); iterator.hasNext(); ) {
                XAResourceTxState statecon = (XAResourceTxState) iterator.next();
                factory.getXAResourceTxStateManager().deregisterConnection(statecon.getXid());
            }
        }
        this.closed = true;
        this.notifyClosed();

        log.debug("SampleXAResource[" + this.getId() + "]:closed ");
    }

    /**
     * the system is recovered by the factory representing the persistence management system,
     */
    public Xid[] recover(int flags) throws XAException {
        log.info("SampleXAResource[" + this.getId() + "]:recover recover flags=" + ConstantsPrinter.getXAResourceMessage(flags));
        if (flags != TMSTARTRSCAN && flags != TMENDRSCAN && flags != TMNOFLAGS) {
            throw new XAException(XAException.XAER_INVAL);
        }

        Xid[] retval = null;
        retval = this.factory.recover(); // get all valid Xids
        return retval;
    }


    /*
     * This method informs the resource manager to roll back
     * work done on behalf of a transaction branch.
     * Upon return, the resource manager has rolled back the branch�s work and
     * has released all held resources.
     *
     * @param xid A global transaction identifier.
     * @throws XAException An error has occurred.
     *    Possible XAExceptions are
     *       XA_HEURHAZ, XA_HEURCOM,XA_HEURRB, XA_HEURMIX,
     *       XAER_RMERR, XAER_RMFAIL, XAER_NOTA,XAER_INVAL, or XAER_PROTO.
     *
     *
     * @see javax.transaction.xa.XAResource#rollback(javax.transaction.xa.Xid)
     */
    public void rollback(Xid xid) throws XAException {
        {
            try {
                log.debug("SampleXAResource[" + this.getId() + "]:rollback started xid=" + xid);
                if (xid == null)
                    throw new XAException(XAException.XAER_INVAL);

                XAResourceTxState statecon =
                        factory.getXAResourceTxStateManager().getXAResourceTxState(xid);

                if (statecon == null) {
                    return; // already rollbacked ...
                }

                if (!statecon.isActive()) {
                    throw new XAException(XAException.XAER_INVAL);
                }

                try {
                    statecon.rollback();
                } finally {
                    factory.getXAResourceTxStateManager().deregisterConnection(xid);
                }


            } catch (XAException xaExc) {
                log.error("SampleXAResource[" + this.getId() + "]:rollback xid='"
                        + xid
                        + " ERROR  " + ConstantsPrinter.getXAErrorCode(xaExc.errorCode));
                throw xaExc;

            } catch (Exception ex) {
                log.error("SampleXAResource.rollback(" + xid + ") on Resource " + this.xaId + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
                throw new DelegatedRuntimeException("rollback(" + xid + ") on Resource " + this.xaId, ex);

            } finally {
                // stop monitoring the timeout
                this.timeoutCondition.setActive(false);

            }
        }


    }

    /**
     * This method sets the transaction timeout value for this XAResource instance.
     * Once set, this timeout value is effective until setTransactionTimeout
     * is invoked again with a different value.
     * To reset the timeout value to the default value used by the resource manager,
     * set the value to zero.
     * If the timeout operation is performed successfully, the method returns true; otherwise false.
     * If a resource manager does not support transaction timeout value to be set explicitly,
     * this method returns false.
     *
     * @param seconds An positive integer specifying the timout value in seconds.
     *                Zero resets the transaction timeout value to the default one used
     *                by the resource manager. A negative value results in XAExceptio to be thrown
     *                with XAER_INVAL error code.
     * @throws XAException An error has occurred. Possible exception values are: XAER_RMERR, XAER_RMFAIL, or XAER_INVAL.
     * @returns true if transaction timeout value is set successfully; otherwise false.
     */
    public boolean setTransactionTimeout(int seconds) throws XAException {
        if (seconds < 0) {
            throw new XAException(XAException.XAER_INVAL);
        }
        long msecs = seconds * 1000;
        if (seconds == 0) {
            msecs = DEFAULT_TIMEOUT;
        }
        this.timeoutCondition.resetCondition(msecs);

        return true;

    }

    /**
     * This method starts work on behalf of a transaction branch.
     * <p/>
     * If TMJOIN is specified, start is for joining an exisiting transaction
     * branch xid.
     * <p/>
     * If TMRESUME is specified, start is to resume a suspended transaction branch
     * specified in xid.
     * <p/>
     * If neither TMJOIN nor TMRESUME is specified and the transaction branch specified in
     * xid already exists, the resource manager throw the XAException
     * with XAER_DUPID error code.
     * <p/>
     * If the XAResource has a current connection  and flags ==TMJOIN/TMRESUME, the current connection is
     * substituted by the connection of the existing TX.
     *
     * @param xid   A global transaction identifier to be associated with the resource.
     * @param flags One of TMNOFLAGS, TMJOIN, or TMRESUME.
     * @throws: XAException An error has occurred.
     * Possible exceptions are XA_RB*, XAER_RMERR, XAER_RMFAIL, XAER_DUPID, XAER_OUTSIDE, XAER_NOTA, XAER_INVAL, or XAER_PROTO.
     */
    public void start(Xid xid, int flags) throws XAException {
        try {

            if (xid == null) {
                throw new XAException(XAException.XAER_INVAL);
            }

            XAResourceTxState statecon = null;

            /**
             * An existing TX has to be joined with the current connection
             */
            if (flags == TMRESUME || flags == TMJOIN) {

                // if resuming or joining an existing transaction
                statecon =
                        factory.getXAResourceTxStateManager().getXAResourceTxState(xid);
                if (statecon == null) {
                    throw new XAException(XAException.XAER_INVAL);
                }
                // the current connection is substituted by the connection used by the
                // existing TX
                // TODO  wird TMJOIN korrekt behandelt
                if (this.currentXAConnectionHandle != null) {
                    statecon.join(this.currentXAConnectionHandle);
                }
                if (flags == TMRESUME) {
                    statecon.resume();
                }

            } else {
                // check if there are any transaction contexts associated with the current XAResource
                List<XAResourceTxState> statecons = factory.getXAResourceTxStateManager().getXAResourceTxStates(this);
                for (int i = 0; i < statecons.size(); i++) {
                    XAResourceTxState sc = statecons.get(i);
                    if (!sc.isSuspended()) {
                        throw new XAException(XAException.XA_RBINTEGRITY);
                    }
                }

                PhynixxManagedXAConnection handle = null;
                if (this.currentXAConnectionHandle != null) {
                    handle = this.currentXAConnectionHandle;
                    this.currentXAConnectionHandle = null; // used
                } else {
                    // get a new connection
                    IPhynixxManagedConnection con;
                    try {
                        con = this.factory.getConnection();
                    } catch (Exception e) {
                        throw new DelegatedRuntimeException(e);
                    }
                    handle = new PhynixxManagedXAConnection(this, this.tmMgr, con);
                }
                handle.associateTransaction();
                statecon =
                        factory.getXAResourceTxStateManager().getXAResourceTxState(xid);
                if (statecon != null) {
                    throw new XAException(XAException.XAER_DUPID);
                }

                statecon = new XAResourceTxState(xid, handle);
            }
            log.debug(
                    "SampleXAResource[" + this.getId() + "]:start xid='"
                            + xid
                            + "' flags='"
                            + ConstantsPrinter.getXAResourceMessage(flags)
                            + "'"
                            + " Connected to " +
                            statecon.getXAConnectionHandle());


            if (factory.getXAResourceTxStateManager().getXAResourceTxState(xid) == null) {
                factory.getXAResourceTxStateManager().registerConnection(statecon);
            }

            // start monitoring the timeout
            this.timeoutCondition.setActive(true);

        } catch (XAException xaExc) {
            log.error("SampleXAResource[" + this.getId() + "]:start xid='"
                    + xid
                    + "' flags='"
                    + ConstantsPrinter.getXAResourceMessage(flags)
                    + "'"
                    + " ERROR  " + ConstantsPrinter.getXAErrorCode(xaExc.errorCode));
            throw xaExc;
        } catch (Exception ex) {
            log.error("SampleXAResource.start(" + xid + "," + flags + ") on Resource " + this.xaId + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
            throw new DelegatedRuntimeException("start(" + xid + "," + flags + ") on Resource " + this.xaId, ex);

        }
    }

    public final boolean equals(Object obj) {
        return super.equals(obj);
    }


    public final int hashCode() {
        return super.hashCode();
    }

    public Object getId() {
        return this.xaId;
    }

	/*
    public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("SampleXAResource "+this.xaId+" :\n");
		sb.append("     is closed =<"+this.isClosed() + ">\n");
		sb.append("     next timeOut =<"+this.nextTimeout + ">\n");
		sb.append("     timeOut period =<"+this.timeoutPeriod + ">\n");
		sb.append("     timeOut secs =<"+this.timeoutSecs + ">\n");
		sb.append("     transaction manager=<"+this.tmMgr + ">\n");
		return sb.toString();	
	}
	*/

    public String toString() {
        return this.factory.getId().toString() + "." + this.xaId.toString();
    }


    private List listeners = new ArrayList();

    public void addXAResourceListener(IPhynixxXAResourceListener listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeXAResourceListener(IPhynixxXAResourceListener listener) {

        this.listeners.remove(listener);
    }

    private void notifyClosed() {
        IPhynixxXAResourceEvent event = new PhynixxXAResourceEvent(this);
        for (int i = 0; i < listeners.size(); i++) {
            IPhynixxXAResourceListener listener = (IPhynixxXAResourceListener) listeners.get(i);
            listener.closed(event);
        }

    }


}
