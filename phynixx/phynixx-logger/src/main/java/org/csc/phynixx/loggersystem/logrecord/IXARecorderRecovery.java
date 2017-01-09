package org.csc.phynixx.loggersystem.logrecord;

/*
 * #%L
 * phynixx-logger
 * %%
 * Copyright (C) 2014 - 2017 Christoph Schmidt-Casdorff
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
import java.util.Set;


/**
 * tries to re-establish all recorders. The logger system is closed. All
 * remaining (already closed) recorder are reopen for read
 * 
 * @see #getRecoveredXADataRecorders()
 * */
public interface IXARecorderRecovery {

	/**
	 * 
	 * @return all recoverable dataRecoders
	 */
	Set<IXADataRecorder> getRecoveredXADataRecorders();

	/**
	 * closes all open recorder. The recorder are
	 * {@link IXADataRecorder#disqualify()} and removed from the repository.
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
	void destroy() throws IOException;


}