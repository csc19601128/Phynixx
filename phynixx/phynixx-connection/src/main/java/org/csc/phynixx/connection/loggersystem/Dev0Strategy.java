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
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.IManagedDataRecordLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * written data are fired an forgotten. Their are not stored as if wriiten to dev0.
 */

public class Dev0Strategy extends PhynixxConnectionProxyListenerAdapter implements ILoggerSystemStrategy {

    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());


    public static final Dev0Logger THE_DEV0_LOGGER = new Dev0Logger();

    private static class Dev0Logger implements IManagedDataRecordLogger {

        public void commitRollforwardData(byte[][] data) {
        }

        public void commitRollforwardData(byte[] data) {
        }

        public boolean isCommitting() {
            return false;
        }

        public boolean isCompleted() {
            return false;
        }

        public boolean isPrepared() {
            return false;
        }

        public void replayRecords(IDataRecordReplay replay) {
        }

        public void writeRollbackData(byte[] data) {
        }

        public void writeRollbackData(byte[][] data) {
        }

        public void close() {
        }

        public void destroy() {
        }

    }

    public void close() {
    }

    public IPhynixxConnectionProxy decorate(IPhynixxConnectionProxy connectionProxy) {
        connectionProxy.addConnectionListener(this);
        return connectionProxy;
    }


    public void connectionRequiresTransaction(IPhynixxConnectionProxyEvent event) {
        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        if (con == null || !(con instanceof IRecordLoggerAware)) {
            return;
        }
        IRecordLoggerAware messageAwareConnection = (IRecordLoggerAware) con;
        // Transaction is close and the logger is destroyed ...
        Dev0Logger logger = (Dev0Logger) messageAwareConnection.getRecordLogger();
        if (logger == null) {
            messageAwareConnection.setRecordLogger(THE_DEV0_LOGGER);
        }
        event.getConnectionProxy().addConnectionListener(this);
    }


    public List<IManagedDataRecordLogger> readIncompleteTransactions() {
        return new ArrayList(0);
    }


}
