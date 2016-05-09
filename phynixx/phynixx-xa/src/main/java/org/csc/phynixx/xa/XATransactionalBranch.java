package org.csc.phynixx.xa;

/*
 * #%L
 * phynixx-xa
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
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

import java.lang.ref.SoftReference;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.csc.phynixx.common.exceptions.SampleTransactionalException;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IManagedConnectionEvent;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnectionListener;
import org.csc.phynixx.connection.PhynixxManagedConnectionListenerAdapter;

/**
 * an XID represents a transaction branch. A transaction branch represents the
 * context a rollback/commit affects.
 * <p/>
 * A transactional branch corresponds to an physical connection
 * <p/>
 * Created by Christoph Schmidt-Casdorff on 10.02.14.
 */
class XATransactionalBranch<C extends IPhynixxConnection> extends
		PhynixxManagedConnectionListenerAdapter<C> implements
		IPhynixxManagedConnectionListener<C> {

	private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(XATransactionalBranch.class);

	private XAResourceProgressState progressState = XAResourceProgressState.ACTIVE;

	private boolean readOnly = true;

	private XAResourceActivationState activeState = XAResourceActivationState.ACTIVE;

	private volatile boolean rollbackOnly = false;

	private int heuristicState = 0;

	private Xid xid;

	private IPhynixxManagedConnection<C> managedConnection;
	
	private SoftReference<Transaction> transactionRef; 
	
	private XAResource xaResource; 

	XATransactionalBranch(Xid xid,
			             IPhynixxManagedConnection<C> managedConnection, 
			             XAResource xaResource,
			             Transaction transaction) {
		this.xid = xid;
		this.transactionRef=new SoftReference<Transaction>(transaction);
		this.managedConnection = managedConnection;
		this.xaResource= xaResource;
		
		this.managedConnection.addConnectionListener(this);
		
		
	}

	Xid getXid() {
		return xid;
	}

	IPhynixxManagedConnection<C> getManagedConnection() {
		return managedConnection;
	}
	
	
	boolean isSame(Transaction tx, XAResource xa) throws XAException {
		Transaction transaction = this.transactionRef.get();
		if( transaction==null) {
			return false;
		}		
		return transaction.equals(tx) && (xa!=null && this.xaResource.isSameRM(xa));
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		XATransactionalBranch that = (XATransactionalBranch) o;

		if (!xid.equals(that.xid)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return xid.hashCode();
	}

	void suspend() {
		if (this.activeState == XAResourceActivationState.SUSPENDED) {
			return;
		}
		if (this.progressState != XAResourceProgressState.ACTIVE) {
			throw new IllegalStateException(
					"A already prepared or preparing TX can not be suspended");

		}
		this.activeState = XAResourceActivationState.ACTIVE;
	}

	void resume() {
		if (this.activeState == XAResourceActivationState.SUSPENDED) {
			return;
		}
		this.activeState = XAResourceActivationState.ACTIVE;
	}

	public boolean isInActive() {
		return this.getActiveState() != XAResourceActivationState.ACTIVE;
	}

	public boolean isXAProtocolFinished() {
		return !this.isProgressStateIn(XAResourceProgressState.ACTIVE);
	}

	boolean isSuspended() {
		return this.getActiveState() == XAResourceActivationState.SUSPENDED;
	}

	boolean isReadOnly() {
		return readOnly;
	}

	void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	private XAResourceActivationState getActiveState() {
		return activeState;
	}

	void setRollbackOnly(boolean rbOnly) {
		this.rollbackOnly = rbOnly;
	}

	boolean isRollbackOnly() {
		// new Exception("Read " +
		// this.rollbackOnly+" ObjectId"+super.toString()).printStackTrace();
		return rollbackOnly;
	}

	XAResourceProgressState getProgressState() {
		return progressState;
	}

	private void setProgressState(XAResourceProgressState progressState) {
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
		if (this.getProgressState() == XAResourceProgressState.COMMITTED) {
			return;
		}

		if (this.hasHeuristicOutcome()) {
			throw new XAException(this.heuristicState);
		}
		if (!checkTXRequirements()) {
			throw new SampleTransactionalException("State not transactional "
					+ this);
		}

		this.checkRollback();

		if (this.getProgressState() != XAResourceProgressState.ACTIVE
				&& onePhase) {
			throw new XAException(XAException.XAER_RMFAIL);
		} else if (this.getProgressState() != XAResourceProgressState.PREPARED
				&& !onePhase) {
			throw new XAException(XAException.XAER_RMFAIL);
		} else if (this.getProgressState() != XAResourceProgressState.PREPARED
				&& this.getProgressState() != XAResourceProgressState.ACTIVE) {
			throw new XAException(XAException.XAER_RMFAIL);
		}

		this.setProgressState(XAResourceProgressState.COMMITTING);
		try {
			this.getManagedConnection().commit(onePhase);
		} catch (Exception e) {
			throwXAException(XAException.XAER_PROTO, e);
		}

		this.setProgressState(XAResourceProgressState.COMMITTED);

	}

	private void checkRollback() throws XAException {
		if (!this.getManagedConnection().hasCoreConnection()
				|| this.getManagedConnection().isClosed()) {
			this.setProgressState(XAResourceProgressState.ROLLING_BACK);
			throw new XAException(XAException.XA_RBROLLBACK);
		}

		if (this.isRollbackOnly()) {
			this.setProgressState(XAResourceProgressState.ROLLING_BACK);
			throw new XAException(XAException.XA_RBROLLBACK);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("RollbackOnly " + this.rollbackOnly);
		}
	}

	int prepare() throws XAException {
		// must find connection for this transaction
		if (!this.isProgressStateIn(XAResourceProgressState.ACTIVE)) {// must
																		// have
																		// had
																		// start()
																		// called
			LOG.error("XAResource " + this + " must have start() called ");
			throw new XAException(XAException.XAER_PROTO);
		}

		if (this.progressState == XAResourceProgressState.PREPARED) {
			if (this.isReadOnly()) {
				return (XAResource.XA_RDONLY);
			} else {
				return XAResource.XA_OK;
			}
		}

		// check if resource is READONLY -> no prepare is required
		if (this.isReadOnly()) {
			this.setProgressState(XAResourceProgressState.PREPARED);
			return (XAResource.XA_RDONLY);
		}

		if (this.progressState == XAResourceProgressState.PREPARING) {
			throw new IllegalStateException("XAResource already preparing");
		}
		if (!checkTXRequirements()) {
			throw new SampleTransactionalException("State not transactional "
					+ this);
		}

		if (this.hasHeuristicOutcome()) {
			throw new XAException(this.heuristicState);
		}
		this.checkRollback();
		this.setProgressState(XAResourceProgressState.PREPARING);
		try {
			this.getManagedConnection().prepare();
		} catch (Exception e) {
			throwXAException(XAException.XAER_PROTO, e);
		}
		this.setProgressState(XAResourceProgressState.PREPARED);

		// anything to commit?
		if (this.isReadOnly()) {
			return (XAResource.XA_RDONLY);
		} else {
			return XAResource.XA_OK;
		}
	}

	private boolean isProgressStateIn(XAResourceProgressState... states) {
		if (states == null || states.length == 0) {
			return false;
		}
		XAResourceProgressState currentStates = this.getProgressState();
		for (int i = 0; i < states.length; i++) {
			if (states[i] == currentStates) {
				return true;
			}
		}
		return false;
	}

	void rollback() throws XAException {
		if (!checkTXRequirements()) {
			LOG.error("State not transactional " + this);
			throw new XAException(XAException.XAER_PROTO);
		}

		LOG.debug("XATransactionalBranch:rollback for xid=" + xid
				+ " current Status="
				+ ConstantsPrinter.getStatusMessage(this.getProgressState()));
		switch (this.getProgressState()) {
		case PREPARED: // ready to do rollback
		case ACTIVE:
			try {
				LOG.debug("XATransactionalBranch:rollback try to perform the rollback operation");
				this.setProgressState(XAResourceProgressState.ROLLING_BACK);

				// do the rollback
				if (this.getManagedConnection() != null
						&& !this.getManagedConnection().isClosed()) {
					try {
						this.getManagedConnection().rollback();
					} catch (Exception e) {
						throwXAException(XAException.XAER_PROTO, e);
					}
				} else {
					LOG.info("XATransactionalBranch connection already closed -> no rollback");
				}

				this.setProgressState(XAResourceProgressState.ROLLEDBACK);
				// perform the rollback operation
				LOG.debug("XATransactionalBranch:rollback performed the rollback");
			} catch (Exception e) {
				LOG.error("Error in " + this + " \n" + e.getMessage());
				throwXAException(XAException.XAER_PROTO, e);
				// rollback will have been performed
			}
			break;
		default:
			LOG.error("XATransactionalBranch : rollback not permitted on TX state "
					+ ConstantsPrinter.getStatusMessage(this.getProgressState())
					+ " XAResoureTXState=" + this);
			throw new XAException(XAException.XAER_RMFAIL);
		}

	}

	private void throwXAException(int code, Throwable th) throws XAException {
		XAException xaException = new XAException(code);
		xaException.initCause(th);
	}

	/**
	 * checks, if the current state complies with the requirements of an active
	 * TX
	 *
	 * @return
	 */
	private boolean checkTXRequirements() {
		if (this.getManagedConnection() == null) {
			return false;
		}
		if (isInActive()) {
			return false;
		}

		return true;
	}

	public void close() {
		this.getManagedConnection().close();
		this.setProgressState(XAResourceProgressState.CLOSED);
	}

	@Override
	public void connectionRequiresTransaction(IManagedConnectionEvent<C> event) {
		this.setReadOnly(false);
	}

	@Override
	public void connectionReleased(IManagedConnectionEvent<C> event) {
		event.getManagedConnection().removeConnectionListener(this);
	}

	@Override
	public void connectionFreed(IManagedConnectionEvent<C> event) {
		event.getManagedConnection().removeConnectionListener(this);
	}

}
