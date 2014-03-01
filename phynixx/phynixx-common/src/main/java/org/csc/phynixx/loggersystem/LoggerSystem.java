package org.csc.phynixx.loggersystem;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.LogFileCollector.ICollectorCallback;

/**
 * 
 * 
 * the current class is responsible for instanciating new
 * {@link XAResourceLogger}. A new Logger ist requested by the ILoggerFactory an
 * assigned to the {@link XAResourceLogger} The LoggerSystem manages the opened
 * XAResourceLogger and
 * 
 * @author christoph
 * 
 */
public class LoggerSystem implements ILoggerListener {

	private static final String GLOBAL_FORMAT_PATTREN = "({0}_[a-z,A-Z,0-9]*[^_])_([0-9]*[^\\.])\\.[\\w]*";

	private static final String LOGGER_FORMAT_PATTREN = "({0})_([0-9]*[^\\.])\\.[\\w]*";

	private ILoggerFactory loggerFactory = null;

	private Set openLoggers = new HashSet();

	private long idGenerator = System.currentTimeMillis();

	/**
	 * ILoggereListeners watching the lifecycle of this logger
	 */
	private List listeners = new ArrayList();

	private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

	private String loggerSystemName = null;

	public LoggerSystem(String loggerSystemName, ILoggerFactory loggerFactory) {
		super();
		this.loggerSystemName = loggerSystemName;
		this.loggerFactory = loggerFactory;
		this.addListener(this);
	}

	public String getLoggerSystemName() {
		return this.loggerSystemName;
	}

	public XAResourceLogger instanciateLogger() throws IOException,
			InterruptedException {
		return this.instanciateLogger(null, true);
	}

	public synchronized XAResourceLogger instanciateLogger(String loggerName,
			boolean open) throws IOException, InterruptedException {
		String myLoggerName = loggerName;
		if (myLoggerName == null) {
			myLoggerName = this.getLoggerSystemName() + "_"
					+ Long.toString(idGenerator);
			idGenerator++;
		}

		XAResourceLogger logger = new XAResourceLogger(this.loggerFactory
				.instanciateLogger(myLoggerName));
		for (int i = 0; i < this.listeners.size(); i++) {
			logger.addListener((ILoggerListener) listeners.get(i));
		}

		// IHowlLogger lw= new HowlLoggerWrapper(loggerName,howlLogger);
		if (open) {
			logger.open();
		}
		this.openLoggers.add(logger);
		return logger;
	}

	public void loggerClosed(XAResourceLogger logger) {
		synchronized (this) {
			this.openLoggers.remove(logger);
		}
	}

	public synchronized void loggerOpened(XAResourceLogger logger) {
		if (!this.openLoggers.contains(logger)) {
			this.openLoggers.add(logger);
		}
	}

	public void destroy(XAResourceLogger logger) {

		try {
			logger.destroy();
		} catch (Exception e) {
			log.error("Error destroying logger " + this + " :: "
					+ e.getMessage());
		}

		// delete all log files .....
		String pattern = MessageFormat.format(LOGGER_FORMAT_PATTREN,
				new String[] { logger.getLoggerName() });
		LogFilenameMatcher matcher = new LogFilenameMatcher(pattern);

		ICollectorCallback cb = new ICollectorCallback() {
			public void match(File file,
					LogFilenameMatcher.LogFilenameParts parts) {
				boolean success = file.delete();
				if (log.isDebugEnabled()) {
					log.debug("Deleting " + file + " success=" + success);
				}
			}
		};
		LogFileCollector logfileCollector = new LogFileCollector(matcher,
				LoggerSystem.this.loggerFactory.getLoggingDirectory(), cb);

	}

	/**
	 * recovers all {@link XAResourceLogger} having log files
	 * 
	 * @return
	 * @throws LogConfigurationException
	 * @throws IOException
	 * @throws InvalidFileSetException
	 * @throws InvalidLogBufferException
	 * @throws InterruptedException
	 */
	public synchronized Set recover() throws Exception {
		Set loggers = new HashSet();

		// delete all log files .....
		String pattern = MessageFormat.format(GLOBAL_FORMAT_PATTREN,
				new String[] { this.getLoggerSystemName() });
		LogFilenameMatcher matcher = new LogFilenameMatcher(pattern);

		final Set loggerNames = new HashSet();
		ICollectorCallback cb = new ICollectorCallback() {
			public void match(File file,
					LogFilenameMatcher.LogFilenameParts parts) {
				loggerNames.add(parts.getLoggerName());
			}
		};

		LogFileCollector logfileCollector = new LogFileCollector(matcher,
				LoggerSystem.this.loggerFactory.getLoggingDirectory(), cb);

		for (Iterator iterator = loggerNames.iterator(); iterator.hasNext();) {
			String loggerName = (String) iterator.next();
			loggers.add(this.instanciateLogger(loggerName, true));
		}
		return loggers;
	}

	/**
	 * 
	 * closes all open loggers
	 */
	public synchronized void close() {
		HashSet copiedLoggers = new HashSet(this.openLoggers);
		for (Iterator iterator = copiedLoggers.iterator(); iterator.hasNext();) {
			XAResourceLogger logger = (XAResourceLogger) iterator.next();
			try {
				logger.close();
			} catch (Exception e) {
				;
			}
			openLoggers.remove(logger);
		}

	}

	public synchronized void addListener(ILoggerListener listener) {
		if (!listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}

}
