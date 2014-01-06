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


import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.messages.IRecordLogger;

import java.util.ArrayList;
import java.util.List;


public abstract class PhynixxConnectionProxyAdapter implements IPhynixxConnectionProxy, IRecordLoggerAware {

    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());

    public abstract class ExecutionTemplate {
        public Object run() throws Exception {
            try {
                PhynixxConnectionProxyAdapter.this.prepareAction();
                Object obj = this.call();
                PhynixxConnectionProxyAdapter.this.finishAction();
                return obj;
            } catch (Exception e) {
                PhynixxConnectionProxyAdapter.this.finishAction(e);
                throw e;
            }

        }

        protected abstract Object call() throws Exception;
    }

    private IPhynixxConnection connection = null;

    // indicates, that the core connection is executing
    private volatile boolean executing = false;

    // indicates that the connection is expired
    private volatile boolean expired = false;


    /**
     * If this proxy is implemented by a DynProxy. If a referene to this object has to be propagated
     * ({@link #fireEvents(org.csc.phynixx.connection.PhynixxConnectionProxyAdapter.IEventDeliver)}, it would leads to invalid references
     * if <code>this</code> is returned but not the implementing DynaProxy.
     * <p/>
     * See the implementation of a java proxy.
     *
     * @return he object via <code>this</code> is accessible
     * @see DynaProxyFactory.ConnectionProxy
     */
    protected IPhynixxConnectionProxy getObservableProxy() {
        return this;
    }

    public synchronized void setConnection(IPhynixxConnection con) {
        if ((this.connection == null && con == null) ||
                (this.connection != null && this.connection.equals(con))
                ) {
            return;
        }

        if (this.connection != null) {
            this.fireConnectionDereferenced();
        }
        this.connection = con;

        if (this.connection != null) {
            this.fireConnectionReferenced();
        }
    }


    public IRecordLogger getRecordLogger() {
        if (this.getConnection() != null && this.getConnection() instanceof IRecordLoggerAware) {
            return ((IRecordLoggerAware) getConnection()).getRecordLogger();
        }
        return null;

    }

    public void setRecordLogger(IRecordLogger messageLogger) {
        if (this.getConnection() != null && this.getConnection() instanceof IRecordLoggerAware) {
            ((IRecordLoggerAware) getConnection()).setRecordLogger(messageLogger);
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


    public synchronized IPhynixxConnection getConnection() {
        return this.connection;
    }

    /**
     * A xaresource's connection is  never closed but always dereferenced .
     * As a connection proxy shields a xa resource, the current connection is not closed but dereferenced (==released)
     */
    public synchronized void close() {
        if (this.getConnection() != null) {
            // notify the action
            this.fireConnectionClosed();
        }
    }

    public synchronized void commit() {
        if (this.getConnection() != null) {
            this.getConnection().commit();
        }
        this.fireConnectionCommitted();

    }

    public synchronized boolean isClosed() {
        if (this.getConnection() != null) {
            return this.getConnection().isClosed();
        }
        return true;
    }


    public synchronized void prepare() {
        this.fireConnectionPreparing();
        if (this.getConnection() != null) {
            this.getConnection().prepare();
        }
        this.fireConnectionPrepared();
    }

    public synchronized void rollback() {

        if (this.getConnection() != null) {
            this.getConnection().rollback();
        }
        this.fireConnectionRolledback();
    }


    public void recover() {
        // the connection has to re establish the state of the message logger
        IRecordLogger msgLogger = this.getRecordLogger();
        if (msgLogger.isCompleted()) {
            return;
        }

        IPhynixxConnection con = this.getConnection();
        if (con == null) {
            return;
        }

        this.fireConnectionRecovering();
        con.recover();

        if (msgLogger.isCommitting()) {
            con.commit();
        } else {
            con.rollback();
        }

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


    interface IEventDeliver {
        void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event);
    }

    private List listeners = new ArrayList();

    public synchronized void addConnectionListener(IPhynixxConnectionProxyListener listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public synchronized void removeConnectionListener(IPhynixxConnectionProxyListener listener) {

        this.listeners.remove(listener);
    }

    private void fireEvents(IEventDeliver deliver) {
        fireEvents(deliver, null);
    }

    private void fireEvents(IEventDeliver deliver, Exception exception) {
        // copy all listeners as the callback may change the list of listeners ...
        List tmp = new ArrayList(this.listeners);
        PhynixxConnectionProxyEvent event = new PhynixxConnectionProxyEvent(getObservableProxy(), exception);
        for (int i = 0; i < tmp.size(); i++) {
            IPhynixxConnectionProxyListener listener = (IPhynixxConnectionProxyListener) tmp.get(i);
            if (logger.isDebugEnabled()) {
                logger.debug("ConnectionProxy " + event + " called listener " + listener + " on " + deliver);
            }
            deliver.fireEvent(listener, event);
        }
    }


    protected void fireConnectionClosed() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
                listener.connectionClosed(event);
            }

            public String toString() {
                return "connectionClosed";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionErrorOccurred(final Exception exception) {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
                listener.connectionErrorOccurred(event);
            }

            public String toString() {
                return "connectionErrorOccurred";
            }
        };
        fireEvents(deliver, exception);
    }

    protected void fireConnectionRequiresTransaction() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
                listener.connectionRequiresTransaction(event);
            }

            public String toString() {
                return "connectionRequiresTransaction";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionRolledback() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
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
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
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
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
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
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
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
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
                listener.connectionCommitted(event);
            }

            public String toString() {
                return "connectionCommitted";
            }
        };
        fireEvents(deliver);
    }


    protected void fireConnectionDereferenced() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
                listener.connectionDereferenced(event);
            }

            public String toString() {
                return "connectionDereferenced";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionReferenced() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
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
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
                listener.connectionRecovering(event);
            }

            public String toString() {
                return "connectionRecovering";
            }
        };
        fireEvents(deliver);
    }

    protected void fireConnectionRecovered() {
        IEventDeliver deliver = new IEventDeliver() {
            public void fireEvent(IPhynixxConnectionProxyListener listener, IPhynixxConnectionProxyEvent event) {
                listener.connectionRecovered(event);
            }

            public String toString() {
                return "connectionRecovered";
            }
        };
        fireEvents(deliver);
    }


    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PhynixxConnectionProxyAdapter)) {
            return false;
        }

        PhynixxConnectionProxyAdapter another = (PhynixxConnectionProxyAdapter) obj;
        if (another.getConnection() == null && getConnection() == null) {
            return true;
        }
        if (another.getConnection() == null || getConnection() == null) {
            return false;
        }

        return another.getConnection().equals(getConnection());


    }

    public int hashCode() {
        if (this.getConnection() != null) {
            return this.getConnection().hashCode();
        }
        return 0;
    }

    public String toString() {
        if (this.getConnection() != null) {
            return this.getConnection().toString();
        }
        return "no core connection";
    }


}
