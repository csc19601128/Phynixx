package org.csc.phynixx.loggersystem.howl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.objectweb.howl.log.LogException;
import org.objectweb.howl.log.LogRecord;
import org.objectweb.howl.log.LogRecordType;
import org.objectweb.howl.log.ReplayListener;



class TestReplayListener implements ReplayListener {
	
	private IPhynixxLogger log= PhynixxLogManager.getLogger(this.getClass());
	
    public void onRecord (LogRecord lr) {

    	byte[][] content= lr.getFields();
    	log.info("Field size = ["+content.length+"]");
    	
    	if( content!=null) {
    		for(int i=0;i <content.length;i++) {
    			byte[] cc= content[i];
    			log.info("field["+i+"]="+new String(cc));
    		}
    	}
    	/*
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
                    log.info("Howl User Record");  
                	byte[][] fieldData= lr.getFields();
                	if(fieldData==null || fieldData.length==0) {
                		break;
                	}
                	for( int i=0; i< fieldData.length;i++) {
                		try {
							DataInputStream io= new DataInputStream(new ByteArrayInputStream(fieldData[i]));
							long messageSequenceId= io.readLong();
							int ordinal= io.readInt();
							byte[] data= new byte[lr.data.length];
							int readbytes= io.read(data);
							String s= new String(data,0,readbytes);
							log.info("Record messageSequenceId="+messageSequenceId+" ordinal="+ordinal+" content="+s);
						} catch (IOException e) {
							e.printStackTrace();
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
        return new LogRecord(120);
    }
}