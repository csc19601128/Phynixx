package org.csc.phynixx.connection;

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


import java.util.EventObject;


class PhynixxConnectionProxyEvent<C extends IPhynixxConnection> extends EventObject implements IManagedConnectionProxyEvent<C> {

    private Exception exception = null;

    /**
     *
     */
    private static final long serialVersionUID = 2146374246818609618L;

    public PhynixxConnectionProxyEvent(IPhynixxManagedConnection<C> source) {
        super(source);
    }


    public PhynixxConnectionProxyEvent(IPhynixxManagedConnection<C> source, Exception exception) {
        super(source);
        this.exception = exception;
    }


    public Exception getException() {
        return exception;
    }

    public IPhynixxManagedConnection<C> getManagedConnection() {
        return (IPhynixxManagedConnection) this.getSource();
    }


    public String toString() {
        return this.getManagedConnection() + ((getException() != null) ? " Exception:: " + getException() : "");
    }


}
