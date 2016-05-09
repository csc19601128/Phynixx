package org.csc.phynixx.loggersystem.logrecord;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.generator.IDGenerator;
import org.csc.phynixx.common.generator.IDGenerators;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;

/**
 * XAResource logger is specialized to support the logging of a xaresource to
 * rollback/recover the resource in the context of an transaction manager.
 * 
 * This repository watches and manages the lifycycle of
 * {@link PhynixxXADataRecorder}
 * 
 * 
 * 
 *
 * @author christoph
 */
public class XARecorderRecovery implements IXADataRecorderLifecycleListener, IXARecorderRecovery {


	private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(XARecorderRecovery.class);
	
	private IDataLoggerFactory dataLoggerFactory = null;
	

	private SortedMap<Long, IXADataRecorder> xaDataRecorders = new TreeMap<Long, IXADataRecorder>();

	/**
	 * management of the closed XADataLoggers
	 */
	private SortedMap<Long, PhynixxXADataRecorder> closedDataRecorders = new TreeMap<Long, PhynixxXADataRecorder>();


	public XARecorderRecovery(IDataLoggerFactory dataLoggerFactory) {
		this.dataLoggerFactory = dataLoggerFactory;
		if (this.dataLoggerFactory == null) {
			throw new IllegalArgumentException("No dataLoggerFactory set");
		}
		this.recover();
	}
	
	private void addXADataRecorder(IXADataRecorder xaDataRecorder) {
		if (!this.xaDataRecorders.containsKey(xaDataRecorder
				.getXADataRecorderId())) {
			this.xaDataRecorders.put(xaDataRecorder.getXADataRecorderId(),
					xaDataRecorder);
		}
	}

	private void removeXADataRecoder(IXADataRecorder xaDataRecorder) {
		if (xaDataRecorder == null) {
			return;
		}
		this.xaDataRecorders.remove(xaDataRecorder.getXADataRecorderId());

	}


	
	/* (non-Javadoc)
	 * @see org.csc.phynixx.loggersystem.logrecord.IXARecoderRecovery#close()
	 */
	@Override
	public synchronized void close() {
		Set<IXADataRecorder> recoveredXADataRecorders = this.getRecoveredXADataRecorders();
		for (IXADataRecorder dataRecorder : recoveredXADataRecorders) {
			dataRecorder.disqualify();			
		}
	}

	/* (non-Javadoc)
	 * @see org.csc.phynixx.loggersystem.logrecord.IXARecoderRecovery#destroy()
	 */
	@Override
	public synchronized void destroy() throws IOException {
		Set<IXADataRecorder> recoveredXADataRecorders = this.getRecoveredXADataRecorders();
		for (IXADataRecorder dataRecorder : recoveredXADataRecorders) {
			dataRecorder.destroy();			
		}
	}

	/**
	 * recovers all dataRecorder of the loggerSystem. All reopen dataRecorders
	 * are closed and all dataRecorder that can be recovered are opened for
	 * reading
	 *
	 * @see #getRecoveredXADataRecorders()
	 */
	private synchronized void recover() {

		try {

			// close all reopen dataRecorders
			this.close();

			Set<String> loggerNames = this.dataLoggerFactory.findLoggerNames();

			// recover all logs
			for (String loggerName : loggerNames) {

				IDataLogger dataLogger = this.dataLoggerFactory.instanciateLogger(loggerName);
				XADataLogger xaLogger = new XADataLogger(dataLogger);

				PhynixxXADataRecorder phynixxXADataRecorder = PhynixxXADataRecorder.recoverDataRecorder(xaLogger, this);
				this.addXADataRecorder(phynixxXADataRecorder);

			}
		} catch (Exception e) {
			throw new DelegatedRuntimeException(e);
		}

	}

	/* (non-Javadoc)
	 * @see org.csc.phynixx.loggersystem.logrecord.IXARecoderRecovery#getXADataRecorders()
	 */
	@Override
	public Set<IXADataRecorder> getRecoveredXADataRecorders() {
		synchronized (xaDataRecorders) {
			return new HashSet<IXADataRecorder>(xaDataRecorders.values());			
		}
	}

	/**
	 * Logger is closed an can be forgotten. It is ready for recover but can not
	 * be re-used
	 */
	@Override
	public synchronized void recorderDataRecorderClosed(IXADataRecorder xaDataRecorder) {
		this.removeXADataRecoder(xaDataRecorder);
	}

	@Override
	public void recorderDataRecorderOpened(IXADataRecorder xaDataRecorder) {
		this.removeXADataRecoder(xaDataRecorder);
	}

	
	/**
	 * a recovered Recorder isn't used any longer
	 * @param xaDataRecorder
	 */
	@Override
	public synchronized void recorderDataRecorderReleased(	IXADataRecorder xaDataRecorder) {
		this.removeXADataRecoder(xaDataRecorder);
		xaDataRecorder.destroy();

	}

	@Override
	public void recorderDataRecorderDestroyed(IXADataRecorder xaDataRecorder) {
		this.removeXADataRecoder(xaDataRecorder);
	}



}
