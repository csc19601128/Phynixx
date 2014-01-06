package org.csc.phynixx.test_connection;

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


import java.util.HashMap;
import java.util.Map;

public class TestConnectionStatusManager {

    private static Map connectionStati = new HashMap();

    public static synchronized TestStatusStack getStatusStack(Object id) {
        return (TestStatusStack) (connectionStati.get(id));
    }

    public static synchronized void addStatusStack(Object id) {
        if (!connectionStati.containsKey(id)) {
            connectionStati.put(id, new TestStatusStack(id));
        }
    }

    public synchronized static void clear() {
        connectionStati.clear();
    }


}
