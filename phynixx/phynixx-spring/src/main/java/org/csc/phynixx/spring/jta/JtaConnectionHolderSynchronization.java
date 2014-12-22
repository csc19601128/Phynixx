package org.csc.phynixx.spring.jta;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.support.ResourceHolder;
import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Named
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
final class JtaConnectionHolderSynchronization<H extends ResourceHolder, K> extends ResourceHolderSynchronization<H,K> implements TransactionSynchronization {

	public JtaConnectionHolderSynchronization(H resourceHolder, K resourceKey) {
		super(resourceHolder, resourceKey);
	}


}