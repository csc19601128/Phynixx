package org.csc.phynixx.xa;

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


import org.apache.commons.lang.Validate;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;

class TransactionBinding<C extends IPhynixxConnection> implements ITransactionBinding<C>{
     
    private TransactionBindingType transactionBindingType=TransactionBindingType.NoTransaction;
    private GlobalTransactionProxy<C> globalTransactionProxy;
    private LocalTransactionProxy<C> localTransactionProxy;


    @Override
    public IPhynixxManagedConnection<C> getManagedConnection() {
        if (this.isGlobalTransaction()) {
            return this.getEnlistedGlobalTransaction().getConnection();
        } else if (this.isLocalTransaction()) {
            return this.getEnlistedLocalTransaction().getConnection();
        } else {
            throw new IllegalStateException("no transaction");
        }
    }
     
    
    @Override
    public void close() {
        if (this.isGlobalTransaction()) {
            this.getEnlistedGlobalTransaction().close();
        } else if (this.isLocalTransaction()) {
            this.getEnlistedLocalTransaction().close();
        } 
        reset();
    }

    
    @Override
    public void release() {
        if (this.isGlobalTransaction()) {
            this.getEnlistedGlobalTransaction().release();
        } else if (this.isLocalTransaction()) {
            this.getEnlistedLocalTransaction().release();
        } 
        reset();
    }


    private void reset() {
        this.transactionBindingType=TransactionBindingType.NoTransaction; 
        this.globalTransactionProxy=null;
        this.localTransactionProxy=null;
    }


    @Override
    public TransactionBindingType getTransactionBindingType() {
     return transactionBindingType;
    }


    @Override
    public boolean isLocalTransaction() {
        return localTransactionProxy!=null;
    }


    @Override
    public boolean isGlobalTransaction() {
        return globalTransactionProxy!=null;
    }


    @Override
    public GlobalTransactionProxy<C> getEnlistedGlobalTransaction() {
        Validate.isTrue(isGlobalTransaction(), "not in global transaction");
        return globalTransactionProxy;
      }
    


    @Override
    public LocalTransactionProxy<C> getEnlistedLocalTransaction() {
      Validate.isTrue(isLocalTransaction(), "not in local transaction");
      return localTransactionProxy;
    }


    @Override
    public void activateGlobalTransaction(GlobalTransactionProxy<C> proxy) {
        checkReset();
        this.globalTransactionProxy=proxy;
        transactionBindingType=TransactionBindingType.GlobalTransaction;
    }

        
   


    @Override
    public String toString() {
        return "TransactionBinding [transactionBindingType=" + this.transactionBindingType
                + ", globalTransactionProxy=" + this.globalTransactionProxy + ", localTransactionProxy="
                + this.localTransactionProxy + "]";
    }


    @Override
    public void activateLocalTransaction(LocalTransactionProxy<C> proxy) {
        checkReset();
        this.localTransactionProxy=proxy;
        transactionBindingType=TransactionBindingType.LocalTransaction;
        
    }


    private void checkReset() {
        Validate.isTrue(this.transactionBindingType==TransactionBindingType.NoTransaction, "A Transaction already associated");  
        Validate.isTrue(this.localTransactionProxy==null, "Local Transaction already associated");
        Validate.isTrue(this.globalTransactionProxy==null, "Global Transaction already associated");
    }
    
    

}
