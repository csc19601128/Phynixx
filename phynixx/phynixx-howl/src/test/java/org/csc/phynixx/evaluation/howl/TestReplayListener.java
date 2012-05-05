package org.csc.phynixx.evaluation.howl;

import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.objectweb.howl.log.LogException;
import org.objectweb.howl.log.LogRecord;
import org.objectweb.howl.log.LogRecordType;
import org.objectweb.howl.log.ReplayListener;
import org.objectweb.howl.log.xa.XALogRecord;



class TestReplayListener implements ReplayListener {
	
	private IPhynixxLogger log= PhynixxLogManager.getLogger(this.getClass());
	
    public void onRecord (LogRecord lr) {

    	XALogRecord record= (XALogRecord)lr;
    	byte[][] content= lr.getFields();
    	log.info("Field size = ["+content.length+"]");
    	
    	if( content!=null) {
    		for(int i=0;i <content.length;i++) {
    			byte[] cc= content[i];
    			log.info("field["+i+"]="+new String(cc));
    		}
    	}
    	log.info("XACommittingTX="+record.getTx());
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