package org.csc.phynixx.phynixx.evaluation.howl;

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


import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.objectweb.howl.log.LogException;
import org.objectweb.howl.log.LogRecord;
import org.objectweb.howl.log.LogRecordType;
import org.objectweb.howl.log.ReplayListener;
import org.objectweb.howl.log.xa.XALogRecord;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


class ChainedMessageReplayListener implements ReplayListener {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());


    private static final int HEADER_SIZE = 16;


    private Map logMessages = new HashMap();


    public Map getLogMessages() {
        return logMessages;
    }

    public void onRecord(LogRecord lr) {

        XALogRecord record = (XALogRecord) lr;
        byte[][] content = lr.getFields();
        log.info("Field size = [" + ((content != null) ? content.length : 0) + "]");

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

        switch (lr.type) {
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
                    if (content.length > 0) {
                        byte[] c0 = content[0];
                        DataInputStream io = null;
                        long referencePtr = -1;
                        try {
                            ByteArrayInputStream byteIO = new ByteArrayInputStream(c0);
                            io = new DataInputStream(byteIO);

                            long backwardPtr = io.readLong();
                            long messageId = io.readLong();
                            // String messageChunk=io.readUTF();
                            byte[] data = new byte[c0.length - HEADER_SIZE];

                            log.info("Data-Size=" + c0.length + " HEADER-SIZE=" + HEADER_SIZE + " bufferSize=" + data.length);

                            io.read(data, 0, data.length);

                            log.info("BackwardPtr=" + backwardPtr + " messageId=" + messageId + " MessageChunk=" + new String(data));

                            MessageChunk mc = new MessageChunk(backwardPtr, messageId, data);
                            LogMessage lm = (LogMessage) this.logMessages.get(mc.getMessageId());
                            if (lm == null) {
                                lm = new LogMessage(mc.getMessageId());
                                this.logMessages.put(lm.getMessageId(), lm);
                            }

                            // add the message chunk ....
                            lm.addMessageChunk(mc);

                        } catch (IOException ioException) {
                            log.error("IOException " + ioException.getMessage());
                        } finally {
                            if (io != null) {
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
