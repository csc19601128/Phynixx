package org.objectweb.howl.log;

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


import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.ILogRecordReplayListener;
import org.csc.phynixx.loggersystem.ILogger;
import org.csc.phynixx.loggersystem.XALogRecordType;

import java.io.IOException;

public class HowlLogger extends Logger implements ILogger {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());
    private String loggerName = null;

    public String getLoggerName() {
        return loggerName;
    }

    public HowlLogger(Configuration config) throws IOException {
        super(config);
        this.loggerName = config.getLogFileName();
    }


    public void close() throws InterruptedException, IOException {
        try {
            super.close();
            // Bugfixing - the flushmanager is marked as closed but not stopped at all
            this.bmgr.flushManager.interrupt();
        } catch (InterruptedException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new DelegatedRuntimeException(e);
        }
    }


    public boolean isClosed() {
        return super.isClosed;
    }

    public String toString() {
        return "HowlLogger (" + this.loggerName + ")";
    }


    public void open() throws IOException, InterruptedException {
        try {
            super.open();
        } catch (InterruptedException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
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
    public long write(short type, byte[][] data)
            throws InterruptedException, IOException {
        try {
            return super.put(type, data, true);
        } catch (InterruptedException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
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

        private int count = 0;


        public RecoverReplayListener(ILogRecordReplayListener listener) {
            super();
            this.listener = listener;
        }

        public void onRecord(LogRecord lr) {

            count++;

            switch (lr.type) {
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
            log.error("RecoverReplayListener.onError " + exception);
        }


        public LogRecord getLogRecord() {
            if (log.isDebugEnabled()) {
                log.debug("getLogRecord - TestReplayListener started for replay");
            }
            return new LogRecord(120);
        }


    }


}
