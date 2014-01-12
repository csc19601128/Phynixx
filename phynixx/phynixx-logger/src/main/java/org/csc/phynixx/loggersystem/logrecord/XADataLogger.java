package org.csc.phynixx.loggersystem.logrecord;

import org.apache.commons.io.IOUtils;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.exceptions.ExceptionUtils;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.channellogger.AccessMode;

import java.io.*;

/**
 * brings IXADataRecorder and dataLoger together
 * Created by christoph on 10.01.14.
 */
public class XADataLogger {

    private class RecoverReplayListener implements ILogRecordReplayListener {

        private int count = 0;
        private String loggerName;

        private PhynixxXADataRecorder dataRecorder;

        private RecoverReplayListener(PhynixxXADataRecorder dataRecorder) {
            this.dataRecorder = dataRecorder;
        }

        public int getCountLogRecords() {
            return count;
        }


        public void onRecord(XALogRecordType recordType, byte[][] fieldData) {
            if (count == 0) {
                recoverMessageSequenceId(fieldData[0]);
            } else {
                short typeId = recordType.getType();
                switch (typeId) {
                    case XALogRecordType.XA_START_TYPE:
                    case XALogRecordType.XA_PREPARED_TYPE:
                    case XALogRecordType.XA_COMMIT_TYPE:
                    case XALogRecordType.XA_DONE_TYPE:
                    case XALogRecordType.USER_TYPE:
                        XADataLogger.this.recoverData(dataRecorder, recordType, fieldData);
                        break;
                    default:
                        LOGGER.error("Unknown LogRecordtype " + recordType);
                        break;
                }
            }
            count++;
        }

    }


    private static final IPhynixxLogger LOGGER = PhynixxLogManager.getLogger(XADataLogger.class);

    private static int HEADER_SIZE = 8 + 4;

    private IDataLogger dataLogger;


    XADataLogger(IDataLogger dataLogger) {
        this.dataLogger = dataLogger;
    }

    /**
     * prepares the Logger for writing.
     *
     * @param dataRecorder
     * @throws IOException
     * @throws InterruptedException
     */
    void prepareForWrite(PhynixxXADataRecorder dataRecorder) throws IOException, InterruptedException {
        this.dataLogger.open(AccessMode.WRITE);
        this.writeStartSequence(dataRecorder);
    }

    /**
     * prepares the Logger for writing.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    void prepareForAppend(PhynixxXADataRecorder dataRecorder) throws IOException, InterruptedException {
        this.dataLogger.open(AccessMode.APPEND);
    }

    /**
     * prepares the Logger for writing.
     *
     * @param dataRecorder
     * @throws IOException
     * @throws InterruptedException
     */
    void prepareForRead(PhynixxXADataRecorder dataRecorder) throws IOException, InterruptedException {
        this.dataLogger.open(AccessMode.READ);
    }

    void writeData(PhynixxXADataRecorder dataRecorder, IDataRecord message) throws IOException {
        DataOutputStream io = null;
        try {

            ByteArrayOutputStream byteIO = new ByteArrayOutputStream(HEADER_SIZE);
            io = new DataOutputStream(byteIO);

            io.writeLong(message.getXADataRecorderId());
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
                this.dataLogger.write(message.getLogRecordType().getType(), content);
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

    void recover(PhynixxXADataRecorder dataRecorder) throws IOException, InterruptedException {
        RecoverReplayListener listener = new RecoverReplayListener(dataRecorder);
        dataRecorder.reset();
        this.dataLogger.replay(listener);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("# Records=" + listener.getCountLogRecords());
        }
    }

    private void recoverData(PhynixxXADataRecorder dataRecorder, XALogRecordType logRecordType, byte[][] fieldData) {
        if (LOGGER.isDebugEnabled()) {
            if (fieldData == null || fieldData.length == 0) {
                throw new IllegalArgumentException("Record fields are empty");
            }
        }
            // field 0 is header
        byte[] headerData = fieldData[0];
        DataInputStream io = new DataInputStream(new ByteArrayInputStream(headerData));
        try {
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

            PhynixxDataRecord msg = new PhynixxDataRecord(dataRecorder.getXADataRecorderId(), ordinal, logRecordType, content);
            dataRecorder.addMessage(msg);


        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        } finally {
            if (io != null) {
                IOUtils.closeQuietly(io);
            }
        }
    }

    void close() {
        try {
            this.dataLogger.close();
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

    }

    /**
     * Start Sequence writes the ID of the XAdataLogger to identify the content of the logger
     */

    private void writeStartSequence(IXADataRecorder dataRecorder) throws IOException, InterruptedException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            DataOutputStream dos = new DataOutputStream(byteOut);
            dos.writeLong(dataRecorder.getXADataRecorderId());
            dos.flush();
        } finally {
            if (byteOut != null) {
                IOUtils.closeQuietly(byteOut);
            }
        }

        byte[][] startSequence = new byte[1][];
        startSequence[0] = byteOut.toByteArray();

        this.dataLogger.write(XALogRecordType.USER.getType(), startSequence);
    }

    private long recoverMessageSequenceId(byte[] bytes) {
        byte[] headerData = bytes;
        DataInputStream io = new DataInputStream(new ByteArrayInputStream(headerData));
        try {
            long messageSequenceId = io.readLong();
            return messageSequenceId;
        } catch (IOException e) {
            throw new DelegatedRuntimeException(e);
        }
    }

}
