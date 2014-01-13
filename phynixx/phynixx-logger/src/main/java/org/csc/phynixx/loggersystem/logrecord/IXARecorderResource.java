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
public interface IXARecorderResource extends IXADataRecorderLifecycleListener {


    boolean isClosed();

    IXADataRecorder createXADataRecorder() throws IOException;

    String getLoggerSystemName();

    void logUserData(IXADataRecorder dataRecorder, byte[][] data) throws InterruptedException, IOException;

    void logUserData(IXADataRecorder dataRecorder, byte[] data) throws InterruptedException, IOException;

    void preparedXA(IXADataRecorder dataRecorder) throws IOException;

    void committingXA(IXADataRecorder dataRecorder, byte[][] data) throws InterruptedException, IOException;

    void startXA(IXADataRecorder dataRecorder, String resourceId, byte[] xid) throws IOException, InterruptedException;

    void doneXA(IXADataRecorder dataRecorder) throws IOException;

    void open() throws IOException, InterruptedException;

    void close();

    void destroy() throws IOException, InterruptedException;

    void recover();

    /**
     *
     *
     */
    Set<IXADataRecorder> getXADataRecorders();
}
