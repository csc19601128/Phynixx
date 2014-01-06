package org.csc.phynixx.test_connection;

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


import org.csc.phynixx.connection.IPhynixxConnectionProxy;
import org.csc.phynixx.connection.PhynixxConnectionProxyAdapter;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;


public class TestConnectionProxy extends PhynixxConnectionProxyAdapter implements
        IPhynixxConnectionProxy, ITestConnection {


    public void act(final int inc) {
        PhynixxConnectionProxyAdapter.ExecutionTemplate template =
                new PhynixxConnectionProxyAdapter.ExecutionTemplate() {
                    protected Object call() throws Exception {
                        ((ITestConnection) TestConnectionProxy.this.getConnection()).act(inc);
                        return null;
                    }
                };
        try {
            template.run();
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

    }


    protected IPhynixxConnectionProxy getObservableProxy() {
        return this;
    }


    public Object getId() {
        if (this.getConnection() != null) {
            return ((ITestConnection) this.getConnection()).getId();
        }
        return null;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TestConnectionProxy)) {
            return false;
        }

        return super.equals(obj);

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
        return "unbound";
    }

    public int getCurrentCounter() {
        if (this.getConnection() != null) {
            return ((ITestConnection) this.getConnection()).getCurrentCounter();
        }
        return -1;
    }


}
