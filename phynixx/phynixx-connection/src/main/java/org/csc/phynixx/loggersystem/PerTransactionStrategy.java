package org.csc.phynixx.loggersystem;

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


import org.csc.phynixx.connection.*;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.loggersystem.messages.ILogRecordReplay;
import org.csc.phynixx.loggersystem.messages.ILogRecordSequence;
import org.csc.phynixx.loggersystem.messages.IManagedRecordLogger;
import org.csc.phynixx.loggersystem.messages.IRecordLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class PerTransactionStrategy extends PhynixxConnectionProxyListenerAdapter implements ILoggerSystemStrategy, IPhynixxConnectionProxyListener {

    private LoggerSystem loggerSystem = null;

    private class MessageLoggerWrapper implements IManagedRecordLogger {
        private XAResourceLogger xaresourceLogger = null;
        private ILogRecordSequence msgSeq = null;

        public MessageLoggerWrapper(XAResourceLogger xaresourceLogger,
                                    ILogRecordSequence msgSeq) {
            super();
            this.xaresourceLogger = xaresourceLogger;
            this.msgSeq = msgSeq;
        }

        public XAResourceLogger getXAResourceLogger() {
            return xaresourceLogger;
        }

        public ILogRecordSequence getMsgSeq() {
            return msgSeq;
        }

        public void commitRollforwardData(byte[] data) {
            this.msgSeq.commitRollforwardData(data);
        }

        public void commitRollforwardData(byte[][] data) {
            this.msgSeq.commitRollforwardData(data);
        }

        public boolean isCommitting() {
            return this.msgSeq.isCommitting();
        }

        public boolean isCompleted() {
            return this.msgSeq.isCompleted();
        }

        public boolean isPrepared() {
            return this.msgSeq.isPrepared();
        }

        public void replayRecords(ILogRecordReplay replay) {
            this.msgSeq.replayRecords(replay);

        }

        public void writeRollbackData(byte[] data) {
            this.msgSeq.writeRollbackData(data);

        }

        public void writeRollbackData(byte[][] data) {
            this.msgSeq.writeRollbackData(data);

        }

        /**
         * releases the connections
         * the log files are nortt destroyed as they could be part of an incomplete transaction
         * To complete a transaction call @link {@link #destroy()}
         */
        public void close() {
            synchronized (xaresourceLogger) {
                if (!xaresourceLogger.isClosed()) {
                    try {
                        xaresourceLogger.close();
                    } catch (Exception e) {
                        throw new DelegatedRuntimeException("closing " + xaresourceLogger, e);
                    }
                }
            }
        }

        /**
         * releases the connection an destroys the logfiles
         */
        public void destroy() {
            synchronized (xaresourceLogger) {
                try {
                    xaresourceLogger.destroy();
                } catch (Exception e) {
                    throw new DelegatedRuntimeException("closing " + xaresourceLogger, e);
                }
                PerTransactionStrategy.this.loggerSystem.destroy(xaresourceLogger);
            }
        }


    }

    /**
     * the logger is added to all instanciated Loggers
     */
    public void addLoggerListener(ILoggerListener listener) {
        this.loggerSystem.addListener(listener);

    }


    public PerTransactionStrategy(String loggersystemName, ILoggerFactory loggerFactory) throws Exception {
        this.loggerSystem = new LoggerSystem(loggersystemName, loggerFactory);
    }


    public IPhynixxConnectionProxy decorate(IPhynixxConnectionProxy connectionProxy) {
        connectionProxy.addConnectionListener(this);
        return connectionProxy;
    }

    public void close() {
        this.loggerSystem.close();
    }


    public void connectionRecovering(IPhynixxConnectionProxyEvent event) {
        this.connectionRequiresTransaction(event);

    }


    public void connectionClosed(IPhynixxConnectionProxyEvent event) {

        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        if (con == null || !(con instanceof IRecordLoggerAware)) {
            return;
        }

        IRecordLoggerAware messageAwareConnection = (IRecordLoggerAware) con;
        // Transaction is closed and the logger is destroyed ...
        IRecordLogger logger = messageAwareConnection.getRecordLogger();
        if (logger == null) {
            return;
        }
        if (!(logger instanceof IManagedRecordLogger)) {
            throw new IllegalStateException("Logger " + logger + " has to implement the IF IManagedMessageLogger");
        }
        IManagedRecordLogger managedLogger = (IManagedRecordLogger) logger;
        managedLogger.close();
        messageAwareConnection.setRecordLogger(null);

    }

    public void connectionRolledback(IPhynixxConnectionProxyEvent event) {
        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        if (con == null || !(con instanceof IRecordLoggerAware)) {
            return;
        }

        IRecordLoggerAware messageAwareConnection = (IRecordLoggerAware) con;

        // Transaction is close and the logger is destroyed ...
        IRecordLogger logger = messageAwareConnection.getRecordLogger();
        if (logger == null) {
            return;
        }
        if (!(logger instanceof IManagedRecordLogger)) {
            throw new IllegalStateException("Logger " + logger + " has to implement the IF IManagedMessageLogger");
        }
        IManagedRecordLogger managedLogger = (IManagedRecordLogger) logger;

        managedLogger.destroy();

        messageAwareConnection.setRecordLogger(null);

        event.getConnectionProxy().addConnectionListener(this);
    }


    public void connectionCommitted(IPhynixxConnectionProxyEvent event) {
        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        if (con == null || !(con instanceof IRecordLoggerAware)) {
            return;
        }

        IRecordLoggerAware messageAwareConnection = (IRecordLoggerAware) con;


        // Transaction is close and the logger is destroyed ...
        IRecordLogger logger = messageAwareConnection.getRecordLogger();
        if (logger == null) {
            return;
        }
        if (!(logger instanceof IManagedRecordLogger)) {
            throw new IllegalStateException("Logger " + logger + " has to implement the IF IManagedMessageLogger");
        }
        IManagedRecordLogger managedLogger = (IManagedRecordLogger) logger;

        managedLogger.destroy();
        messageAwareConnection.setRecordLogger(null);

        event.getConnectionProxy().addConnectionListener(this);
    }


    public void connectionRequiresTransaction(IPhynixxConnectionProxyEvent event) {
        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        if (con == null || !(con instanceof IRecordLoggerAware)) {
            return;
        }

        IRecordLoggerAware messageAwareConnection = (IRecordLoggerAware) con;


        // Transaction is close and the logger is destroyed ...
        IRecordLogger logger = messageAwareConnection.getRecordLogger();
        if (logger == null) {
        }

        // it's my logger ....

        // Transaction is closed and the logger is destroyed ...
        else if (logger instanceof MessageLoggerWrapper) {
            MessageLoggerWrapper wrapper = (MessageLoggerWrapper) logger;
            if (wrapper != null && wrapper.getXAResourceLogger().isClosed()) {
                logger = null;
            }
        } else {
            if (!(logger instanceof IManagedRecordLogger)) {
                throw new IllegalStateException("Logger " + logger + " has to implement the IF IManagedRecordLogger");
            }
            IManagedRecordLogger managedLogger = (IManagedRecordLogger) logger;
            managedLogger.destroy();
            logger = null;
        }

        if (logger == null) {
            try {
                XAResourceLogger xaLogger = this.loggerSystem.instanciateLogger();
                ILogRecordSequence seq = xaLogger.createMessageSequence();
                MessageLoggerWrapper wrapper = new MessageLoggerWrapper(xaLogger, seq);
                messageAwareConnection.setRecordLogger(wrapper);
            } catch (Exception e) {
                // retry ...
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e1) {
                }
                try {
                    XAResourceLogger xaLogger = this.loggerSystem.instanciateLogger();
                    ILogRecordSequence seq = xaLogger.createMessageSequence();
                    MessageLoggerWrapper wrapper = new MessageLoggerWrapper(xaLogger, seq);
                    messageAwareConnection.setRecordLogger(wrapper);
                } catch (Exception ee) {
                    throw new DelegatedRuntimeException("creating new Logger for " + con, ee);
                }
            }
        }
        event.getConnectionProxy().addConnectionListener(this);

    }


    public List readIncompleteTransactions() {
        List messageSequences = new ArrayList();
        // recover all loggers ....
        try {
            Set loggers = this.loggerSystem.recover();
            for (Iterator iterator = loggers.iterator(); iterator.hasNext(); ) {
                XAResourceLogger xaLogger = (XAResourceLogger) iterator.next();

                // recover the message sequences
                xaLogger.readMessageSequences();
                // read all open message sequences of the logger ...
                List seqs = xaLogger.getOpenMessageSequences();
                boolean hasIncompleteSequences = false;
                for (int i = 0; i < seqs.size(); i++) {
                    ILogRecordSequence seq = (ILogRecordSequence) seqs.get(i);
                    if (!seq.isCompleted()) {
                        hasIncompleteSequences = true;
                        MessageLoggerWrapper wrapper = new MessageLoggerWrapper(xaLogger, seq);
                        messageSequences.add(wrapper);
                    }
                }
                if (!hasIncompleteSequences) {
                    xaLogger.destroy();
                }
            }
            return messageSequences;
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }


}
