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
     * the returned connection has to be explicitly closed
     * @return a ready for use managed connection
     */
    IPhynixxManagedConnection<C> getManagedConnection();


    /**
     *
     *
     * Default ist true
     */
    void setAutocommitAware(boolean state);


    /**
     *
     */
    boolean isAutocommitAware();


    /**
     *
     */
    void setSynchronizeConnection(boolean state);


    /**
     *
     */
    boolean isSynchronizeConnection();




    /**
     * closes all currently opened connections
     */
    void close();
}
