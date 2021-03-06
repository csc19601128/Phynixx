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

import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
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
public class PhynixxXARecorderRepository implements IXARecorderRepository {


	private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(PhynixxXARecorderRepository.class);

	
	final IXARecorderProvider xaRecordProvider; 


	public PhynixxXARecorderRepository(IXARecorderProvider dataRecorderPool) {
		this.xaRecordProvider = dataRecorderPool;
		if (this.xaRecordProvider == null) {
			throw new IllegalArgumentException("No dataLoggerFactory set");
		}
	}

	/**
	 * the dataRecorder are not re-used. If they are reset they are destroyed
	 * 
	 * @param dataLoggerFactory
	 */
	public PhynixxXARecorderRepository(IDataLoggerFactory dataLoggerFactory) {
		this(new SimpleXADataRecorderProvider(dataLoggerFactory));
	}


	@Override
	public IXADataRecorder createXADataRecorder() throws Exception {
		return this.xaRecordProvider.provideXADataRecorder();
	}

	@Override
	public boolean isClosed() {
		return this.xaRecordProvider.isClosed();
	}

	

	

	public synchronized void open() throws IOException, InterruptedException {
		if (this.xaRecordProvider == null) {
         throw new IllegalStateException("No XADataRecordProvider set");
		}
	}

	@Override
	public synchronized void close() {
		this.xaRecordProvider.close();
	}

	@Override
	public synchronized void destroy() throws IOException, InterruptedException {
		this.xaRecordProvider.destroy();
	}

	

	
}
