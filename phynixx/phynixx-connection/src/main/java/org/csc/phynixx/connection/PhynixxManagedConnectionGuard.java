package org.csc.phynixx.connection;

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


import org.csc.phynixx.common.cast.ImplementorUtils;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

import java.util.ArrayList;
import java.util.List;


/**
 * decorates a connection an takes care of calling the listener during the different phases of a transaction
 * <p/>
 * <pre>
 *
 *     Event
 *
 *     requiresTransaction - the connection is to be changed in a transaction an needs needs support for managing transactional data
 *
 *     rollingBack - roll back starts
 *     rolledBack  - rollback is finished
 *     preparing  - prepare starts
 *     prepared   - prepare is finished
 *     committing  - commit starts
 *     committed   - commit is finished
 *
 *     connection reference
 *     connection dereferenced
 *
 *     connection closed
 *
 *
 * </pre>
 *
 * @param <C> Typ of the connection
 */


/**
 * guards all calls to methods of {@link org.csc.phynixx.connection.IPhynixxConnection} and ensures that the correct events are deliverd to the listeners.
 * This class has the character of an abstract class and should no be instanciated.
 *
 * @param <C>
 */
abstract class PhynixxManagedConnectionGuard<C extends IPhynixxConnection> implements IPhynixxManagedConnection<C>, IXADataRecorderAware {

    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());

    private C connection = null;

    private Class<C> connectionInterface;

    // indicates, that the core connection is executing
    private volatile boolean executing = false;

    // indicates that the connection is expired
    private volatile boolean expired = false;

    final Long id;

    protected PhynixxManagedConnectionGuard(long id, Class<C> connectionInterface, C connection) {
        this.id = id;
        this.connectionInterface = connectionInterface;
        this.setConnection(connection);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof IPhynixxManagedConnection)) return false;

        IPhynixxManagedConnection that = (IPhynixxManagedConnection) o;
        return that.getManagedConnectionId() == this.getManagedConnectionId();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public long getManagedConnectionId() {
        return id;
    }

    public String toString() {
        if (this.getCoreConnection() != null) {
            return this.getCoreConnection().toString();
        }
        return "no core connection";
    }


    /**
     * If this proxy is implemented by a DynProxy. If a referene to this object has to be propagated
     * ({@link #fireEvents(PhynixxManagedConnectionGuard.IEventDeliver)}, it would leads to invalid references
     * if <code>this</code> is returned but not the implementing DynaProxy.
     * <p/>
     * See the implementation of a java proxy.
     *
     * @return he object via <code>this</code> is accessible
     * @see org.csc.phynixx.connection.DynaPhynixxManagedConnectionFactory.ConnectionPhynixxGuard
     */
    abstract protected IPhynixxManagedConnection<C> getObservableProxy();

    @Override
    public C toConnection() {
        return ImplementorUtils.cast(this.getObservableProxy(), this.connectionInterface);
    }

    private void setConnection(C con) {
        if ((this.connection == null && con == null) ||
                (this.connection != null && this.connection.equals(con))
                ) {
            return;
        }
        this.connection = con;
    }


    public IXADataRecorder getXADataRecorder() {
        if (this.getCoreConnection() != null && ImplementorUtils.isImplementationOf(getCoreConnection(), IXADataRecorderAware.class)) {
            return ImplementorUtils.cast(getCoreConnection(), IXADataRecorderAware.class).getXADataRecorder();
        }
        return null;

    }

    @Override
    public IDataRecordReplay recoverReplayListener() {
        if (this.getCoreConnection() != null && ImplementorUtils.isImplementationOf(getCoreConnection(), IXADataRecorderAware.class)) {
            return ImplementorUtils.cast(getCoreConnection(), IXADataRecorderAware.class).recoverReplayListener();
        }
        return null;
    }

    public void setXADataRecorder(IXADataRecorder dataRecorder) {
        if (this.getCoreConnection() != null && ImplementorUtils.isImplementationOf(getCoreConnection(), IXADataRecorderAware.class)) {
            ImplementorUtils.cast(getCoreConnection(), IXADataRecorderAware.class).setXADataRecorder(dataRecorder);
        }
    }


    public boolean isExpired() {
        return expired;
    }


    public void setExpired(boolean expired) {
        this.expired = expired;
    }


    public boolean isExecuting() {
        return executing;
    }


    private void setExecuting(boolean executing) {
        this.executing = executing;
    }


    @Override
    public C getCoreConnection() {
        return this.connection;
    }

    /**
     * A xaresource's connection is  never closed but always dereferenced .
     * As a connection proxy shields a xa resource, the current connection is not closed but dereferenced (==released)
     */
    public void close() {
        if (this.getCoreConnection() != null) {
            this.getCoreConnection().close();
            // notify the action
            this.fireConnectionClosed();
        }
    }

    public boolean isAutoCommit() {
        if (this.getCoreConnection() != null) {
            return this.getCoreConnection().isAutoCommit();
        }
        return false;
    }

    public void setAutoCommit(boolean autoCommit) {
        if (this.getCoreConnection() != null) {
            this.getCoreConnection().setAutoCommit(autoCommit);
        }
    }


    /**
     * A xaresource's connection is  never closed but always dereferenced .
     * As a connection proxy shields a xa resource, the current connection is not closed but dereferenced (==released)
     */
    public void reset() {
        if (this.getCoreConnection() != null) {
            this.getCoreConnection().reset();
            // notify the action
            this.fireConnectionReset();
        }
    }

    public synchronized void commit() {
        fireConnectionCommitting();
        if (this.getCoreConnection() != null) {
            this.getCoreConnection().commit();
        }
        this.fireConnectionCommitted();

    }


    public boolean isClosed() {
        if (this.getCoreConnection() != null) {
            return this.getCoreConnection().isClosed();
        }
        return true;
    }

    public void prepare() {
        this.fireConnectionPreparing();
        if (this.getCoreConnection() != null) {
            this.getCoreConnection().prepare();
        }
        this.fireConnectionPrepared();
    }


    public void rollback() {
        fireConnectionRollingBack();
        if (this.getCoreConnection() != null) {
            this.getCoreConnection().rollback();
        }
        this.fireConnectionRolledback();
    }


    /**
     * recovers the data of the dataLogger and provodes the recoverd data to the connection via the replaylistener
     */
    @Override
    public void recover() {

        // not revoverable
        if (this.getCoreConnection() == null || !ImplementorUtils.isImplementationOf(getCoreConnection(), IXADataRecorderAware.class)) {
            return;
        }

        IXADataRecorderAware con = ImplementorUtils.cast(getCoreConnection(), IXADataRecorderAware.class);

        // the connection has to re establish the state of the message logger
        IXADataRecorder msgLogger = this.getXADataRecorder();
        if (msgLogger.isCompleted()) {
            return;
        }
        this.fireConnectionRecovering();
        IDataRecordReplay dataRecordReplay = con.recoverReplayListener();

        msgLogger.recover();
        msgLogger.replayRecords(dataRecordReplay);

        this.fireConnectionRecovered();

    }


    /**
     * checks, if the current proxy is prepared to execute
     * and modifies the state .
     */
    protected void prepareAction() {
        this.setExecuting(true);


        this.fireConnectionRequiresTransaction();

        if (this.isExpired()) {
            throw new IllegalStateException("Connection is expired");
        }
    }

    protected void finishAction() {
        this.setExecuting(false);
        if (this.isExpired()) {
            throw new IllegalStateException("Connection is expired");
        }
    }


    protected void finishAction(Exception exception) {
        this.setExecuting(false);
        this.fireConnectionErrorOccurred(exception);
    }

    private void fireConnectionReset() {

        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionReset(event);
            }

            public String toString() {
                return "connectionRest";
            }
        };
        fireEvents(deliver);
    }


    interface IEventDeliver {
        void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event);
    }

    private List<IPhynixxManagedConnectionListener<C>> listeners = new ArrayList<IPhynixxManagedConnectionListener<C>>();

    public void addConnectionListener(IPhynixxManagedConnectionListener<C> listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public synchronized void removeConnectionListener(IPhynixxManagedConnectionListener<C> listener) {

        this.listeners.remove(listener);
    }

    private void fireEvents(IEventDeliver deliver) {
        fireEvents(deliver, null);
    }

    private void fireEvents(IEventDeliver deliver, Exception exception) {
        // copy all listeners as the callback may change the list of listeners ...
        List<IPhynixxManagedConnectionListener<C>> tmp = new ArrayList(this.listeners);
        PhynixxConnectionProxyEvent event = new PhynixxConnectionProxyEvent(getObservableProxy(), exception);
        for (int i = 0; i < tmp.size(); i++) {
            IPhynixxManagedConnectionListener<C> listener = tmp.get(i);
            if (logger.isDebugEnabled()) {
                logger.debug("ConnectionPhynixxGuard " + event + " called listener " + listener + " on " + deliver);
            }
            deliver.fireEvent(listener, event);
        }
    }


    protected void fireConnectionClosed() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionClosed(event);
            }

            public String toString() {
                return "connectionClosed";
            }
        };
        fireEvents(deliver);
    }

    @Override
    public void fireConnectionErrorOccurred(final Exception exception) {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionErrorOccurred(event);
            }

            public String toString() {
                return "connectionErrorOccurred";
            }
        };
        fireEvents(deliver, exception);
    }

    protected void fireConnectionRequiresTransactionFinished() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionRequiresTransaction(event);
            }

            public String toString() {
                return "connectionRequiresTransaction";
            }
        };
        fireEvents(deliver);
    }


    protected void fireConnectionRequiresTransaction() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionRequiresTransaction(event);
            }

            public String toString() {
                return "connectionRequiresTransaction";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionRequiresTransactionExecuted(Exception exception) {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionRequiresTransactionExecuted(event);
            }

            public String toString() {
                return "connectionRequiresTransactionExecuted";
            }
        };
        fireEvents(deliver, exception);
    }

    protected void fireConnectionRequiresTransactionExecuted() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionRequiresTransactionExecuted(event);
            }

            public String toString() {
                return "connectionRequiresTransactionExecuted";
            }
        };
        fireEvents(deliver);
    }


    protected void fireConnectionRolledback() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionRolledback(event);
            }

            public String toString() {
                return "connectionRolledback";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionPreparing() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionPreparing(event);
            }

            public String toString() {
                return "connectionPreparing";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionPrepared() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionPrepared(event);
            }

            public String toString() {
                return "connectionPrepared";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionCommitting() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionCommitting(event);
            }

            public String toString() {
                return "connectionCommitting";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionCommitted() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionCommitted(event);
            }

            public String toString() {
                return "connectionCommitted";
            }
        };
        fireEvents(deliver);
    }


    @Override
    public void fireConnectionDereferenced() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionDereferenced(event);
            }

            public String toString() {
                return "connectionDereferenced";
            }
        };
        fireEvents(deliver);
    }

    @Override
    public void fireConnectionReferenced() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionReferenced(event);
            }

            public String toString() {
                return "connectionReferenced";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionRecovering() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionRecovering(event);
            }

            public String toString() {
                return "connectionRecovering";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionRollingBack() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionRollingBack(event);
            }

            public String toString() {
                return "connectionRollingBack";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionRecovered() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionRecovered(event);
            }

            public String toString() {
                return "connectionRecovered";
            }
        };
        fireEvents(deliver);
    }


}
