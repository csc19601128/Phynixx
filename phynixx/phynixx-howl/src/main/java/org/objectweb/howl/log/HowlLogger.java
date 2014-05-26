package org.objectweb.howl.log;

import java.io.IOException;

import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.ILogRecordReplayListener;
import org.csc.phynixx.loggersystem.ILogger;
import org.csc.phynixx.loggersystem.XALogRecordType;
import org.objectweb.howl.log.Configuration;
import org.objectweb.howl.log.LogConfigurationException;
import org.objectweb.howl.log.LogException;
import org.objectweb.howl.log.LogRecord;
import org.objectweb.howl.log.LogRecordType;
import org.objectweb.howl.log.Logger;
import org.objectweb.howl.log.ReplayListener;

public class HowlLogger extends Logger implements ILogger {

	private IPhynixxLogger log= PhynixxLogManager.getLogger(this.getClass());
	private String loggerName=null;

	public String getLoggerName() {
		return loggerName;
	}

	public HowlLogger(Configuration config) throws IOException {
		super(config);
		this.loggerName= config.getLogFileName();
	}
	

	public void close() throws InterruptedException, IOException  {		
		  try {				
			  super.close();
			  // Bugfixing - the flushmanager is marked as closed but not stopped at all
			  this.bmgr.flushManager.interrupt();
		  } catch(InterruptedException e) {
			  throw e;
		  } catch (IOException e) {
			  throw e;
		  } catch(Throwable e) {
			  throw new DelegatedRuntimeException(e);
		  }
	}
	
	

	
	public boolean isClosed() {
		return super.isClosed;
	}
	
	 public String toString() {
		 return "HowlLogger ("+this.loggerName+")";
	 }
	
	
	
	
	public void open() throws IOException,InterruptedException 
	{
	  try {
		super.open();
	  } catch(InterruptedException e) {
		  throw e;
	  } catch (IOException e) {
		  throw e;
	  } catch(Throwable e) {
		  throw new DelegatedRuntimeException(e);
	  }
	}

	/**
	   * Sub-classes call this method to write log records with
	   * a specific record type.
	   * 
	   * @param type a record type defined in LogRecordType.
	   * @param data record data to be logged.
	   * @return a log key that can be used to reference
	   * the record.
	   */
	  public long write (short type, byte[][] data)
	  			                     throws InterruptedException, IOException {
		  try {
	        return super.put(type, data, true);
		  } catch(InterruptedException e) {
			  throw e;
		  } catch (IOException e) {
			  throw e;
		  } catch(Throwable e) {
			  throw new DelegatedRuntimeException(e);
		  }
	  }
	  
	  public void replay(ILogRecordReplayListener replayListener) {
		  try {
			this.replay(new RecoverReplayListener(replayListener));
		} catch (LogConfigurationException e) {
			throw new DelegatedRuntimeException(e);
		}
		
	  }

	private class RecoverReplayListener implements ReplayListener {
			
		private ILogRecordReplayListener listener = null; 
		
		private int count=0; 
		
		
			
		public RecoverReplayListener(ILogRecordReplayListener listener) {
			super();
			this.listener = listener;
		}

		public void onRecord (LogRecord lr) {

	    	count++; 
	    	
	        switch(lr.type) {
	            case LogRecordType.EOB:
	                if (log.isDebugEnabled()) {
	                    log.debug("Howl End of Buffer Record");
	                }
	                break;
	            case LogRecordType.END_OF_LOG:
	                if (log.isDebugEnabled()) {
	                    log.debug("Howl End of Log Record");
	                }
	                break;
	            case XALogRecordType.XA_START_TYPE:
	            case XALogRecordType.XA_PREPARED_TYPE:
	            case XALogRecordType.XA_COMMIT_TYPE:
	            case XALogRecordType.XA_DONE_TYPE:   
	            case XALogRecordType.USER_TYPE:	
	            	this.listener.onRecord(lr.type, lr.getFields());
	                break;
	            default:
	                if (log.isDebugEnabled()) {
	                    log.debug("Unknown Howl LogRecord");
	                }
	                break;
	        }
	    }

	    public void onError(LogException exception) {
	            log.error("RecoverReplayListener.onError "+ exception);
	    }


	    public LogRecord getLogRecord() {
	        if (log.isDebugEnabled()) {
	            log.debug("getLogRecord - TestReplayListener started for replay");
	        }
	        return new LogRecord(120);
	    }

	
	};



}
