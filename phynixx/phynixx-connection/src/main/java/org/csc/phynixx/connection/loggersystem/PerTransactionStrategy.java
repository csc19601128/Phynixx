package org.csc.phynixx.connection.loggersystem;

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


import org.csc.phynixx.connection.*;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.loggersystem.IXAResourceLogger;
import org.csc.phynixx.loggersystem.RecordLoggerSystem;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logrecord.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class PerTransactionStrategy extends PhynixxConnectionProxyListenerAdapter implements ILoggerSystemStrategy, IPhynixxConnectionProxyListener {

    private RecordLoggerSystem loggerSystem = null;

    class MessageLoggerWrapperData implements IManagedDataRecordLogger {
        private IXAResourceLogger xaresourceLogger = null;
        private IDataRecordSequence msgSeq = null;

        public MessageLoggerWrapperData(IXAResourceLogger xaresourceLogger) {
            super();
            this.xaresourceLogger = xaresourceLogger;
            this.msgSeq = this.xaresourceLogger.createMessageSequence();
        }

        public MessageLoggerWrapperData(IXAResourceLogger xaresourceLogger, IDataRecordSequence seq) {
            super();
            this.xaresourceLogger = xaresourceLogger;
            this.msgSeq = seq;
        }

        public IXAResourceLogger getXAResourceLogger() {
            return xaresourceLogger;
        }

        public IDataRecordSequence getMsgSeq() {
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

        public void replayRecords(IDataRecordReplay replay) {
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
    public void addLoggerListener(IXARecorderResourceListener listener) {
        this.loggerSystem.addListener(listener);

    }


    /**
     * per thread a new Logger cpould be instanciated with aid of the loggerFacrory
     *
     * @param loggersystemName
     * @param loggerFactory
     * @throws Exception
     */
    public PerTransactionStrategy(String loggersystemName, IDataLoggerFactory loggerFactory) throws Exception {
        this.loggerSystem = new RecordLoggerSystem(loggersystemName, loggerFactory);
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
        IXADataRecorder logger = messageAwareConnection.getRecordLogger();
        if (logger == null) {
            return;
        }
        if (!(logger instanceof IManagedDataRecordLogger)) {
            throw new IllegalStateException("Logger " + logger + " has to implement the IF IManagedMessageLogger");
        }
        IManagedDataRecordLogger managedLogger = (IManagedDataRecordLogger) logger;
        managedLogger.close();
        messageAwareConnection.setRecordLogger(null);

    }

    public void connectionRolledback(IPhynixxConnectionProxyEvent event) {
        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        if (con == null || !(con instanceof IRecordLoggerAware)) {
            return;
        }

        IRecordLoggerAware messageAwareConnection = (IRecordLoggerAware) con;

        // Transaction is closed and the logger is destroyed ...
        IXADataRecorder logger = messageAwareConnection.getRecordLogger();
        if (logger == null) {
            return;
        }
        if (!(logger instanceof IManagedDataRecordLogger)) {
            throw new IllegalStateException("Logger " + logger + " has to implement the IF IManagedMessageLogger");
        }
        IManagedDataRecordLogger managedLogger = (IManagedDataRecordLogger) logger;

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
        IXADataRecorder logger = messageAwareConnection.getRecordLogger();
        if (logger == null) {
            return;
        }
        if (!(logger instanceof IManagedDataRecordLogger)) {
            throw new IllegalStateException("Logger " + logger + " has to implement the IF IManagedMessageLogger");
        }
        IManagedDataRecordLogger managedLogger = (IManagedDataRecordLogger) logger;

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
        IXADataRecorder logger = messageAwareConnection.getRecordLogger();
        if (logger == null) {
        }

        // it's my logger ....

        // Transaction is closed and the logger is destroyed ...
        else if (logger instanceof MessageLoggerWrapperData) {
            MessageLoggerWrapperData wrapper = (MessageLoggerWrapperData) logger;
            if (wrapper != null && wrapper.getXAResourceLogger().isClosed()) {
                logger = null;
            }
        } else {
            if (!(logger instanceof IManagedDataRecordLogger)) {
                throw new IllegalStateException("Logger " + logger + " has to implement the IF IManagedDataRecordLogger");
            }
            IManagedDataRecordLogger managedLogger = (IManagedDataRecordLogger) logger;
            managedLogger.destroy();
            logger = null;
        }

        if (logger == null) {
            try {
                IXAResourceLogger xaLogger = this.loggerSystem.instanciateLogger();
                MessageLoggerWrapperData wrapper = new MessageLoggerWrapperData(xaLogger);
                messageAwareConnection.setRecordLogger(wrapper);
            } catch (Exception e) {
                // retry ...
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e1) {
                }
                try {
                    IXAResourceLogger xaLogger = this.loggerSystem.instanciateLogger();
                    IDataRecordSequence seq = xaLogger.createMessageSequence();
                    MessageLoggerWrapperData wrapper = new MessageLoggerWrapperData(xaLogger);
                    messageAwareConnection.setRecordLogger(wrapper);
                } catch (Exception ee) {
                    throw new DelegatedRuntimeException("creating new Logger for " + con, ee);
                }
            }
        }
        event.getConnectionProxy().addConnectionListener(this);

    }


    public List<IManagedDataRecordLogger> readIncompleteTransactions() {
        List messageSequences = new ArrayList();
        // recover all loggers ....
        try {
            Set<IXAResourceLogger> loggers = this.loggerSystem.recover();
            for (Iterator<IXAResourceLogger> iterator = loggers.iterator(); iterator.hasNext(); ) {
                IXAResourceLogger xaLogger = iterator.next();

                // recover the message sequences
                xaLogger.readMessageSequences();
                // read all open message sequences of the logger ...
                List<IDataRecordSequence> seqs = xaLogger.getOpenMessageSequences();
                boolean hasIncompleteSequences = false;
                for (int i = 0; i < seqs.size(); i++) {
                    IDataRecordSequence seq = seqs.get(i);
                    if (!seq.isCompleted()) {
                        hasIncompleteSequences = true;
                        MessageLoggerWrapperData wrapper = new MessageLoggerWrapperData(xaLogger, seq);
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
