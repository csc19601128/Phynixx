package org.csc.phynixx.phynixx.test_connection;

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


import org.csc.phynixx.connection.IPhynixxManagedConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestConnectionStatusManager {

    private static Map<Object, List<TestConnectionStatus>> connectionStati = new HashMap<Object, List<TestConnectionStatus>>();

    public static synchronized TestStatusStack getStatusStack(Object connectionId) {
        return new TestStatusStack(connectionId, assertTestConnectionStati(connectionId));
    }

    public synchronized static void clear() {
        connectionStati.clear();
    }


    public synchronized static void registerStatus(IPhynixxManagedConnection<ITestConnection> connection, TestConnectionStatus status) {
        assertTestConnectionStati(connection.toConnection().getConnectionId()).add(status);
    }

    private static List<TestConnectionStatus> assertTestConnectionStati(Object connectionId) {
        List<TestConnectionStatus> stati = connectionStati.get(connectionId);
        if (stati == null) {
            stati = new ArrayList<TestConnectionStatus>();
            connectionStati.put(connectionId, stati);
        }
        return stati;
    }

    public synchronized static String toDebugString() {
        return "TestConnectionStatusManager{" +
                "connectionStati=" + connectionStati +
                '}';
    }
}
