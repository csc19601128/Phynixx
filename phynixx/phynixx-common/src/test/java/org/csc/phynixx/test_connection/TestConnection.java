package org.csc.phynixx.test_connection;

import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.Dev0Strategy;
import org.csc.phynixx.loggersystem.messages.ILogRecord;
import org.csc.phynixx.loggersystem.messages.IRecordLogger;
import org.csc.phynixx.loggersystem.messages.ILogRecordReplay;


/**
 * 
 * TestConnection provides a mechanism to activate predetermined points of interruption.
 * These points leads the current work to interrupt. 
 * You can simulate abnormal situation like system crashes.
 * 
 * The points of interruption are defined by the call of {@link #interrupt()}.
 * 
 * They can be activated by setting an interrupt flag which makes connection 
 * interrupt the next time a interruption point is reached.
 * If you set an interrupt offset to <code>n</code>the connection will interrupt, 
 * if it reaches the n-th interruption point.
 * Setting the interuption flag is equivalent to set the interruptin offeset to 1.
 * 
 * 
 * @author christoph
 *
 */

public class TestConnection implements ITestConnection {


	private IPhynixxLogger log= PhynixxLogManager.getLogger("test"); 
	
	private Object id= null;
	private boolean closed= false;
	
	private int currentCounter= 0; 
	
	private int interruptCounter= -1;
	
	private IRecordLogger messageLogger= Dev0Strategy.THE_DEV0_LOGGER; 
	

	public IRecordLogger getRecordLogger() {
		return messageLogger;
	}

	public void setRecordLogger(IRecordLogger messageLogger) {
		this.messageLogger = messageLogger;
	}

	public TestConnection(Object id) {
		this.id = id;
		TestConnectionStatusManager.addStatusStack(this.getId());
	}
	
	

	public boolean isInterruptFlag() {
		return interruptCounter == 0;
	}

	public void setInterruptFlag(boolean interruptFlag) {
		this.interruptCounter = 1;
	}
	
	public void setInterruptOffset(int interruptOffset) {
		this.interruptCounter= interruptOffset;
	}

	public int getCurrentCounter() {
		return currentCounter;
	}

	/* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ITestConnection#getId()
	 */
	public Object getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see de.csc.xaresource.sample.ITestConnection#act()
	 */
	public void act(int inc) 
	{
	    this.getRecordLogger().writeRollbackData(Integer.toString(inc).getBytes());
	    interrupt();
	    this.currentCounter= this.currentCounter+inc;
		log.info("TestConnection " + id + " counter incremented to "+inc +" counter="+this.getCurrentCounter());
		
		TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.ACT);
	}

	public boolean isClosed() {
		return closed;
	}

	public void close() 
	{
		TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.CLOSED);
		log.info("TestConnection " + id + " closed");
		this.closed=true;
	}

	public void commit() {		
		 this.getRecordLogger().commitRollforwardData(Integer.toString(RF_INCREMENT).getBytes());
	  	 interrupt();	 
	     this.currentCounter= this.currentCounter+RF_INCREMENT;	
	     TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.COMMITTED);	 
	     log.info("TestConnection " + id + " is committed");
	 	
	}

	public void prepare() {
		interrupt();	
		TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.PREPARED);
		log.info("TestConnection " + id + " is prepared");
	}
	
	public void rollback() {
		interrupt();
		this.currentCounter=0;	
		TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.ROLLBACKED);
		log.info("TestConnection " + id + " rollbacked");
	}
	
	public String toString() {
		return "TestConnection "+id; 
	}


	private void interrupt() 
	{
		this.interruptCounter--;
		
		if( isInterruptFlag()) {			
			throw new ActionInterruptedException();
		}
	}
	
	public void recover() {
		this.getRecordLogger().replayRecords(new MessageReplay());
	}
	
	
	private class MessageReplay implements ILogRecordReplay {

		public void replayRollback(ILogRecord message) 
		{
			int inc= Integer.parseInt(new String(message.getData()[0]));
			TestConnection.this.currentCounter= 
				TestConnection.this.currentCounter+inc;
		}

		public void replayRollforward(ILogRecord message) {
			int inc= Integer.parseInt(new String(message.getData()[0]));
			TestConnection.this.currentCounter= 
				TestConnection.this.currentCounter+inc;
		}
		
	}
}
