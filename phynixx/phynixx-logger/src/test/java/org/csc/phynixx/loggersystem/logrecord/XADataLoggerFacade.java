package org.csc.phynixx.loggersystem.logrecord;

/*
 * #%L
 * phynixx-logger
 * %%
 * Copyright (C) 2014 - 2017 Christoph Schmidt-Casdorff
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


import java.io.IOException;

public class XADataLoggerFacade {
	
	  
    private final static byte[][] EMPTY_DATA = new byte[][]{};
    private static int HEADER_SIZE = 8 + 4;

	/**
	 * logs user data into the message sequence
	 */
	public static void logUserData(IXADataRecorder xaDataRecorder, byte[][] data) {
		xaDataRecorder.createDataRecord(XALogRecordType.USER, data);
	}

	public static void logUserData(IXADataRecorder sequence, byte[] data)
			throws InterruptedException, IOException {
		logUserData(sequence, new byte[][] { data });
	}

	/**
	 * Indicates that the XAResource has been prepared

	 * All information to perform a complete roll forward during commit are
	 * logged

	 * all previous rollback information are
	 *
	 * @param dataRecorder
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static  void preparedXA(IXADataRecorder dataRecorder) throws IOException {
		dataRecorder.createDataRecord(XALogRecordType.XA_PREPARED, EMPTY_DATA);
	}

	/**
	 * Indicates that the XAResource has been prepared and enters the
	 * 'committing' state

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
		dataRecorder.createDataRecord(XALogRecordType.ROLLFORWARD_DATA, data);
	}

	/**
	 * indicates the start of a TX,

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
		dataRecorder.createDataRecord(XALogRecordType.XA_START,
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

		dataRecorder.createDataRecord(XALogRecordType.XA_DONE, new byte[][] {});
	}

}
