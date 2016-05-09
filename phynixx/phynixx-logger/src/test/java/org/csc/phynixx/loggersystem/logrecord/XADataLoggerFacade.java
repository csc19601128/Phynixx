package org.csc.phynixx.loggersystem.logrecord;

import java.io.IOException;

public class XADataLoggerFacade {
	
	  
    private final static byte[][] EMPTY_DATA = new byte[][]{};
    private static int HEADER_SIZE = 8 + 4;

	/**
	 * logs user data into the message sequence
	 */
	public static void logUserData(IXADataRecorder xaDataRecorder, byte[][] data) {
		xaDataRecorder.writeData(XALogRecordType.USER, data);
	}

	public static void logUserData(IXADataRecorder sequence, byte[] data)
			throws InterruptedException, IOException {
		logUserData(sequence, new byte[][] { data });
	}

	/**
	 * Indicates that the XAResource has been prepared
	 * <p/>
	 * All information to perform a complete roll forward during commit are
	 * logged
	 * <p/>
	 * all previous rollback information are
	 *
	 * @param dataRecorder
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static  void preparedXA(IXADataRecorder dataRecorder) throws IOException {
		dataRecorder.writeData(XALogRecordType.XA_PREPARED, EMPTY_DATA);
	}

	/**
	 * Indicates that the XAResource has been prepared and enters the
	 * 'committing' state
	 * <p/>
	 * All information to perform a complete roll forward during commit are
	 * logged
	 *
	 * @param dataRecorder
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void committingXA(IXADataRecorder dataRecorder, byte[][] data)
			throws IOException {
		dataRecorder.writeData(XALogRecordType.ROLLFORWARD_DATA, data);
	}

	/**
	 * indicates the start of a TX,
	 * <p/>
	 * To recover this resource in the context of its XID, both the XID and the
	 * id of the resource have to be logged
	 *
	 * @param dataRecorder
	 * @param resourceId
	 * @param xid
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void startXA(IXADataRecorder dataRecorder, String resourceId,
			byte[] xid) throws IOException {
		dataRecorder.writeData(XALogRecordType.XA_START,
				new byte[][] { xid });

	}

	/**
	 * indicated the end of the TX
	 *
	 * @param dataRecorder
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void doneXA(IXADataRecorder dataRecorder) throws IOException {

		dataRecorder.writeData(XALogRecordType.XA_DONE, new byte[][] {});
	}

}
