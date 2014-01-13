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
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;
import org.csc.phynixx.loggersystem.logrecord.IXARecorderResource;
import org.csc.phynixx.loggersystem.logrecord.IXARecorderResourceListener;
import org.csc.phynixx.loggersystem.logrecord.PhynixxXARecorderResource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * this listener observes the lifecycle of a connection and associates a xaDataRecorder is neccessary
 */
public class PerTransactionStrategy extends PhynixxConnectionProxyListenerAdapter implements ILoggerSystemStrategy, IPhynixxConnectionProxyListener {


    private IXARecorderResource xaRecorderResource;

    /**
     * the logger is added to all instanciated Loggers
     */
    public void addLoggerListener(IXARecorderResourceListener listener) {
        // this.xaRecorderResource.addListener(listener);

    }


    /**
     * per thread a new Logger cpould be instanciated with aid of the loggerFacrory
     *
     *
     * @param loggerFactory
     * @throws Exception
     */
    public PerTransactionStrategy(IDataLoggerFactory loggerFactory) {
        this.xaRecorderResource = new PhynixxXARecorderResource(loggerFactory);
    }


    public IPhynixxConnectionProxy decorate(IPhynixxConnectionProxy connectionProxy) {
        connectionProxy.addConnectionListener(this);
        return connectionProxy;
    }

    public void close() {
        this.xaRecorderResource.close();
    }


    public void connectionRecovering(IPhynixxConnectionProxyEvent event) {
        this.connectionRequiresTransaction(event);

    }


    public void connectionClosed(IPhynixxConnectionProxyEvent event) {

        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        if (con == null || !(con instanceof IXADataRecorderAware)) {
            return;
        }

        IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;
        // Transaction is closed and the xaDataRecorder is destroyed ...
        IXADataRecorder xaDataRecorder = messageAwareConnection.getXADataRecorder();
        if (xaDataRecorder == null) {
            return;
        }
        xaDataRecorder.close();
        messageAwareConnection.setXADataRecorder(null);

    }

    public void connectionRolledback(IPhynixxConnectionProxyEvent event) {
        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        if (con == null || !(con instanceof IXADataRecorderAware)) {
            return;
        }

        IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;

        // Transaction is closed and the logger is destroyed ...
        IXADataRecorder xaDataRecorder = messageAwareConnection.getXADataRecorder();
        if (xaDataRecorder == null) {
            return;
        }

        // if the rollback is completed the rollback data isn't needed
        xaDataRecorder.destroy();

        messageAwareConnection.setXADataRecorder(null);

        event.getConnectionProxy().addConnectionListener(this);
    }


    public void connectionCommitted(IPhynixxConnectionProxyEvent event) {
        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        if (con == null || !(con instanceof IXADataRecorderAware)) {
            return;
        }

        IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;


        // Transaction is close and the logger is destroyed ...
        IXADataRecorder xaDataRecorder = messageAwareConnection.getXADataRecorder();
        if (xaDataRecorder == null) {
            return;
        }
        xaDataRecorder.destroy();
        messageAwareConnection.setXADataRecorder(null);

        event.getConnectionProxy().addConnectionListener(this);
    }


    public void connectionRequiresTransaction(IPhynixxConnectionProxyEvent event) {
        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        if (con == null || !(con instanceof IXADataRecorderAware)) {
            return;
        }

        IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;


        // Transaction is close and the logger is destroyed ...
        IXADataRecorder xaDataRecorder = messageAwareConnection.getXADataRecorder();
        if (xaDataRecorder == null) {
        }
        // it's my logger ....

        // Transaction is closed and the logger is destroyed ...
        else if (xaDataRecorder.isClosed()) {
            xaDataRecorder = null;
            xaDataRecorder.destroy();
        }

        if (xaDataRecorder == null) {
            try {
                IXADataRecorder xaLogger = this.xaRecorderResource.createXADataRecorder();
                messageAwareConnection.setXADataRecorder(xaLogger);
            } catch (Exception e) {
                // retry ...
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e1) {
                }
                try {
                    IXADataRecorder xaLogger = this.xaRecorderResource.createXADataRecorder();
                    messageAwareConnection.setXADataRecorder(xaLogger);
                } catch (Exception ee) {
                    throw new DelegatedRuntimeException("creating new Logger for " + con, ee);
                }
            }
        }
        event.getConnectionProxy().addConnectionListener(this);

    }


    /**
     * recovers all incomplete dataRecorders {@link org.csc.phynixx.loggersystem.logrecord.IXADataRecorder#isCompleted()} and destroys all complete dataRecorders
     *
     * @return incomplete dataRecorders
     */
    public List<IXADataRecorder> readIncompleteTransactions() {
        List<IXADataRecorder> messageSequences = new ArrayList<IXADataRecorder>();
        // recover all loggers ....
        try {

            this.xaRecorderResource.recover();
            Set<IXADataRecorder> xaDataRecorders = this.xaRecorderResource.getXADataRecorders();

            for (Iterator<IXADataRecorder> iterator = xaDataRecorders.iterator(); iterator.hasNext(); ) {
                IXADataRecorder dataRecorder = iterator.next();

                if (!dataRecorder.isCompleted()) {
                    messageSequences.add(dataRecorder);
                } else {
                    dataRecorder.destroy();
                }

            }
            return messageSequences;
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }


}