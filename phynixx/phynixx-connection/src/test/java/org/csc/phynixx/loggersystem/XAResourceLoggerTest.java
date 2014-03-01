package org.csc.phynixx.loggersystem;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.channellogger.FileChannelLoggerFactory;
import org.csc.phynixx.loggersystem.messages.ILogRecord;
import org.csc.phynixx.loggersystem.messages.ILogRecordSequence;

public class XAResourceLoggerTest extends TestCase {
	
    private IPhynixxLogger log= PhynixxLogManager.getLogger(this.getClass());    
	
	private TmpDirectory tmpDir= null; 
		
	protected void setUp() throws Exception 
	{
		// configuring the log-system (e.g. log4j)
		TestUtils.configureLogging(); 
		
		// delete all tmp files ...
		this.tmpDir= new TmpDirectory("howllogger");
		this.tmpDir.clear();
		
		this.tmpDir= new TmpDirectory("howllogger");
		System.getProperties().setProperty("howl.log.logFileDir", this.tmpDir.getDirectory().getCanonicalPath());
		
	}

	protected void tearDown() throws Exception 
	{
		// delete all tmp files ...
		this.tmpDir.clear();
	}
	
	private Properties loadHowlConfig() throws Exception
	{
        Properties howlprop= new Properties();
        howlprop.put("listConfig", "true");
        howlprop.put("bufferSize", "32");
        howlprop.put("minBuffers", "16");
        howlprop.put("maxBuffers", "16");
        howlprop.put("maxBlocksPerFile", "10");
        howlprop.put("logFileDir", this.tmpDir.getDirectory().getAbsolutePath());
        howlprop.put("logFileName", "test1");
        howlprop.put("maxLogFiles", "6");
        
        return howlprop;
	}

	
	public void testXAResourceLogger() throws Exception 
	{
		
		// Start XALogger ....
		ILoggerFactory loggerFactory= new FileChannelLoggerFactory("mt",this.tmpDir.getDirectory());
		XAResourceLogger logger= new XAResourceLogger(loggerFactory.instanciateLogger("test"));
	    
		int countMessages =0; 
		
		try {
			logger.open();
			
			// start the sequence to be tested 
			ILogRecordSequence seq= logger.createMessageSequence();
			
			logger.startXA(seq, "test1", "XID".getBytes());				
			logger.logUserData(seq,"Log1".getBytes());
			logger.preparedXA(seq);
			TestCase.assertTrue(seq.isPrepared());
			
			logger.committingXA(seq,new byte[] [] {"A".getBytes(), "B".getBytes()});
			TestCase.assertTrue(seq.isCommitting());			
			try {
				logger.logUserData(seq,new byte[] [] {"A".getBytes(), ".".getBytes()});
				throw new AssertionFailedError("No more RB Data; Sequence is committing");
			} catch(Exception e) {};			
			
			// more commiting data are accepted
			logger.committingXA(seq,new byte[] [] {});
			
			logger.doneXA(seq);
			TestCase.assertTrue(seq.isCompleted());
			
			
			countMessages= seq.getMessages().size();
			
		} finally {				
			logger.destroy(); 
		}
		
		
		
		try {			
			logger.open();
			
			// recover the message sequences 
			logger.readMessageSequences();				
			
			List sequences= logger.getOpenMessageSequences();
			log.info(sequences);		
			
			TestCase.assertEquals(1,sequences.size());
			
			ILogRecordSequence seq= (ILogRecordSequence)sequences.get(0);			
			List messages= seq.getMessages();			

			TestCase.assertTrue(seq.isCompleted());
			TestCase.assertEquals(countMessages,messages.size());

			ILogRecord msg= (ILogRecord)messages.get(0);
			TestCase.assertTrue( msg.getLogRecordType()==XALogRecordType.XA_START);
			
			msg= (ILogRecord)messages.get(1);
			TestCase.assertTrue( msg.getLogRecordType()==XALogRecordType.USER);
			TestCase.assertTrue( msg.getData().length==1);
			TestCase.assertTrue(Arrays.equals("Log1".getBytes(), msg.getData()[0]));			


			msg= (ILogRecord)messages.get(2);
			TestCase.assertTrue( msg.getLogRecordType()==XALogRecordType.XA_PREPARED);
			
			msg= (ILogRecord)messages.get(3);
			TestCase.assertTrue( msg.getLogRecordType()==XALogRecordType.XA_COMMIT);
			TestCase.assertTrue( msg.getData().length==2);
			TestCase.assertTrue(Arrays.equals("A".getBytes(), msg.getData()[0]));	
			TestCase.assertTrue(Arrays.equals("B".getBytes(), msg.getData()[1]));	
			

			msg= (ILogRecord)messages.get(5);
			TestCase.assertTrue( msg.getLogRecordType()==XALogRecordType.XA_DONE);
			
		
		
		} finally {			
			logger.close(); 
		}
		
	}
	
	
}
