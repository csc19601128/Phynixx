package org.csc.phynixx.xa;

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


import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.xa.IPhynixxXAResourceListener.IPhynixxXAResourceEvent;

import java.util.EventObject;


public class PhynixxXAResourceEvent<C extends IPhynixxConnection> extends EventObject implements IPhynixxXAResourceEvent {

    /**
     *
     */
    private static final long serialVersionUID = 336547313996504512L;

    public PhynixxXAResourceEvent(PhynixxXAResource<C> xaresource) {
        super(xaresource);

    }

    public PhynixxXAResource<C> getXAResource() {
        return (PhynixxXAResource) this.getSource();
    }

}
