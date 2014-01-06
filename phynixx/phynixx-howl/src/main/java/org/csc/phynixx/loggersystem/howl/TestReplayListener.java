package org.csc.phynixx.loggersystem.howl;

/*
 * #%L
 * phynixx-howl
 * %%
 * Copyright (C) 2014 csc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.objectweb.howl.log.LogException;
import org.objectweb.howl.log.LogRecord;
import org.objectweb.howl.log.LogRecordType;
import org.objectweb.howl.log.ReplayListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


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
