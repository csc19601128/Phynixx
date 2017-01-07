package org.csc.phynixx.connection.loggersystem;

/*
 * #%L
 * phynixx-common
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.io.LogRecordWriter;
import org.csc.phynixx.connection.IManagedConnectionCommitEvent;
import org.csc.phynixx.connection.IManagedConnectionEvent;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnectionListener;
import org.csc.phynixx.connection.IXADataRecorderAware;
import org.csc.phynixx.connection.PhynixxManagedConnectionListenerAdapter;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;
import org.csc.phynixx.loggersystem.logrecord.IXARecorderRecovery;
import org.csc.phynixx.loggersystem.logrecord.IXARecorderRepository;
import org.csc.phynixx.loggersystem.logrecord.IXARecorderResourceListener;
import org.csc.phynixx.loggersystem.logrecord.PhynixxXARecorderRepository;
import org.csc.phynixx.loggersystem.logrecord.XARecorderRecovery;

/**
 * this listener observes the lifecycle of a connection and associates a
 * xaDataRecorder if necessary.
 */
public class LoggerPerTransactionStrategy<C extends IPhynixxConnection & IXADataRecorderAware>
		extends PhynixxManagedConnectionListenerAdapter<C> implements
		IPhynixxLoggerSystemStrategy<C>, IPhynixxManagedConnectionListener<C> {

	private IXARecorderRepository xaRecorderRepository;
	private IDataLoggerFactory loggerFactory;

	/**
	 * the logger is added to all instanciated Loggers
	 */
	public void addLoggerListener(IXARecorderResourceListener listener) {
		// this.xaRecorderResource.addListener(listener);

	}

	/**
	 * per thread a new Logger cpould be instanciated with aid of the
	 * loggerFacrory
	 *
	 * @param loggerFactory	 * @throws Exception
	 */
	public LoggerPerTransactionStrategy(IDataLoggerFactory loggerFactory) {
		this.xaRecorderRepository = new PhynixxXARecorderRepository(loggerFactory);
		this.loggerFactory= loggerFactory;
	}

	@Override
	public void close() {
		this.xaRecorderRepository.close();
	}

	@Override
	public void connectionRecovering(IManagedConnectionEvent<C> event) {
		this.connectionRequiresTransaction(event);
	}

	/**
	 * If a dataRecorder is found in this phase it indicates an abnormal program
	 * flow. Die Transaktion isn't terminate via commit/rollback and this
	 * connection happens to be recoverd
	 *
	 * Therefore the dataRecorder isn't close and keeps it's content to possibly
	 * be recovered
	 *
	 */
	@Override
	public void connectionReleased(IManagedConnectionEvent<C> event) {

		// physical connection is already set free
		if (!event.getManagedConnection().hasCoreConnection()) {
			return;
		}

		C con = event.getManagedConnection().getCoreConnection();
		if (con == null || !(con instanceof IXADataRecorderAware)) {
			return;
		}

		IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;
		// Transaction is closed and the xaDataRecorder is destroyed ...
		IXADataRecorder xaDataRecorder = messageAwareConnection
				.getXADataRecorder();
		if (xaDataRecorder == null) {
			return;
		}

		// if commit/rollback was performed, nothing happened. If no the logged
		// data is closed but not destroy. So recovery can happen
		xaDataRecorder.disqualify();

	}

	/**
	 * Logger will be closed. If a dataRecorder has remaining transactional data
	 * an abnormal prgram flow is detected an the data of the logger is not
	 * destroy but kept to further recovery
	 *
	 * @param event
	 *            current connection
	 */

	@Override
	public void connectionFreed(IManagedConnectionEvent<C> event) {
		// physical connection is already set free
		if (!event.getManagedConnection().hasCoreConnection()) {
			return;
		}
		C con = event.getManagedConnection().getCoreConnection();
		if (con == null || !(con instanceof IXADataRecorderAware)) {
			return;
		}

		IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;
		// Transaction is closed and the xaDataRecorder is destroyed ...
		IXADataRecorder xaDataRecorder = messageAwareConnection
				.getXADataRecorder();
		if (xaDataRecorder == null) {
			return;
		}

		// if commit/rollback was performed, nothing happend. If no the logged
		// data is closed but not destroy. So recovery can happen

		if (event.getManagedConnection().hasTransactionalData()) {
			xaRecorderRepository.close(); // close without removing the recover
											// data
		} else {
			xaDataRecorder.destroy();
		}
		messageAwareConnection.setXADataRecorder(null);

	}

	/**
	 * destroys the datalogger
	 */
	@Override
	public void connectionRecovered(IManagedConnectionEvent<C> event) {
		// physical connection is already set free
		if (!event.getManagedConnection().hasCoreConnection()) {
			return;
		}
		IPhynixxConnection con = event.getManagedConnection()
				.getCoreConnection();
		if (con == null || !(con instanceof IXADataRecorderAware)) {
			return;
		}

		IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;

		// Transaction is close and the logger is destroyed ...
		IXADataRecorder xaDataRecorder = messageAwareConnection
				.getXADataRecorder();
		if (xaDataRecorder == null) {
			return;
		}
		// it's my logger ....

		// the logger has to be destroyed ...
		else {
			xaDataRecorder.destroy();
			messageAwareConnection.setXADataRecorder(null);
		}

	}

	@Override
	public void connectionRolledback(IManagedConnectionEvent<C> event) {
		IPhynixxConnection con = event.getManagedConnection()
				.getCoreConnection();
		if (con == null || !(con instanceof IXADataRecorderAware)) {
			return;
		}

		IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;

		// Transaction is closed and the logger is destroyed ...
		IXADataRecorder xaDataRecorder = messageAwareConnection
				.getXADataRecorder();
		if (xaDataRecorder == null) {
			return;
		}

		// if the rollback is completed the rollback data isn't needed
		xaDataRecorder.release();
		messageAwareConnection.setXADataRecorder(null);

		event.getManagedConnection().removeConnectionListener(this);
	}

	@Override
	public void connectionCommitted(IManagedConnectionCommitEvent<C> event) {
		IPhynixxConnection con = event.getManagedConnection()
				.getCoreConnection();
		if (con == null || !(con instanceof IXADataRecorderAware)) {
			return;
		}

		IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;

		// Transaction is close and the logger is destroyed ...
		IXADataRecorder xaDataRecorder = messageAwareConnection
				.getXADataRecorder();
		if (xaDataRecorder == null) {
			return;
		}
		xaDataRecorder.release();
		messageAwareConnection.setXADataRecorder(null);

	}

	/**
	 * start sequence writes the ID of the XADataLogger to identify the content
	 * of the logger
	 *
	 * @param dataRecorder
	 *            DataRecorder that uses /operates on the current physical
	 *            logger
	 */
	private void writeStartSequence(IXADataRecorder dataRecorder)
			throws IOException, InterruptedException {

		LogRecordWriter writer = new LogRecordWriter();
		writer.writeLong(dataRecorder.getXADataRecorderId());
		dataRecorder.writeRollbackData(writer.toByteArray());
	}

	@Override
	public void connectionRequiresTransaction(IManagedConnectionEvent<C> event) {
		IPhynixxConnection con = event.getManagedConnection()
				.getCoreConnection();
		if (con == null || !(con instanceof IXADataRecorderAware)) {
			return;
		}

		IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;

		// Transaction is close and the logger is destroyed ...
		IXADataRecorder xaDataRecorder = messageAwareConnection
				.getXADataRecorder();
		// it's my logger ....

		// Transaction is closed and the logger is destroyed ...
		if (xaDataRecorder != null && xaDataRecorder.isClosed()) {

			// pending transaction data --> ready for recover
			xaDataRecorder.disqualify();
			xaDataRecorder = null; // gonna be refreshed
		}

		// refresh the datarecorder , if
		if (xaDataRecorder == null) {
			try {
				IXADataRecorder xaLogger = this.xaRecorderRepository
						.createXADataRecorder();
				messageAwareConnection.setXADataRecorder(xaLogger);
			} catch (Exception e) {
				// retry ...
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
				try {
					IXADataRecorder xaLogger = this.xaRecorderRepository
							.createXADataRecorder();
					messageAwareConnection.setXADataRecorder(xaLogger);
				} catch (Exception ee) {
					throw new DelegatedRuntimeException(
							"creating new Logger for " + con, ee);
				}
			}
		}
		event.getManagedConnection().addConnectionListener(this);

	}

	/**
	 * recovers all incomplete dataRecorders
	 * {@link org.csc.phynixx.loggersystem.logrecord.IXADataRecorder#isEmpty()}
	 * and destroys all complete dataRecorders
	 *
	 * @return incomplete dataRecorders
	 */

	@Override
	public List<IXADataRecorder> readIncompleteTransactions() {
		List<IXADataRecorder> messageSequences = new ArrayList<IXADataRecorder>();
		// recover all loggers ....
		try {
			
			IXARecorderRecovery recorderRecovery= new XARecorderRecovery(this.loggerFactory);

			Set<IXADataRecorder> xaDataRecorders = recorderRecovery.getRecoveredXADataRecorders();

			for (Iterator<IXADataRecorder> iterator = xaDataRecorders.iterator(); iterator.hasNext();) {
				IXADataRecorder dataRecorder = iterator.next();

				if (!dataRecorder.isEmpty()) {
					messageSequences.add(dataRecorder);
				} else {
					dataRecorder.destroy();
				}

			}
			return messageSequences;
		} catch (Exception e) {
			throw new DelegatedRuntimeException(e);
		}
	}

	@Override
	public IPhynixxManagedConnection<C> decorate(
			IPhynixxManagedConnection<C> managedConnection) {
		managedConnection.addConnectionListener(this);
		return managedConnection;
	}

}
