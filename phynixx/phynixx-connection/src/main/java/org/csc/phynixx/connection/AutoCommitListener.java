package org.csc.phynixx.connection;

/*
 * #%L
 * phynixx-xa
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


import org.csc.phynixx.common.cast.ImplementorUtils;

/**
 * checks, if the method is called in autocommit mode.
 *
 * @author Christoph Schmidt-Casdorff
 */
class AutoCommitListener<C extends IPhynixxConnection> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxManagedConnectionListener<C> {

    @Override
    public void connectionRequiresTransactionExecuted(IManagedConnectionEvent<C> event) {

        if(!event.getManagedConnection().hasCoreConnection()) {
            return;
        }

        if(!ImplementorUtils.isImplementationOf(event.getManagedConnection().getCoreConnection(), IAutoCommitAware.class)) {
            return;
        }


        IAutoCommitAware autoCommitAware= ImplementorUtils.cast(event.getManagedConnection().getCoreConnection(),IAutoCommitAware.class);
        if (!autoCommitAware.isAutoCommit()) {
            return;
        }

        if (event.getException() != null) {
            event.getManagedConnection().rollback();
        } else {
            event.getManagedConnection().commit();
        }

    }
}
