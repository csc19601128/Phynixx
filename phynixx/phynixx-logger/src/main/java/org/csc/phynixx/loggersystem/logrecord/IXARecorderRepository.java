package org.csc.phynixx.loggersystem.logrecord;

/*
 * #%L
 * phynixx-logger
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

import java.io.IOException;

/**
 * XAResource logger is specialized to support the logging of a xaresource to
 * rollback/recover the resource in the context of an transaction manager.
 * 
 * 
 *
 * @author christoph
 */
public interface IXARecorderRepository  {

	/**
	 * creates a recorder ready for logging. This recorder is managed by the
	 * repository.
	 * 
	 * 
	 * {@link IXADataRecorder#disqualify()} closes the logger (but keeps the
	 * content) and the repository forgets this logger.
	 * 
	 * {@link IXADataRecorder#disqualify()} indicates that the content of the
	 * logger can be discarded. The repository decides, what to do with the
	 * logger.
	 * 
	 * 
	 * {@link IXADataRecorder#destroy()} destroys the logger (incl.the content)
	 * and repository forgets this logger.
	 * 
	 * @return
	 * @throws IOException
	 * @throws Exception 
	 */
	IXADataRecorder createXADataRecorder() throws Exception;

	boolean isClosed();

	/**
	 * closes all open recorder. The recorder are
	 * {@link IXADataRecorder#disqualify()} and removed from the repository.
	 *
	 * Depending on recording, closed recorder could recovered if it contains
	 * relevant information and it is not destroyed but can be re-established by
	 * {@link #recover}
	 * 
	 * 
	 * They can not be recovered.
	 */
	void close();

	/**
	 * destroys all recovered recorder. The recorder are
	 * {@link org.csc.phynixx.loggersystem.logrecord.IXADataRecorder#destroy()}
	 * and removed from the repository.
	 * 
	 * They can not be recovered.
	 */
	void destroy() throws IOException, InterruptedException;

	
}
