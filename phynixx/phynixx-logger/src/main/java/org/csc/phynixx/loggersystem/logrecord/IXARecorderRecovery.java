package org.csc.phynixx.loggersystem.logrecord;

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
	void destroy() throws IOException;


}