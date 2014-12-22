package org.csc.phynixx.spring.integration.atomikos;

/**
 * Created by Christoph Schmidt-Casdorff on 27.08.14.
 *
 * @see http://stackoverflow.com/questions/20681245/how-to-use-atomikos-transaction-essentials-with-hibernate-4-3
 */


import com.atomikos.icatch.jta.UserTransactionManager;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * see http://stackoverflow.com/questions/20681245/how-to-use-atomikos-transaction-essentials-with-hibernate-4-3
 */
public class AtomikosJtaPlatform extends AbstractJtaPlatform {

    private static final long serialVersionUID = 1L;
    private UserTransactionManager utm;

    public AtomikosJtaPlatform() {
        utm = new UserTransactionManager();
    }

    @Override
    protected TransactionManager locateTransactionManager() {
        return utm;
    }

    @Override
    protected UserTransaction locateUserTransaction() {
        return utm;
    }
}

