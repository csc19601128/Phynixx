package org.csc.phynixx.loggersystem.logrecord;

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


import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.generator.IDGenerator;
import org.csc.phynixx.common.generator.IDGenerators;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
public class PhynixxXARecorderRepository implements IXARecorderRepository {


    public interface IEventDeliver {
        void fireEvent(IXARecorderResourceListener listener);
    }

    private final static byte[][] EMPTY_DATA = new byte[][]{};

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private static int HEADER_SIZE = 8 + 4;

    private IDataLoggerFactory dataLoggerFactory = null;


    /**
     * ILoggerListeners watching the lifecycle of this logger
     */
    private List<IXARecorderResourceListener> listeners = new ArrayList<IXARecorderResourceListener>();

    private SortedMap<Long, PhynixxXADataRecorder> xaDataRecorders = new TreeMap<Long, PhynixxXADataRecorder>();

    private IDGenerator<Long> messageSeqGenerator = IDGenerators.synchronizeGenerator(IDGenerators.createLongGenerator(1l));


    public PhynixxXARecorderRepository(IDataLoggerFactory dataLoggerFactory) {
        this.dataLoggerFactory = dataLoggerFactory;
        if (this.dataLoggerFactory == null) {
            throw new IllegalArgumentException("No dataLoggerFactory set");
        }
    }

    @Override

    /**
     * opens a new Recorder for writing. The recorder gets a new ID.
     * @return created dataRecorder
     */
    public IXADataRecorder createXADataRecorder() {

        try {
            long xaDataRecorderId = this.messageSeqGenerator.generate();

            // create a new Logger
            IDataLogger dataLogger = this.dataLoggerFactory.instanciateLogger(Long.toString(xaDataRecorderId));

            // create a new XADataLogger
            XADataLogger xaDataLogger = new XADataLogger(dataLogger);


            PhynixxXADataRecorder xaDataRecorder = PhynixxXADataRecorder.openRecorderForWrite(xaDataRecorderId, xaDataLogger, this);
            addXADataRecorder(xaDataRecorder);
            return xaDataRecorder;

        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

    }

    @Override
    public IXADataRecorder findXADataRecord(long dataRecordId) {
        return this.xaDataRecorders.get(dataRecordId);
    }

    public String getLoggerSystemName() {
        return dataLoggerFactory.getLoggerSystemName();
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getLoggerSystemName() == null) ? 0 : getLoggerSystemName().hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final PhynixxXARecorderRepository other = (PhynixxXARecorderRepository) obj;
        if (getLoggerSystemName() == null) {
            if (other.getLoggerSystemName() != null)
                return false;
        } else if (!this.getLoggerSystemName().equals(other.getLoggerSystemName()))
            return false;
        return true;
    }

    public String toString() {
        return (this.dataLoggerFactory == null) ? "Closed Logger" : this.dataLoggerFactory.toString();
    }


    /**
     * logs user data into the message sequence
     */
    public void logUserData(IXADataRecorder xaDataRecorder, byte[][] data) {
        IDataRecord message = xaDataRecorder.createDataRecord(XALogRecordType.USER, data);
    }

    public void logUserData(IXADataRecorder sequence, byte[] data) throws InterruptedException, IOException {
        this.logUserData(sequence, new byte[][]{data});
    }


    /**
     * Indicates that the XAResource has been prepared
     * <p/>
     * All information to perform a complete roll forward during commit are logged
     * <p/>
     * all previous rollback information are
     *
     * @param dataRecorder
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public void preparedXA(IXADataRecorder dataRecorder) throws IOException {
        IDataRecord message = dataRecorder.createDataRecord(XALogRecordType.XA_PREPARED, EMPTY_DATA);
    }


    /**
     * Indicates that the XAResource has been prepared and enters the 'committing' state
     * <p/>
     * All information to perform a complete roll forward during commit are logged
     *
     * @param dataRecorder
     * @param data
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public void committingXA(IXADataRecorder dataRecorder, byte[][] data) throws IOException {
        IDataRecord message = dataRecorder.createDataRecord(XALogRecordType.ROLLFORWARD_DATA, data);
    }


    /**
     * indicates the start of a TX,
     * <p/>
     * To recover this resource in the context of its XID, both the XID and the id of the resource have to be logged
     *
     * @param dataRecorder
     * @param resourceId
     * @param xid
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public void startXA(IXADataRecorder dataRecorder, String resourceId, byte[] xid) throws IOException {
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

            IDataRecord message = dataRecorder.createDataRecord(XALogRecordType.XA_START, new byte[][]{xid});


        } finally {
            if (outputIO != null) outputIO.close();
        }

    }

    /**
     * indicated the end of the TX
     *
     * @param dataRecorder
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public void doneXA(IXADataRecorder dataRecorder) throws IOException {

        IDataRecord message = dataRecorder.createDataRecord(XALogRecordType.XA_DONE, new byte[][]{});
    }


    public synchronized void open() throws IOException, InterruptedException {
        if (this.dataLoggerFactory == null) {
            throw new IllegalStateException("No logger set");
        }
        // xaDataRecorders.clear();
        fireXARecorderResourceOpened();
    }

    @Override
    public synchronized void close() {
        if (!isClosed()) {
            SortedMap<Long, PhynixxXADataRecorder> tmp= new TreeMap<Long, PhynixxXADataRecorder>(xaDataRecorders);
            for (PhynixxXADataRecorder dataRecorder : tmp.values()) {
                dataRecorder.close();
            }
            xaDataRecorders.clear();
            fireXARecorderResourceClosed();
        }
    }

    @Override
    public synchronized void destroy() throws IOException, InterruptedException {
        this.close();
        this.dataLoggerFactory.cleanup();
        this.listeners = new ArrayList();
        this.messageSeqGenerator = null;
    }

    /**
     * recovers all dataRecorder of the loggerSystem. All reopen dataRecorders
     * are closed and all dataRecorder that can be recovered are opened for reading
     *
     * @see #getXADataRecorders()
     */
    @Override
    public void recover() {

        try {

            // close all reopen dataRecorders
            this.close();

            Set<String> loggerNames = this.dataLoggerFactory.findLoggerNames();

            // recover all logs
            for (String loggerName : loggerNames) {

                IDataLogger dataLogger = this.dataLoggerFactory.instanciateLogger(loggerName);
                XADataLogger xaLogger = new XADataLogger(dataLogger);

                PhynixxXADataRecorder phynixxXADataRecorder = PhynixxXADataRecorder.recoverDataRecorder(xaLogger, this);
                this.addXADataRecorder(phynixxXADataRecorder);

            }
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }


    }


    private void addXADataRecorder(PhynixxXADataRecorder xaDataRecorder) {
        if (!this.xaDataRecorders.containsKey(xaDataRecorder.getXADataRecorderId())) {
            this.xaDataRecorders.put(xaDataRecorder.getXADataRecorderId(), xaDataRecorder);
        }
    }

    @Override
    public Set<IXADataRecorder> getXADataRecorders() {
        Set<IXADataRecorder> seqs = new HashSet<IXADataRecorder>(this.xaDataRecorders.size());
        for (Iterator<PhynixxXADataRecorder> iterator = xaDataRecorders.values().iterator(); iterator.hasNext(); ) {
            seqs.add(iterator.next());
        }
        return seqs;
    }


    @Override
    public void recorderDataRecorderClosed(IXADataRecorder xaDataRecorder) {
        this.removeXADataRecoder(xaDataRecorder);
    }

    @Override
    public void recorderDataRecorderOpened(IXADataRecorder xaDataRecorder) {

    }

    private void removeXADataRecoder(IXADataRecorder xaDataRecorder) {
        this.xaDataRecorders.remove(xaDataRecorder.getXADataRecorderId());
    }

    private void fireEvents(IEventDeliver deliver) {

        if (this.listeners == null || this.listeners.size() == 0) {
            return;
        }

        // copy all listeners as the callback may change the list of listeners ...
        List<IXARecorderResourceListener> tmp = new ArrayList<IXARecorderResourceListener>(this.listeners);
        for (int i = 0; i < tmp.size(); i++) {
            IXARecorderResourceListener listener = tmp.get(i);
            deliver.fireEvent(listener);
        }
    }


    protected void fireXARecorderResourceClosed() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IXARecorderResourceListener listener) {
                listener.recorderResourceClosed(PhynixxXARecorderRepository.this);
            }
        };
        fireEvents(deliver);
    }

    protected void fireXARecorderResourceOpened() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IXARecorderResourceListener listener) {
                listener.recorderResourceOpened(PhynixxXARecorderRepository.this);
            }
        };
        fireEvents(deliver);
    }


}
