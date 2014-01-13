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
import org.csc.phynixx.loggersystem.logrecord.IDataRecord;
import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;
import org.csc.phynixx.loggersystem.logrecord.XALogRecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * written data are fired an forgotten. Their are not stored as if wriiten to dev0.
 */

public class Dev0Strategy extends PhynixxConnectionProxyListenerAdapter implements ILoggerSystemStrategy {

    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());


    public static final Dev0Logger THE_DEV0_LOGGER = new Dev0Logger();

    private static class Dev0Logger implements IXADataRecorder {

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

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public IDataRecord createDataRecord(XALogRecordType logRecordType, byte[][] recordData) {
            return null;
        }

        public void writeRollbackData(byte[] data) {
        }

        public void writeRollbackData(byte[][] data) {
        }

        public void close() {
        }

        public void destroy() {
        }

        @Override
        public List<IDataRecord> getDataRecords() {
            return Collections.emptyList();
        }

        @Override
        public long getXADataRecorderId() {
            return -1;
        }

        @Override
        public void recover() {

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
        if (con == null || !(con instanceof IXADataRecorderAware)) {
            return;
        }
        IXADataRecorderAware messageAwareConnection = (IXADataRecorderAware) con;
        // Transaction is close and the logger is destroyed ...
        Dev0Logger logger = (Dev0Logger) messageAwareConnection.getXADataRecorder();
        if (logger == null) {
            messageAwareConnection.setXADataRecorder(THE_DEV0_LOGGER);
        }
        event.getConnectionProxy().addConnectionListener(this);
    }


    public List<IXADataRecorder> readIncompleteTransactions() {
        return new ArrayList(0);
    }


}