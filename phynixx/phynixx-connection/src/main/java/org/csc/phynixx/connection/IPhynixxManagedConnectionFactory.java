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


public interface IPhynixxManagedConnectionFactory<C extends IPhynixxConnection> {

    /**
     * Created by christoph on 02.02.14.
     */
    interface IRecoveredManagedConnection<C> {
        public void managedConnectionRecovered(C con);
    }

    IPhynixxManagedConnection<C> getManagedConnection();

    void recover(IRecoveredManagedConnection<C> recoveredManagedConnectionCallback);

    void close();
}
