package org.csc.phynixx.loggersystem;

/*
 * #%L
 * phynixx-connection
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


import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.exceptions.ExceptionUtils;
import org.csc.phynixx.generator.IDGenerator;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.messages.*;

import java.io.*;
import java.util.*;


/**
 * XAResource logger is specialized to support the logging of a xaresource to rollback/recover the
 * resource in the context of an transaction manager.
 * <p/>
 * <table>
 * <tr>
 * <td><i>prepare</i></td>
 * <td>XAResource muss sich an der wiederhergestellten
 * globalen TX beteiligen , um den Transactionsmanager �ber den
 * korrekten Abschluss der TX entscheiden zu lassen.
 * </td>
 * </tr>
 * <p/>
 * <tr>
 * <td><i>committing</i></td>
 * <p/>
 * <td>XAResource f�hrt abschliessende Arbeiten aus, um das
 * <i>commit</i> abzuschliessen. Es ist keine
 * globale TX notwendig, innerhalb derer die XAResource
 * <i>committed</i> werden muss. (<i>roll
 * forward</i>)</td>
 * </tr>
 * <p/>
 * <tr>
 * <td><i>executing/aborting</i></td>
 * <p/>
 * <td>XAResource f�hrt abschliessende Arbeiten aus, um dass
 * <i>rollback/abort</i> abzuschliessen. Es ist
 * keine globale TX notwendig, innerhalb derer die XAResource
 * <i>rollbacked</i> werden muss.</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>keiner der obigen Zust�nde</td>
 * <p/>
 * <td>Da nicht klar ist, ob die XAResource w�hrend
 * <i>executing phase</i> oder des
 * <i>prepares</i> abgebrochen ist, wird die
 * XAResource zuerst der untersucht, ob zur XAResource eine
 * wiederhergestellte, globale TX existiert. Wenn ja, so wird die
 * XAResource dieser �bergeben, allerdings im Zustand
 * MARK_ROLLBACK. Wenn nein, so wird ein
 * <i>abort</i> durchgef�hrt.</td>
 * </tr>
 * </table>
 *
 * @author christoph
 */
public class XAResourceLogger
        implements ILogWriter, ILogReader, ILogRecordListener, ILogRecordSequenceListener {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private static int HEADER_SIZE = 8 + 4;

    private ILogger logger = null;


    /**
     * ILoggereListeners watching the lifecycle of this logger
     */
    private List listeners = new ArrayList();

    private SortedMap messageSequences = new TreeMap();

    private IDGenerator messageSeqGenerator = new IDGenerator();


    public XAResourceLogger(ILogger logger) throws IOException {
        this.logger = logger;
        if (this.logger == null) {
            throw new IllegalArgumentException("No logger set");
        }
    }


    public boolean isClosed() {
        synchronized (this) {
            return this.logger.isClosed();
        }
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getLoggerName() == null) ? 0 : getLoggerName().hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final XAResourceLogger other = (XAResourceLogger) obj;
        if (getLoggerName() == null) {
            if (other.getLoggerName() != null)
                return false;
        } else if (!this.getLoggerName().equals(other.getLoggerName()))
            return false;
        return true;
    }

    public String toString() {
        return (this.logger == null) ? "Closed Logger" : this.logger.toString();
    }

    public String getLoggerName() {
        return logger.getLoggerName();
    }


    /**
     * logs user data into the message sequence
     */
    public void logUserData(ILogRecordSequence sequence, byte[][] data) throws InterruptedException, IOException {
        ILogRecord message = sequence.createNewMessage(XALogRecordType.USER);
        message.setData(data);
    }

    public void logUserData(ILogRecordSequence sequence, byte[] data) throws InterruptedException, IOException {
        this.logUserData(sequence, new byte[][]{data});
    }


    /**
     * Indicates that the XAResource has been prepared
     * <p/>
     * All information to perform a complete roll forward during commit are logged
     * <p/>
     * all previous rollback information are
     *
     * @param data
     * @param sync
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws LogFileOverflowException
     * @throws LogRecordSizeException
     * @throws LogClosedException
     */
    public void preparedXA(ILogRecordSequence sequence) {
        ILogRecord message = sequence.createNewMessage(XALogRecordType.XA_PREPARED);
        message.setData(new byte[]{});
    }


    /**
     * Indicates that the XAResource has been prepared and enters the 'committing' state
     * <p/>
     * All information to perform a complete roll forward during commit are logged
     *
     * @param data
     * @param sync
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws LogFileOverflowException
     * @throws LogRecordSizeException
     * @throws LogClosedException
     */
    public void committingXA(ILogRecordSequence sequence, byte[][] data) throws InterruptedException, IOException {
        ILogRecord message = sequence.createNewMessage(XALogRecordType.XA_COMMIT);
        message.setData(data);
    }


    /**
     * indicates the start of a TX,
     * <p/>
     * To recover this resource in the context of its XID, both the XIOD and the id of the resource have to be logged
     *
     * @param resourceId
     * @param xid
     * @return
     * @throws IOException
     * @throws LogClosedException
     * @throws LogRecordSizeException
     * @throws LogFileOverflowException
     * @throws InterruptedException
     */
    public void startXA(ILogRecordSequence sequence, String resourceId, byte[] xid) throws IOException, InterruptedException {
        DataOutputStream outputIO = null;
        try {

            byte[] data = null;
            ByteArrayOutputStream byteIO = new ByteArrayOutputStream();
            outputIO = new DataOutputStream(byteIO);

            outputIO.writeUTF(resourceId);
            outputIO.writeInt(xid.length);
            outputIO.write(xid);

            outputIO.flush();

            data = byteIO.toByteArray();

            ILogRecord message = sequence.createNewMessage(XALogRecordType.XA_START);
            message.setData(data);

        } finally {
            if (outputIO != null) outputIO.close();
        }

    }

    /**
     * indicated the end of the TX
     *
     * @return
     * @throws LogClosedException
     * @throws LogRecordSizeException
     * @throws LogFileOverflowException
     * @throws InterruptedException
     * @throws IOException
     */
    public void doneXA(ILogRecordSequence sequence) {

        ILogRecord message = sequence.createNewMessage(XALogRecordType.XA_DONE);
        message.setData(new byte[]{});
    }


    public synchronized void open() throws IOException, InterruptedException {
        if (this.logger == null) {
            throw new IllegalStateException("No logger set");
        }
        this.logger.open();
        // messageSequences.clear();
        fireConnectionOpened();
    }

    public synchronized void close() throws IOException, InterruptedException {
        if (!isClosed()) {
            if (this.logger != null) {
                this.logger.close();
            }
            messageSequences.clear();
            fireConnectionClosed();
        }
    }

    public synchronized void destroy() throws IOException, InterruptedException {
        this.close();
        this.listeners = new ArrayList();
        this.messageSeqGenerator = null;
    }


    public synchronized ILogRecordSequence createMessageSequence() {
        PhynixxLogRecordSequence seq = new PhynixxLogRecordSequence(messageSeqGenerator.generateLong());
        seq.addLogRecordListener(this);
        seq.addLogRecordSequenceListener(this);
        this.addMessageSequence(seq);
        return seq;
    }

    private PhynixxLogRecordSequence recoverMessageSequence(Long messageSequenceId) {
        PhynixxLogRecordSequence seq = new PhynixxLogRecordSequence(messageSequenceId);
        this.addMessageSequence(seq);
        return seq;
    }

    public void writeData(ILogRecord message) throws IOException {
        DataOutputStream io = null;
        try {

            ByteArrayOutputStream byteIO = new ByteArrayOutputStream(HEADER_SIZE);
            io = new DataOutputStream(byteIO);

            io.writeLong(message.getRecordSequenceId().longValue());
            io.writeInt(message.getOrdinal().intValue());
            byte[] header = byteIO.toByteArray();

            byte[][] data = message.getData();
            byte[][] content = null;
            if (data == null) {
                content = new byte[][]{header};
            } else {
                content = new byte[data.length + 1][];
                content[0] = header;
                for (int i = 0; i < data.length; i++) {
                    content[i + 1] = data[i];
                }
            }

            try {
                this.logger.write(message.getLogRecordType().getType(), content);
            } catch (Exception e) {
                throw new DelegatedRuntimeException("writing message " + message + "\n" + ExceptionUtils.getStackTrace(e), e);
            }
        } finally {
            if (io != null) {
                io.close();
            }
        }

        // Add the messageSequence to the set og messageSequences ...
    }


    private ILogRecord recoverData(short logRecordType, byte[][] fieldData) {
        if (log.isDebugEnabled()) {
            log.debug("Howl User Record");
            if (fieldData == null || fieldData.length == 0) {
                throw new IllegalArgumentException("Record fields are empty");
            }
        }

        try {

            // field 0 is header
            byte[] headerData = fieldData[0];
            DataInputStream io = new DataInputStream(new ByteArrayInputStream(headerData));
            long messageSequenceId = io.readLong();
            int ordinal = io.readInt();

            byte[][] content = null;

            if (fieldData.length > 1) {
                content = new byte[fieldData.length - 1][];
                for (int i = 0; i < fieldData.length - 1; i++) {
                    content[i] = fieldData[i + 1];
                }
            } else {
                content = new byte[][]{};
            }


            // try to find the messageSequence ....
            PhynixxLogRecordSequence seq = null;
            Long msgID = new Long(messageSequenceId);
            if (XAResourceLogger.this.messageSequences.containsKey(msgID)) {
                seq = (PhynixxLogRecordSequence) this.messageSequences.get(msgID);
            } else {
                seq = this.recoverMessageSequence(msgID);
            }

            // recover the message
            ILogRecord msg = seq.recover(ordinal, logRecordType, content);

            return msg;
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

    }

    private void addMessageSequence(ILogRecordSequence sequence) {
        if (!this.messageSequences.containsKey(sequence.getLogRecordSequenceId())) {
            this.messageSequences.put(sequence.getLogRecordSequenceId(), sequence);
        }
    }

    public List getOpenMessageSequences() {
        List seqs = new ArrayList(this.messageSequences.size());
        for (Iterator iterator = messageSequences.values().iterator(); iterator.hasNext(); ) {
            seqs.add(iterator.next());
        }
        return seqs;
    }


    public void recordCompleted(ILogRecord message) {
        try {
            this.writeData(message);
        } catch (IOException e) {
            throw new DelegatedRuntimeException("Writing message " + message, e);
        }
    }


    public void recordCreated(ILogRecord message) {
        return;
    }

    public void recordSequenceCompleted(ILogRecordSequence sequence) {
        if (this.messageSequences.containsKey(sequence.getLogRecordSequenceId())) {
            // schreibe abschluss Satz .....

        }
        this.removeMessageSequence(sequence);
    }

    private void removeMessageSequence(ILogRecordSequence sequence) {
        this.messageSequences.remove(sequence.getLogRecordSequenceId());
    }

    public void recordSequenceCreated(ILogRecordSequence sequence) {
        this.addMessageSequence(sequence);
    }

    public void readMessageSequences() throws IOException {
        RecoverReplayListener listener = new RecoverReplayListener();
        this.logger.replay(listener);
        if (log.isDebugEnabled()) {
            log.debug("# Records=" + listener.getCountLogRecords());
        }
    }

    public synchronized void addListener(ILoggerListener listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public synchronized void removeListener(ILoggerListener listener) {
        this.listeners.remove(listener);
    }

    interface IEventDeliver {
        void fireEvent(ILoggerListener listener);
    }

    private void fireEvents(IEventDeliver deliver) {

        if (this.listeners == null || this.listeners.size() == 0) {
            return;
        }

        // copy all listeners as the callback may change the list of listeners ...
        List tmp = new ArrayList(this.listeners);
        for (int i = 0; i < tmp.size(); i++) {
            ILoggerListener listener = (ILoggerListener) tmp.get(i);
            deliver.fireEvent(listener);
        }
    }


    protected void fireConnectionClosed() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(ILoggerListener listener) {
                listener.loggerClosed(XAResourceLogger.this);
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionOpened() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(ILoggerListener listener) {
                listener.loggerOpened(XAResourceLogger.this);
            }
        };
        fireEvents(deliver);
    }


    private class RecoverReplayListener implements ILogRecordReplayListener {

        private int count = 0;

        public int getCountLogRecords() {
            return count;
        }

        public void onRecord(short type, byte[][] fields) {

            count++;
            switch (type) {
                case XALogRecordType.XA_START_TYPE:
                case XALogRecordType.XA_PREPARED_TYPE:
                case XALogRecordType.XA_COMMIT_TYPE:
                case XALogRecordType.XA_DONE_TYPE:
                case XALogRecordType.USER_TYPE:
                    XAResourceLogger.this.recoverData(type, fields);
                    break;
                default:
                    log.error("Unknown LogRecordtype " + type);
                    break;
            }
        }

    }

}
