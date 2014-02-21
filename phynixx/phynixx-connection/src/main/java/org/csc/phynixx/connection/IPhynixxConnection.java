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


import org.csc.phynixx.common.exceptions.SampleTransactionalException;


public interface IPhynixxConnection {


    /**
     * resets a connection  and prepares it for reuse.
     */
    void reset();

    /**
     * set Autocommit
     */
    void setAutoCommit(boolean autocommit);


    /**
     * @return
     */
    boolean isAutoCommit();

    /**
     */
    void commit();


    /**
     */
    void rollback();


    /**
     * Closes the connection and releases all resources. The connection can not be reused
     *
     */
    void close();



    /**
     */
    void prepare();


}
