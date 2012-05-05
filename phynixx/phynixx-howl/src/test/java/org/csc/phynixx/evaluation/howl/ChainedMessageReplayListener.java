package org.csc.phynixx.evaluation.howl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.objectweb.howl.log.LogException;
import org.objectweb.howl.log.LogRecord;
import org.objectweb.howl.log.LogRecordType;
import org.objectweb.howl.log.ReplayListener;
import org.objectweb.howl.log.xa.XALogRecord;



class ChainedMessageReplayListener implements ReplayListener {
	
	private IPhynixxLogger log= PhynixxLogManager.getLogger(this.getClass());

	
	private static final int HEADER_SIZE= 16; 

	
	private Map logMessages= new HashMap(); 
	
	
	
    public Map getLogMessages() {
		return logMessages;
	}

	public void onRecord (LogRecord lr) {

    	XALogRecord record= (XALogRecord)lr;
    	byte[][] content= lr.getFields();
    	log.info("Field size = ["+( (content!=null)?content.length:0)+"]");
    	
    	/*
    	if( content!=null) {
    		for(int i=0;i <content.length;i++) {
    			byte[] cc= content[i];
    			log.info("field["+i+"]="+new String(cc));
    		}
    	}
    	log.info("XACommittingTX="+record.getTx());
    	
        if (log.isInfoEnabled()) {
            log.info("LogRecord type= " + lr.type);
        }
        */

        switch(lr.type) {
            case LogRecordType.EOB:
                if (log.isInfoEnabled()) {
                    log.info("Howl End of Buffer Record");
                }
                break;
            case LogRecordType.END_OF_LOG:
                if (log.isInfoEnabled()) {
                    log.info("Howl End of Log Record");
                }
                break;
            case LogRecordType.XACOMMIT:
                if (log.isInfoEnabled()) {
                    log.info("Howl XA Commit Record");
                }
                break;
            case LogRecordType.XADONE:
                if (log.isInfoEnabled()) {
                    log.info("Howl XA Done Record");
                }
                break;
            case LogRecordType.USER:
                if (log.isInfoEnabled()) {
                	if(content.length > 0) {
                		byte[] c0=content[0];
                		DataInputStream io= null;	
            			long referencePtr= -1;
            			try {
            				ByteArrayInputStream byteIO= new ByteArrayInputStream(c0);
            				io= new DataInputStream(byteIO); 

            				long backwardPtr= io.readLong();
            				long messageId= io.readLong();
            				// String messageChunk=io.readUTF(); 
            				byte[] data= new byte[c0.length- HEADER_SIZE];
            				
            				log.info("Data-Size="+c0.length+" HEADER-SIZE="+HEADER_SIZE+" bufferSize="+ data.length);
                			
            				io.read(data, 0, data.length);
            				
            				log.info("BackwardPtr="+backwardPtr+" messageId="+messageId+" MessageChunk="+new String(data));
            				
            				MessageChunk mc= new MessageChunk(backwardPtr,messageId,data);
            				LogMessage lm= (LogMessage) this.logMessages.get(mc.getMessageId()) ;
            				if( lm==null){
            					lm= new LogMessage(mc.getMessageId());
            					this.logMessages.put(lm.getMessageId(),lm);
            				}
            				
            				// add the message chunk ....
            				lm.addMessageChunk(mc);
            				
            			} catch (IOException ioException) {
            				log.error("IOException " + ioException.getMessage());
            			}finally {
            				if( io!=null) {
            					try {
									io.close();
								} catch (IOException e) {
		            				log.error("IOException closing stream" + e.getMessage());
								}
            				}
            			}
                	}
                }
                break;
            default:
                if (log.isInfoEnabled()) {
                    log.info("Unknown Howl LogRecord");
                }
                break;
        }
    }

    public void onError(LogException exception) {
        if (log.isInfoEnabled()) {
            log.info("onError");
        }
    }


    public LogRecord getLogRecord() {
        if (log.isInfoEnabled()) {
            log.info("getLogRecord - TestReplayListener started for replay");
        }
        return new XALogRecord(120);
    }
}