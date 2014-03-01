package org.csc.phynixx.loggersystem;

import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.connection.ConnectionFactory;
import org.csc.phynixx.connection.reference.ReferenceConnectionFactory;
import org.csc.phynixx.connection.reference.ReferenceConnectionProxyFactory;
import org.csc.phynixx.loggersystem.channellogger.FileChannelLoggerFactory;
import org.csc.phynixx.loggersystem.messages.ILogRecord;
import org.csc.phynixx.loggersystem.messages.IRecordLogger;
import org.csc.phynixx.loggersystem.messages.ILogRecordReplay;
import org.csc.phynixx.loggersystem.messages.PhynixxLogRecordSequence;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class MessageLoggerTest extends TestCase{
	
	private ConnectionFactory createConnectionFactory() throws Exception {

		ConnectionFactory factory=new ConnectionFactory(new ReferenceConnectionFactory(),new ReferenceConnectionProxyFactory());
		ILoggerFactory loggerFactory= new FileChannelLoggerFactory("reference", new TmpDirectory().getDirectory());	
		factory.setLoggerSystemStrategy(new PerTransactionStrategy("reference",loggerFactory));
		
		
		return factory;
	}
	
	protected void setUp() throws Exception 
	{
		// configuring the log-system (e.g. log4j)
		TestUtils.configureLogging(); 
		
		// delete all tmp files ...
		new TmpDirectory().clear();
		
	}

	protected void tearDown() throws Exception 
	{
		// delete all tmp files ...
		new TmpDirectory().clear();
	}
	
	public void testMessageLogger() throws Exception
	{
		PhynixxLogRecordSequence seq= new PhynixxLogRecordSequence(1L);
		
		IRecordLogger msgLogger= seq;

		TestCase.assertTrue(!seq.isCommitting());

		msgLogger.writeRollbackData("XYZ".getBytes());
		msgLogger.writeRollbackData(new byte[] [] {"XYZ".getBytes(), "ZYX".getBytes()});

		TestCase.assertTrue(!seq.isCommitting());
		msgLogger.commitRollforwardData(new byte[] [] {"XYZ".getBytes(), "ZYX".getBytes()});
		TestCase.assertTrue(seq.isCommitting());
		
		msgLogger.commitRollforwardData(new byte[] [] {"ABCD".getBytes()});
		
		try {
			msgLogger.writeRollbackData(new byte[] [] {});
			throw new AssertionFailedError("No more RF Data; Sequence is committing");
		} catch(Exception e) {};
			

		TestCase.assertTrue(seq.isCommitting());
		TestCase.assertTrue(!seq.isCompleted());
		
		
		// reply the messages 
		ILogRecordReplay replay= new ILogRecordReplay() {
			public void replayRollback(ILogRecord message) {
				switch((int)message.getOrdinal().longValue()) {
					case 1:
						TestCase.assertEquals("XYZ", new String(message.getData()[0]));
						break;
					case 2:
						TestCase.assertEquals("XYZ", new String(message.getData()[0]));
						TestCase.assertEquals("ZYX", new String(message.getData()[1]));
						break;
				    default:
				    	throw new AssertionFailedError("Unexpected Message " + message);				        
				}
			}

			public void replayRollforward(ILogRecord message) {
				if(message.getOrdinal().longValue()==3) {
					TestCase.assertEquals("XYZ", new String(message.getData()[0]));
					TestCase.assertEquals("ZYX", new String(message.getData()[1]));
				} else if (message.getOrdinal().longValue()==4) {
					TestCase.assertEquals("ABCD", new String(message.getData()[0]));
				} else {
			    	throw new AssertionFailedError("Unexpected Message " + message);
				}
			}
			
		};
		
		msgLogger.replayRecords(replay);
		
		
	}
	

}
