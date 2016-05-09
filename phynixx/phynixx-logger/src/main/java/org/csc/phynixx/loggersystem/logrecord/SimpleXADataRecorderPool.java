package org.csc.phynixx.loggersystem.logrecord;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.generator.IDGenerator;
import org.csc.phynixx.common.generator.IDGenerators;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;

public class SimpleXADataRecorderPool implements IXARecorderPool,
		IXADataRecorderLifecycleListener {

	private IDataLoggerFactory dataLoggerFactory = null;

	private IDGenerator<Long> messageSeqGenerator = IDGenerators
			.synchronizeGenerator(IDGenerators.createLongGenerator(1l));

	/**
	 * management of the registered XADataRecorder. This structure has to stand
	 * heavy multithreaded read and write
	 */
	private SortedMap<Long, IXADataRecorder> xaDataRecorders = new TreeMap<Long, IXADataRecorder>();

	private boolean closed = false;

	public SimpleXADataRecorderPool(IDataLoggerFactory dataLoggerFactory) {
		this.dataLoggerFactory = dataLoggerFactory;
		if (this.dataLoggerFactory == null) {
			throw new IllegalArgumentException("No dataLoggerFactory set");
		}
	}

	public String getLoggerSystemName() {
		return dataLoggerFactory.getLoggerSystemName();
	}

	/**
	 * opens a new Recorder for writing. The recorder gets a new ID.
	 * 
	 * @return created dataRecorder
	 */
	private IXADataRecorder createXADataRecorder() {

		try {
			long xaDataRecorderId = this.messageSeqGenerator.generate();

			// create a new Logger
			IDataLogger dataLogger = this.dataLoggerFactory
					.instanciateLogger(Long.toString(xaDataRecorderId));

			// create a new XADataLogger
			XADataLogger xaDataLogger = new XADataLogger(dataLogger);

			PhynixxXADataRecorder xaDataRecorder = PhynixxXADataRecorder
					.openRecorderForWrite(xaDataRecorderId, xaDataLogger, this);
			synchronized (this) {
				addXADataRecorder(xaDataRecorder);
			}
			return xaDataRecorder;

		} catch (Exception e) {
			throw new DelegatedRuntimeException(e);
		}

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

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public IXADataRecorder borrowObject() throws Exception,
			NoSuchElementException {
		if (this.isClosed()) {
			throw new IllegalStateException("Pool already closed");
		}
		return this.createXADataRecorder();
	}

	/**
	 * Recorder is destroyed
	 */
	@Override
	public void returnObject(IXADataRecorder dataRecorder) throws Exception {
		if (this.isClosed()) {
			throw new IllegalStateException("Pool already closed");
		}
		dataRecorder.destroy();

	}

	@Override
	public void invalidateObject(IXADataRecorder dataRecorder) throws Exception {
		if (this.isClosed()) {
			throw new IllegalStateException("Pool already closed");
		}
		dataRecorder.destroy();

	}

	@Override
	public void clear() throws Exception {
		if (this.isClosed()) {
			throw new IllegalStateException("Pool already closed");
		}
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		if (this.isClosed()) {
			return;
		}

		HashSet<IXADataRecorder> copy = new HashSet<IXADataRecorder>(this.xaDataRecorders.values());
		for (IXADataRecorder dataRecorder : copy) {
			dataRecorder.disqualify();
		}
		xaDataRecorders.clear();
	}

	@Override
	public void destroy() {

		try {
			this.close();
		} catch (Exception e) {
		}

		this.dataLoggerFactory.cleanup();
		this.messageSeqGenerator = null;

	}

	/**
	 * a closed recorder is removed from the repository, but the content isn't
	 * discard --> do nothing in the current implementation
	 */
	@Override
	public void recorderDataRecorderClosed(IXADataRecorder xaDataRecorder) {
		this.removeXADataRecoder(xaDataRecorder);
	}

	@Override
	public void recorderDataRecorderOpened(IXADataRecorder xaDataRecorder) {
		this.addXADataRecorder(xaDataRecorder);
	}

	/**
	 * a released recorder will not be re-used. It is destroyed
	 */
	@Override
	public void recorderDataRecorderReleased(IXADataRecorder xaDataRecorder) {
		xaDataRecorder.destroy();
	}

	@Override
	public void recorderDataRecorderDestroyed(IXADataRecorder xaDataRecorder) {
		this.removeXADataRecoder(xaDataRecorder);

	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((getLoggerSystemName() == null) ? 0 : getLoggerSystemName()
						.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SimpleXADataRecorderPool other = (SimpleXADataRecorderPool) obj;
		if (getLoggerSystemName() == null) {
			if (other.getLoggerSystemName() != null)
				return false;
		} else if (!this.getLoggerSystemName().equals(
				other.getLoggerSystemName()))
			return false;
		return true;
	}

	public String toString() {
		return (this.dataLoggerFactory == null) ? "Closed Pool"
				: this.dataLoggerFactory.toString();
	}

}
