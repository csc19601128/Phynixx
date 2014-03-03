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
import org.csc.phynixx.common.exceptions.ExceptionUtils;
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
abstract class PhynixxManagedConnectionGuard<C extends IPhynixxConnection> implements IPhynixxManagedConnection<C>, IXADataRecorderAware, ICloseable , IAutoCommitAware{

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(PhynixxManagedConnectionGuard.class);

    private C connection = null;

    private Class<C> connectionInterface;

    // indicates, that the core connection is transactionalData
    private volatile boolean transactionalData = false;

    private volatile boolean synchronizedConnection=true;

    private volatile boolean closed = false;

    private CloseStrategy closeStrategy;

    final Long id;

    private volatile Long boundThreadId =null;

    protected PhynixxManagedConnectionGuard(long id, Class<C> connectionInterface, C connection, CloseStrategy<C> closeStrategy) {
        this.id = id;
        this.connectionInterface = connectionInterface;
        this.setConnection(connection);
        this.closeStrategy=closeStrategy;
        this.bindToCurrentThread();
    }

    private void bindToCurrentThread() {
        this.boundThreadId= Thread.currentThread().getId();
    }
    
    private void releaseThreadBinding() {
        this.boundThreadId=null; 
    }
    
    private void checkThreadBinding() {
        long currentThreadBinding = Thread.currentThread().getId();
        if( this.boundThreadId!=null && currentThreadBinding!=this.boundThreadId) {
            throw new IllegalStateException("Connection is bound to Thread "+this.boundThreadId+" but called by Thread "+ currentThreadBinding);
        }
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
    public void setSynchronized(boolean state) {
        this.synchronizedConnection=state;
    }

    @Override
    public boolean isSynchronized() {
        return this.synchronizedConnection;
    }

    @Override
    public long getManagedConnectionId() {
        return id;
    }

    public String toString() {
        if (hasCoreConnection()) {
            return this.getCoreConnection().toString();
        }
        return "no core connection";
    }

    @Override
    public boolean hasTransactionalData() {
        return transactionalData;
    }

    public boolean isClosed() {
        return closed;
    }


    void setClosed(boolean closed) {
        this.closed = closed;
        this.transactionalData=false;
    }

    @Override
    public void reopen() {

        if(connection==null) {
            throw new IllegalStateException("Connection is already set free");
        }
        if(this.hasTransactionalData()) {
            LOG.warn("Connection " + this + " has tranactional data and has to be closed safely");
        }
        this.setClosed(false);
        this.reset();
        this.bindToCurrentThread();
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
                (this.connection != null && this.connection.equals(con))  ) {
            return;
        }
        this.connection = con;
    }


    public IXADataRecorder getXADataRecorder() {
        if (hasCoreConnection() && ImplementorUtils.isImplementationOf(getCoreConnection(), IXADataRecorderAware.class)) {
            this.checkThreadBinding();
            return ImplementorUtils.cast(getCoreConnection(), IXADataRecorderAware.class).getXADataRecorder();
        }
        return null;

    }

    @Override
    public IDataRecordReplay recoverReplayListener() {
        if (hasCoreConnection() && ImplementorUtils.isImplementationOf(getCoreConnection(), IXADataRecorderAware.class)) {
            return ImplementorUtils.cast(getCoreConnection(), IXADataRecorderAware.class).recoverReplayListener();
        }
        return null;
    }

    public void setXADataRecorder(IXADataRecorder dataRecorder) {
        if (hasCoreConnection() && ImplementorUtils.isImplementationOf(getCoreConnection(), IXADataRecorderAware.class)) {
            ImplementorUtils.cast(getCoreConnection(), IXADataRecorderAware.class).setXADataRecorder(dataRecorder);
        }
    }

    @Override
    public C getCoreConnection() {
        if( this.connection==null) {
            throw new IllegalStateException("Connection is already set free and is invalid");
        }
        return this.connection;
    }

    /**
     * The implementation delegates to {@link org.csc.phynixx.connection.CloseStrategy}.* the real action is determined by the implementation of {@link org.csc.phynixx.connection.CloseStrategy}.
     */
    @Override
    public void close() {
        if (!this.isClosed() && hasCoreConnection()) {
            this.closeStrategy.close(this);
        }
    }

    /**
     * marks a connection a freed. This connection won't be used any more.
     *
     * The connection is released from the bound thread an can be bind to an other thread
     *
     * The thread binding is not checked as this method is called in emergency and has to be robust
     */
    @Override
    public void free() {
        try {
        if(hasCoreConnection()) {
            this.getCoreConnection().close();
        }
        this.fireConnectionFreed();
        this.boundThreadId= Thread.currentThread().getId();

    } finally {
        this.setClosed(true);
        // state may be important for State-Listener, so its set after the listener did their work
        setTransactionalData(false);

        // re-opem is not possible
        // this.connection=null;
        this.releaseThreadBinding();
    }

    }

    @Override
    public boolean hasCoreConnection() {
        return this.connection!=null;
    }


    /**
     * Implementation of releasing the connection from transactional context
     */
    public void release() {
        try {
            if(hasCoreConnection()) {
                checkThreadBinding();
                this.setClosed(true);
                this.getCoreConnection().reset();
                this.fireConnectionReleased();

            }
        } finally {
            this.setClosed(true);

            // state may be important for Stat-Listener, so its set after the listener did their work
            setTransactionalData(false);

            this.releaseThreadBinding();
        }
    }


    public boolean isAutoCommit() {
        if (hasCoreConnection()) {
            checkThreadBinding();
            if(ImplementorUtils.isImplementationOf(this.getCoreConnection(), IAutoCommitAware.class)) {
                return ImplementorUtils.cast(this.getCoreConnection(),IAutoCommitAware.class).isAutoCommit();
            }
        }
        return false;

    }

    public void setAutoCommit(boolean autoCommit) {
        if (hasCoreConnection()) {
            checkThreadBinding();
            if(ImplementorUtils.isImplementationOf(this.getCoreConnection(), IAutoCommitAware.class)) {
                ImplementorUtils.cast(this.getCoreConnection(),IAutoCommitAware.class).setAutoCommit(autoCommit);
            }
        }
    }


    /**
     * A xaresource's connection is  never closed but always dereferenced .
     * As a connection proxy shields a xa resource, the current connection is not closed but dereferenced (==released)
     */
    public void reset() {
        if (hasCoreConnection()) {

            checkThreadBinding();
            this.getCoreConnection().reset();
            setTransactionalData(false);
            // notify the action
            this.fireConnectionReset();
        }
    }

    public void commit() {

        if(!hasTransactionalData()) {
            return;
        }
        fireConnectionCommitting();
        if (hasCoreConnection()) {
            checkThreadBinding();
            this.getCoreConnection().commit();
            setTransactionalData(false);
        }
        this.fireConnectionCommitted();

    }


    public void prepare() {

        if(!hasTransactionalData()) {
            return;
        }

        this.fireConnectionPreparing();
        if (hasCoreConnection()) {
            checkThreadBinding();
            this.getCoreConnection().prepare();
        }
        this.fireConnectionPrepared();
    }


    public void rollback() {

        if(!hasTransactionalData()) {
            return;
        }

        fireConnectionRollingBack();
        if (hasCoreConnection()) {
            checkThreadBinding();
            this.getCoreConnection().rollback();
            setTransactionalData(false);

        }
        this.fireConnectionRolledback();
    }

    private void setTransactionalData(boolean state) {
        this.transactionalData=state;
    }


    /**
     * recovers the data of the dataLogger and provides the recovered data to the connection via the replaylistener
     */
    @Override
    public void recover() {

        // not revoverable
        if (this.getCoreConnection() == null || !ImplementorUtils.isImplementationOf(getCoreConnection(), IXADataRecorderAware.class)) {
            return;
        }

        IXADataRecorderAware con = ImplementorUtils.cast(getCoreConnection(), IXADataRecorderAware.class);

        // the connection has to re establish the state of the message LOG
        IXADataRecorder msgLogger = this.getXADataRecorder();
        if (msgLogger.isEmpty()) {
            return;
        }
        this.fireConnectionRecovering();
        IDataRecordReplay dataRecordReplay = con.recoverReplayListener();

        // msgLogger.recover();
        msgLogger.replayRecords(dataRecordReplay);

        this.fireConnectionRecovered();

    }


    private void fireConnectionReset() {

        IEventDeliver<C> deliver = new IEventDeliver<C>() {
            public void fireEvent(IPhynixxManagedConnectionListener<C> listener, IManagedConnectionProxyEvent<C> event) {
                listener.connectionReset(event);
            }

            public String toString() {
                return "connectionRest";
            }
        };
        fireEvents(deliver);
    }


    interface IEventDeliver<X extends IPhynixxConnection> {
        void fireEvent(IPhynixxManagedConnectionListener<X> listener, IManagedConnectionProxyEvent<X> event);
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

    private void fireEvents(IEventDeliver<C> deliver) {
        fireEvents(deliver, null);
    }

    private void fireEvents(IEventDeliver<C> deliver, Exception exception) {
        // copy all listeners as the callback may change the list of listeners ...
        List<IPhynixxManagedConnectionListener<C>> tmp = new ArrayList(this.listeners);
        PhynixxConnectionProxyEvent<C> event = new PhynixxConnectionProxyEvent<C>(getObservableProxy(), exception);
        for (int i = 0; i < tmp.size(); i++) {
            IPhynixxManagedConnectionListener<C> listener = tmp.get(i);
            if (LOG.isDebugEnabled()) {
                LOG.debug("ConnectionPhynixxGuard " + event + " called listener " + listener + " on " + deliver);
            }
            deliver.fireEvent(listener, event);
        }
    }


    protected void fireConnectionReleased() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionReleased(event);
            }

            public String toString() {
                return "connectionReleased";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionErrorOccurred(final Exception exception) {
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

        this.setTransactionalData(true);
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
        IEventDeliver deliver = new IEventDeliver<C>() {
            public void fireEvent(IPhynixxManagedConnectionListener<C> listener, IManagedConnectionProxyEvent<C> event) {
                listener.connectionCommitted(event);
            }

            public String toString() {
                return "connectionCommitted";
            }
        };
        fireEvents(deliver);
    }


    protected void fireConnectionFreed() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxManagedConnectionListener listener, IManagedConnectionProxyEvent event) {
                listener.connectionFreed(event);
            }

            public String toString() {
                return "connectionFreed";
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
