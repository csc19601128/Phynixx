package org.csc.phynixx.loggersystem.logrecord;

/*
 * #%L
 * phynixx-logger
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
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
import java.util.Set;

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
public interface IXARecorderRepository extends IXADataRecorderLifecycleListener {


    /**

     void open() throws IOException, InterruptedException;

     String getLoggerSystemName();

     void logUserData(IXADataRecorder dataRecorder, byte[][] data) throws InterruptedException, IOException;

     void logUserData(IXADataRecorder dataRecorder, byte[] data) throws InterruptedException, IOException;

     void preparedXA(IXADataRecorder dataRecorder) throws IOException;

     void committingXA(IXADataRecorder dataRecorder, byte[][] data) throws InterruptedException, IOException;

     void startXA(IXADataRecorder dataRecorder, String resourceId, byte[] xid) throws IOException, InterruptedException;

     void doneXA(IXADataRecorder dataRecorder) throws IOException;

     */

    boolean isClosed();

    /**
     * cerets a brand new recorder. This recorder is managed by the repository
     * @return
     * @throws IOException
     */
    IXADataRecorder createXADataRecorder() throws IOException;

    /**
     * closes all open recorder.
     * The recorder are {@link IXADataRecorder#close()} and removed from the repository.
     *
     * Depending on recording closed recorder could recovered if it contains relevant information
     * and it is not destroyed but can be re-established by {@link #recover}
     */
    void close();

    /**
     * destroys all open recorder.
     * The recorder are {@link org.csc.phynixx.loggersystem.logrecord.IXADataRecorder#destroy()} and removed from the repository.
     */
    void destroy() throws IOException, InterruptedException;

    /**
     * tries to re-establish all recorders. The logger system is closed.
     * All remaining (already closed) recorder are reopen for read
     * @see #getXADataRecorders()
     * */
    void recover();


    /**
     *
     * @return all currently open loggers.
     */
    Set<IXADataRecorder> getXADataRecorders();
}
