package org.csc.phynixx.loggersystem.messages;

import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.exceptions.ExceptionUtils;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;

import java.io.*;

/**
 * brings IXADataRecorder and dataLoger together
 * Created by christoph on 10.01.14.
 */
public class XADataLogger {

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
                    XADataLogger.this.recoverData(recordType, fields);
                    break;
                default:
                    LOGGER.error("Unknown LogRecordtype " + recordType);
                    break;
            }
        }

    }

    private static final IPhynixxLogger LOGGER = PhynixxLogManager.getLogger(XADataLogger.class);

    private static int HEADER_SIZE = 8 + 4;

    private IDataLogger dataLogger;

    private final PhynixxXADataRecorder dataRecorder;

    XADataLogger(PhynixxXADataRecorder dataRecorder, IDataLogger dataLogger) {
        this.dataRecorder = dataRecorder;
        this.dataLogger = dataLogger;
    }

    void writeData(IDataRecord message) throws IOException {
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

    void recover() throws IOException {
        RecoverReplayListener listener = new RecoverReplayListener();
        this.dataRecorder.reset();
        this.dataLogger.replay(listener);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("# Records=" + listener.getCountLogRecords());
        }
    }

    private void recoverData(XALogRecordType logRecordType, byte[][] fieldData) {
        if (LOGGER.isDebugEnabled()) {
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

            PhynixxDataRecord msg = new PhynixxDataRecord(this.dataRecorder, ordinal, logRecordType, content);


        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

    }

}
