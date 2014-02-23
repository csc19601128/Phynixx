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


import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.connection.*;
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
 * this listener observes the lifecycle of a connection and associates a xaDataRecorder if necessary.
 */
public class LoggerPerTransactionStrategy<C extends IPhynixxConnection & IXADataRecorderAware> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxLoggerSystemStrategy<C>, IPhynixxManagedConnectionListener<C> {


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
     * @param loggerFactory
     * @throws Exception
     */
    public LoggerPerTransactionStrategy(IDataLoggerFactory loggerFactory) {
        this.xaRecorderResource = new PhynixxXARecorderResource(loggerFactory);
    }


    @Override
    public void close() {
        this.xaRecorderResource.close();
    }


    @Override
    public void connectionRecovering(IManagedConnectionProxyEvent<C> event) {
        this.connectionRequiresTransaction(event);
    }

    /**
     * Noop
     *
     * @param event current connection
     */
    @Override
    public void connectionReset(IManagedConnectionProxyEvent<C> event) {
        C con = event.getManagedConnection().getCoreConnection();
        if (con == null || !(con instanceof IXADataRecorderAware)) {
            return;
        }

        IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;
        // Transaction is closed and the xaDataRecorder is destroyed ...
        IXADataRecorder xaDataRecorder = messageAwareConnection.getXADataRecorder();
        if (xaDataRecorder == null) {
            return;
        }

        // if commit/rollback was performed, nothing happened. If no the logged data is closed but not destroy. So recovery can happen
        xaDataRecorder.reset();
        // messageAwareConnection.setXADataRecorder(null);


    }

    /**
     * Logger isn't close. If a dataRecorder is found in this phase this indicates an abnormal program flow.,
     * <p/>
     * Therefore the dataRecorder isn't close and keep it's content to possibly recover
     *
     * @param event current connection
     */

    @Override
    public void connectionClosed(IManagedConnectionProxyEvent<C> event) {

        C con = event.getManagedConnection().getCoreConnection();
        if (con == null || !(con instanceof IXADataRecorderAware)) {
            return;
        }

        IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;
        // Transaction is closed and the xaDataRecorder is destroyed ...
        IXADataRecorder xaDataRecorder = messageAwareConnection.getXADataRecorder();
        if (xaDataRecorder == null) {
            return;
        }

        // if commit/rollback was performed, nothing happend. If no the logged data is closed but not destroy. So recovery can happen

        if( event.getManagedConnection().hasTransactionalData()) {
            xaRecorderResource.close(); // cklose without removing the revoer data
        } else {
            xaDataRecorder.destroy();
        }
        messageAwareConnection.setXADataRecorder(null);

    }



    /**
     * destroys the datalogger
     */
    @Override
    public void connectionRecovered(IManagedConnectionProxyEvent<C> event) {
        IPhynixxConnection con = event.getManagedConnection().getCoreConnection();
        if (con == null || !(con instanceof IXADataRecorderAware)) {
            return;
        }

        IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;


        // Transaction is close and the logger is destroyed ...
        IXADataRecorder xaDataRecorder = messageAwareConnection.getXADataRecorder();
        if (xaDataRecorder == null) {
            return;
        }
        // it's my logger ....

        // the logger has to be destroyed ...
        else {
            xaDataRecorder.destroy();
            messageAwareConnection.setXADataRecorder(null);
        }

    }

    @Override
    public void connectionRolledback(IManagedConnectionProxyEvent<C> event) {
        IPhynixxConnection con = event.getManagedConnection().getCoreConnection();
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
        xaDataRecorder.reset();

        // messageAwareConnection.setXADataRecorder(null);

        event.getManagedConnection().removeConnectionListener(this);
    }


    @Override
    public void connectionCommitted(IManagedConnectionProxyEvent<C> event) {
        IPhynixxConnection con = event.getManagedConnection().getCoreConnection();
        if (con == null || !(con instanceof IXADataRecorderAware)) {
            return;
        }

        IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;


        // Transaction is close and the logger is destroyed ...
        IXADataRecorder xaDataRecorder = messageAwareConnection.getXADataRecorder();
        if (xaDataRecorder == null) {
            return;
        }
        xaDataRecorder.reset();
    }


    @Override
    public void connectionRequiresTransaction(IManagedConnectionProxyEvent<C> event) {
        IPhynixxConnection con = event.getManagedConnection().getCoreConnection();
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
            xaDataRecorder.close();
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
        event.getManagedConnection().addConnectionListener(this);

    }


    /**
     * recovers all incomplete dataRecorders {@link org.csc.phynixx.loggersystem.logrecord.IXADataRecorder#isCompleted()} and destroys all complete dataRecorders
     *
     * @return incomplete dataRecorders
     */

    @Override
    public List<IXADataRecorder> readIncompleteTransactions() {
        List<IXADataRecorder> messageSequences = new ArrayList<IXADataRecorder>();
        // recover all loggers ....
        try {

            this.xaRecorderResource.recover();
            Set<IXADataRecorder> xaDataRecorders = this.xaRecorderResource.getXADataRecorders();

            for (Iterator<IXADataRecorder> iterator = xaDataRecorders.iterator(); iterator.hasNext(); ) {
                IXADataRecorder dataRecorder = iterator.next();

                if (!dataRecorder.isCompleted() && !dataRecorder.isEmpty() ) {
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


    @Override
    public IPhynixxManagedConnection<C> decorate(IPhynixxManagedConnection<C> connectionProxy) {
        connectionProxy.addConnectionListener(this);
        return connectionProxy;
    }

}
