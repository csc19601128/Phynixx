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


/**
 *
 * maps the call of {@link IPhynixxManagedConnection#close()} to an implementation
 *
 * Its no interface to keep this strategy hidden
 * Created by christoph on 22.02.14.
 */
abstract class CloseStrategy<C extends IPhynixxConnection> {

    /**
     * marks a connection a close from a transactional context. The implematations decides if it can be re-used or if it is destroyes
     * @param managedConnection
     */
    abstract void close(PhynixxManagedConnectionGuard<C> managedConnection);


}
