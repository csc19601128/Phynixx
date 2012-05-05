package org.csc.phynixx.loggersystem;

import java.util.ArrayList;
import java.util.List;

import org.csc.phynixx.connection.IRecordLoggerAware;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxConnectionProxy;
import org.csc.phynixx.connection.IPhynixxConnectionProxyEvent;
import org.csc.phynixx.connection.PhynixxConnectionProxyListenerAdapter;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.messages.ILogRecordReplay;
import org.csc.phynixx.loggersystem.messages.IManagedRecordLogger;


public class Dev0Strategy extends PhynixxConnectionProxyListenerAdapter implements ILoggerSystemStrategy {

	private IPhynixxLogger logger= PhynixxLogManager.getLogger(this.getClass());
	
	
	public static final Dev0Logger THE_DEV0_LOGGER= new Dev0Logger(); 
	
	private static class Dev0Logger implements IManagedRecordLogger
	{

		public void commitRollforwardData(byte[][] data) {			
		}

		public void commitRollforwardData(byte[] data) {			
		}

		public boolean isCommitting() {
			return false;
		}

		public boolean isCompleted() {
			return false;
		}

		public boolean isPrepared() {
			return false;
		}

		public void replayRecords(ILogRecordReplay replay) {			
		}

		public void writeRollbackData(byte[] data) {			
		}

		public void writeRollbackData(byte[][] data) {			
		}

		public void close() {
		}

		public void destroy() {
		}
		
	}

	public void close() {
	}

	public IPhynixxConnectionProxy decorate(IPhynixxConnectionProxy connectionProxy) {
		connectionProxy.addConnectionListener(this);
		return connectionProxy;
	}



	public void connectionRequiresTransaction(IPhynixxConnectionProxyEvent event) 
	{
		IPhynixxConnection con=event.getConnectionProxy().getConnection();
		if( con==null || !(con instanceof IRecordLoggerAware)) {
			return;
		}		
		IRecordLoggerAware messageAwareConnection= (IRecordLoggerAware)con;
		// Transaction is close and the logger is destroyed ...
		Dev0Logger logger= (Dev0Logger) messageAwareConnection.getRecordLogger();		
		if( logger==null) {
			messageAwareConnection.setRecordLogger(THE_DEV0_LOGGER);
		}		
		event.getConnectionProxy().addConnectionListener(this);		
	}



	public List readIncompleteTransactions() {
		return new ArrayList(0); 
	}
	
	
	
}
