package org.csc.phynixx.xa.transactionmanagers;

/*
 * #%L
 * phynixx-xa
 * %%
 * Copyright (C) 2014 - 2015 Christoph Schmidt-Casdorff
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


import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.objectweb.jotm.Jotm;

public class  JotmTransactionManagerProvider implements ITransactionManagerProvider {
    Jotm jotm=null;

    @Override
    public TransactionManager getTransactionManager() {
        return this.jotm.getTransactionManager();
    }

    @Override
    public void start() throws Exception {
        if(this.jotm!=null) {
            throw new IllegalStateException("Jotm is already started");
        }
        this.jotm =  new Jotm(true, false);
    }

    @Override
    public void stop() throws Exception {
            if(jotm!=null) {
                this.jotm.stop();
            }
        this.jotm=null;
    }


    @Override
    public void register(XAResource xaResource) {
        
    }

}