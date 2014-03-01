package org.csc.phynixx.xa;

import javax.transaction.TransactionManager;

import org.csc.phynixx.connection.DynaProxyFactory;
import org.csc.phynixx.test_connection.ITestConnection;
import org.csc.phynixx.test_connection.TestConnection;
import org.csc.phynixx.test_connection.TestConnectionFactory;


public class TestResourceFactory extends PhynixxResourceFactory 
{

	public TestResourceFactory(
			TransactionManager transactionManager) {
		this("TestResourceFactory",transactionManager);
	}

	public TestResourceFactory(String id, 
			TransactionManager transactionManager) {
		super(id, 
			  new XAPooledConnectionFactory(new TestConnectionFactory()),
			  new DynaProxyFactory(new Class[] { ITestConnection.class} ),
			  transactionManager);
	}
	
	public boolean isReleased(TestConnection connection) {
		return this.isFreeConnection(connection);
	}

	
}
