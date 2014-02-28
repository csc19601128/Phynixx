package org.csc.phynixx.connection;

/*
 * #%L
 * phynixx-connection
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


import org.csc.phynixx.common.cast.ImplementorUtils;
import org.csc.phynixx.connection.loggersystem.IPhynixxLoggerSystemStrategy;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

import java.util.List;

/**
 * Created by zf4iks2 on 26.02.14.
 */
public class PhynixxRecovery<C extends IPhynixxConnection> implements IPhynixxRecovery<C> {

    private final PhynixxManagedConnectionFactory managedConnectionFactory;
    IPhynixxLoggerSystemStrategy<C> loggerSystemStrategy = null;

    public PhynixxRecovery(IPhynixxConnectionFactory<C> connectionFactory) {
        managedConnectionFactory = new PhynixxManagedConnectionFactory(connectionFactory);
    }

    public IPhynixxLoggerSystemStrategy<C> getLoggerSystemStrategy() {
        return loggerSystemStrategy;
    }

    public void setLoggerSystemStrategy(IPhynixxLoggerSystemStrategy<C> loggerSystemStrategy) {

        this.loggerSystemStrategy = loggerSystemStrategy;
    }

    private IPhynixxManagedConnection<C> getManagedConnection() {
        return this.managedConnectionFactory.getManagedConnection();
    }

    public List<IPhynixxConnectionProxyDecorator<C>> getConnectionProxyDecorators() {
        return this.managedConnectionFactory.getConnectionProxyDecorators();
    }

    public void addConnectionProxyDecorator(
            IPhynixxConnectionProxyDecorator<C> connectionProxyDecorator) {
        this.managedConnectionFactory.addConnectionProxyDecorator(connectionProxyDecorator);
    }


    @Override
    public void recover(IPhynixxRecovery.IRecoveredManagedConnection<C> recoveredManagedConnectionCallback) {

        if (this.loggerSystemStrategy == null) {
            throw new IllegalStateException("LoggerSystem must be reset to recover from this System");
        }
        if (this.loggerSystemStrategy != null) {
            this.loggerSystemStrategy.close();
        }

        try {
            // get all recoverable transaction data
            List<IXADataRecorder> messageLoggers = this.loggerSystemStrategy.readIncompleteTransactions();
            IPhynixxManagedConnection<C> con = null;
            for (int i = 0; i < messageLoggers.size(); i++) {
                try {
                    IXADataRecorder msgLogger = messageLoggers.get(i);
                    con = this.getManagedConnection();
                    if (!ImplementorUtils.isImplementationOf(con, IXADataRecorderAware.class)) {
                        throw new IllegalStateException("Connection does not support " + IXADataRecorderAware.class + " and can't be recovered");
                    } else {

                        // Falls Connection zugeordneten DataLogger hat, so wird dieser freigegeben

                        IXADataRecorderAware xaDataRecorderAware = ImplementorUtils.cast(con, IXADataRecorderAware.class);
                        IXADataRecorder dataRecorder = xaDataRecorderAware.getXADataRecorder();
                        if (dataRecorder != null) {
                            dataRecorder.destroy();
                        }
                        xaDataRecorderAware.setXADataRecorder(msgLogger);
                    }

                    con.recover();

                    if (recoveredManagedConnectionCallback != null) {
                        recoveredManagedConnectionCallback.managedConnectionRecovered(con.toConnection());
                    }
                } finally {
                    if (con != null) {
                        con.close();
                    }
                }
            }

        } finally {
            if (this.loggerSystemStrategy != null) {
                this.loggerSystemStrategy.close();
            }
        }
    }
}
