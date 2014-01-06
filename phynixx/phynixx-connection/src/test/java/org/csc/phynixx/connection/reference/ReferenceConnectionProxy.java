package org.csc.phynixx.connection.reference;

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


public class ReferenceConnectionProxy extends PhynixxConnectionProxyAdapter implements
        IPhynixxConnectionProxy, IReferenceConnection {


    protected IPhynixxConnectionProxy getObservableProxy() {
        return this;
    }


    public Object getId() {
        if (this.getConnection() != null) {
            return ((IReferenceConnection) this.getConnection()).getId();
        }
        return null;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ReferenceConnectionProxy)) {
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


    public int getCounter() {
        if (this.getConnection() != null) {
            return ((IReferenceConnection) this.getConnection()).getCounter();
        }
        return -1;
    }

    public void incCounter(int inc) {
        if (this.getConnection() != null) {
            this.fireConnectionRequiresTransaction();
            ((IReferenceConnection) this.getConnection()).incCounter(inc);
        }

    }


    public void setInitialCounter(int value) {
        if (this.getConnection() != null) {
            this.fireConnectionRequiresTransaction();
            ((IReferenceConnection) this.getConnection()).setInitialCounter(value);
        }

    }

}
