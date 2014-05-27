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


class ManagedPhynixxConnectionEvent<C extends IPhynixxConnection> implements IManagedConnectionEvent<C> {

    private Exception exception = null;

    private IPhynixxManagedConnection<C> source;

    /**
     *
     */
    private static final long serialVersionUID = 2146374246818609618L;

    public ManagedPhynixxConnectionEvent(IPhynixxManagedConnection<C> source) {
        this.source = source;
    }


    public ManagedPhynixxConnectionEvent(IPhynixxManagedConnection<C> source, Exception exception) {
        this(source);
        this.exception = exception;
    }


    public Exception getException() {
        return exception;
    }

    public IPhynixxManagedConnection<C> getManagedConnection() {
        return this.source;
    }


    public String toString() {
        return this.getManagedConnection() + ((getException() != null) ? " Exception:: " + getException() : "");
    }


}
