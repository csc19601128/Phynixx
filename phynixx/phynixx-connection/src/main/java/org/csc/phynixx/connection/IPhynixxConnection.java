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
     * opens a connection  for reuse. If it isn't close an Exception is thrown
     */
    void reset();

    /**
     * @throws SampleTransactionalException
     */
    void commit();


    /**
     * @throws SampleTransactionalException
     */
    void rollback();


    /**
     * @throws SampleTransactionalException
     */
    void close();

    /*
     *
     */
    boolean isClosed();


    /**
     * @throws SampleTransactionalException
     */
    void prepare();


}
