package org.csc.phynixx.loggersystem.messages;

/*
 * #%L
 * phynixx-common
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
import org.csc.phynixx.loggersystem.logger.IDataLogger;

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
 * globalen TX beteiligen , um den Transactionsmanager ueber den
 * korrekten Abschluss der TX entscheiden zu lassen.
 * </td>
 * </tr>
 * <p/>
 * <tr>
 * <td><i>committing</i></td>
 * <p/>
 * <td>XAResource fuehrt abschliessende Arbeiten aus, um das
 * <i>commit</i> abzuschliessen. Es ist keine
 * globale TX notwendig, innerhalb derer die XAResource
 * <i>committed</i> werden muss. (<i>roll
 * forward</i>)</td>
 * </tr>
 * <p/>
 * <tr>
 * <td><i>executing/aborting</i></td>
 * <p/>
 * <td>XAResource fuehrt abschliessende Arbeiten aus, um dass
 * <i>rollback/abort</i> abzuschliessen. Es ist
 * keine globale TX notwendig, innerhalb derer die XAResource
 * <i>rollbacked</i> werden muss.</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>keiner der obigen Zustaende</td>
 * <p/>
 * <td>Da nicht klar ist, ob die XAResource waehrend
 * <i>executing phase</i> oder des
 * <i>prepares</i> abgebrochen ist, wird die
 * XAResource zuerst der untersucht, ob zur XAResource eine
 * wiederhergestellte, globale TX existiert. Wenn ja, so wird die
 * XAResource dieser uebergeben, allerdings im Zustand
 * MARK_ROLLBACK. Wenn nein, so wird ein
 * <i>abort</i> durchgefuehrt.</td>
 * </tr>
 * </table>
 *
 * @author christoph
 */
class PhynixxXARecorderResource implements IXARecorderResource {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private static int HEADER_SIZE = 8 + 4;

    private IDataLogger logger = null;


    /**
     * ILoggereListeners watching the lifecycle of this logger
     */
    private List listeners = new ArrayList();

    private SortedMap messageSequences = new TreeMap();

    private IDGenerator messageSeqGenerator = new IDGenerator();


    public PhynixxXARecorderResource(IDataLogger logger) throws IOException {
        this.logger = logger;
        if (this.logger == null) {
            throw new IllegalArgumentException("No logger set");
        }
    }


    @Override
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
        final PhynixxXARecorderResource other = (PhynixxXARecorderResource) obj;
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

    @Override
    public String getLoggerName() {
        return logger.getLoggerName();
    }


    /**
     * logs user data into the message sequence
     */
    @Override
    public void logUserData(IDataRecordSequence sequence, byte[][] data) throws InterruptedException, IOException {
        IDataRecord message = sequence.createNewMessage(XALogRecordType.USER);
        message.setData(data);
    }

    @Override
    public void logUserData(IDataRecordSequence sequence, byte[] data) throws InterruptedException, IOException {
        this.logUserData(sequence, new byte[][]{data});
    }


    /**
     * Indicates that the XAResource has been prepared
     * <p/>
     * All information to perform a complete roll forward during commit are logged
     * <p/>
     * all previous rollback information are
     *
     * @param sequence
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void preparedXA(IDataRecordSequence sequence) {
        IDataRecord message = sequence.createNewMessage(XALogRecordType.XA_PREPARED);
        message.setData(new byte[]{});
    }


    /**
     * Indicates that the XAResource has been prepared and enters the 'committing' state
     * <p/>
     * All information to perform a complete roll forward during commit are logged
     *
     * @param sequence
     * @param data
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void committingXA(IDataRecordSequence sequence, byte[][] data) throws InterruptedException, IOException {
        IDataRecord message = sequence.createNewMessage(XALogRecordType.XA_COMMIT);
        message.setData(data);
    }


    /**
     * indicates the start of a TX,
     * <p/>
     * To recover this resource in the context of its XID, both the XID and the id of the resource have to be logged
     *
     * @param resourceId
     * @param xid
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void startXA(IDataRecordSequence sequence, String resourceId, byte[] xid) throws IOException, InterruptedException {
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

            IDataRecord message = sequence.createNewMessage(XALogRecordType.XA_START);
            message.setData(data);

        } finally {
            if (outputIO != null) outputIO.close();
        }

    }

    /**
     * indicated the end of the TX
     *
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public void doneXA(IDataRecordSequence sequence) {

        IDataRecord message = sequence.createNewMessage(XALogRecordType.XA_DONE);
        message.setData(new byte[]{});
    }


    @Override
    public synchronized void open() throws IOException, InterruptedException {
        if (this.logger == null) {
            throw new IllegalStateException("No logger set");
        }
        this.logger.open();
        // messageSequences.clear();
        fireConnectionOpened();
    }

    @Override
    public synchronized void close() throws IOException, InterruptedException {
        if (!isClosed()) {
            if (this.logger != null) {
                this.logger.close();
            }
            messageSequences.clear();
            fireConnectionClosed();
        }
    }

    @Override
    public synchronized void destroy() throws IOException, InterruptedException {
        this.close();
        this.listeners = new ArrayList();
        this.messageSeqGenerator = null;
    }


    @Override
    public synchronized IDataRecordSequence createMessageSequence() {
        PhynixxXADataRecorder seq = new PhynixxXADataRecorder(messageSeqGenerator.generateLong(), this);
        this.addMessageSequence(seq);
        return seq;
    }

    private PhynixxXADataRecorder recoverMessageSequence(Long messageSequenceId) {
        PhynixxXADataRecorder seq = new PhynixxXADataRecorder(messageSequenceId, this);
        this.addMessageSequence(seq);
        return seq;
    }

    @Override
    public void writeData(IDataRecord message) throws IOException {
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


    private IDataRecord recoverData(XALogRecordType logRecordType, byte[][] fieldData) {
        if (log.isDebugEnabled()) {
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
            PhynixxXADataRecorder seq = null;
            Long msgID = new Long(messageSequenceId);
            if (PhynixxXARecorderResource.this.messageSequences.containsKey(msgID)) {
                seq = (PhynixxXADataRecorder) this.messageSequences.get(msgID);
            } else {
                seq = this.recoverMessageSequence(msgID);
            }

            // recover the message
            IDataRecord msg = seq.recover(ordinal, logRecordType, content);

            return msg;
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

    }

    private void addMessageSequence(IDataRecordSequence sequence) {
        if (!this.messageSequences.containsKey(sequence.getLogRecordSequenceId())) {
            this.messageSequences.put(sequence.getLogRecordSequenceId(), sequence);
        }
    }

    @Override
    public List<IDataRecordSequence> getOpenMessageSequences() {
        List<IDataRecordSequence> seqs = new ArrayList<IDataRecordSequence>(this.messageSequences.size());
        for (Iterator<IDataRecordSequence> iterator = messageSequences.values().iterator(); iterator.hasNext(); ) {
            seqs.add(iterator.next());
        }
        return seqs;
    }


    void recordCompleted(IDataRecord message) {
        try {
            this.writeData(message);
        } catch (IOException e) {
            throw new DelegatedRuntimeException("Writing message " + message, e);
        }
    }


    void recordCreated(IDataRecord message) {
        return;
    }

    void recordSequenceCompleted(IDataRecordSequence sequence) {
        if (this.messageSequences.containsKey(sequence.getLogRecordSequenceId())) {
            // schreibe abschluss Satz .....

        }
        this.removeMessageSequence(sequence);
    }

    private void removeMessageSequence(IDataRecordSequence sequence) {
        this.messageSequences.remove(sequence.getLogRecordSequenceId());
    }

    public void recordSequenceCreated(IDataRecordSequence sequence) {
        this.addMessageSequence(sequence);
    }

    public void readMessageSequences() throws IOException {
        RecoverReplayListener listener = new RecoverReplayListener();
        this.logger.replay(listener);
        if (log.isDebugEnabled()) {
            log.debug("# Records=" + listener.getCountLogRecords());
        }
    }

    void addListener(IXARecorderResourceListener listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    void removeListener(IXARecorderResourceListener listener) {
        this.listeners.remove(listener);
    }

    private void fireEvents(IEventDeliver deliver) {

        if (this.listeners == null || this.listeners.size() == 0) {
            return;
        }

        // copy all listeners as the callback may change the list of listeners ...
        List tmp = new ArrayList(this.listeners);
        for (int i = 0; i < tmp.size(); i++) {
            IXARecorderResourceListener listener = (IXARecorderResourceListener) tmp.get(i);
            deliver.fireEvent(listener);
        }
    }


    protected void fireConnectionClosed() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IXARecorderResourceListener listener) {
                listener.recorderResourceClosed(PhynixxXARecorderResource.this);
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionOpened() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IXARecorderResourceListener listener) {
                listener.recorderResourceOpened(PhynixxXARecorderResource.this);
            }
        };
        fireEvents(deliver);
    }


    private class RecoverReplayListener implements ILogRecordReplayListener {

        private int count = 0;

        public int getCountLogRecords() {
            return count;
        }

        public void onRecord(XALogRecordType recordType, byte[][] fields) {

            short typeId = recordType.getType();
            count++;
            switch (typeId) {
                case XALogRecordType.XA_START_TYPE:
                case XALogRecordType.XA_PREPARED_TYPE:
                case XALogRecordType.XA_COMMIT_TYPE:
                case XALogRecordType.XA_DONE_TYPE:
                case XALogRecordType.USER_TYPE:
                    PhynixxXARecorderResource.this.recoverData(recordType, fields);
                    break;
                default:
                    log.error("Unknown LogRecordtype " + recordType);
                    break;
            }
        }

    }

}
