package org.csc.phynixx.connection;

import java.util.List;

import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.Dev0Strategy;
import org.csc.phynixx.loggersystem.ILoggerSystemStrategy;
import org.csc.phynixx.loggersystem.messages.IRecordLogger;


public class ConnectionFactory extends PhynixxConnectionProxyListenerAdapter implements IPhynixxConnectionFactory  , IPhynixxConnectionProxyListener
{
	private IPhynixxLogger logger= PhynixxLogManager.getLogger(this.getClass());
	
	private IPhynixxConnectionFactory connectionFactory= null; 
	private IPhynixxConnectionProxyFactory connectionProxyFactory= null; 
	private ILoggerSystemStrategy loggerSystemStrategy= new Dev0Strategy();
	private IPhynixxConnectionProxyDecorator connectionProxyDecorator= null; 
	
	
		
	public ConnectionFactory() 
	{
	}
		
	public ConnectionFactory(IPhynixxConnectionFactory connectionFactory) 
	{
		this(connectionFactory,null);
	}

	public ConnectionFactory(IPhynixxConnectionFactory connectionFactory,
			                       IPhynixxConnectionProxyFactory connectionProxyFactory) 
	{
		this.connectionFactory = connectionFactory;
		if( connectionProxyFactory==null) {
			this.connectionProxyFactory=
				new DynaProxyFactory(new Class[] {connectionFactory.connectionInterface()});
		} else {
			this.connectionProxyFactory = connectionProxyFactory;
		}	
		
	}

	
	public void setConnectionFactory(IPhynixxConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void setConnectionProxyFactory(
			IPhynixxConnectionProxyFactory connectionProxyFactory) {
		this.connectionProxyFactory = connectionProxyFactory;
	}

	public IPhynixxConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public IPhynixxConnectionProxyFactory getConnectionProxyFactory() {
		return connectionProxyFactory;
	}
	
	
	public IPhynixxConnectionProxyDecorator getConnectionProxyDecorator() {
		return connectionProxyDecorator;
	}

	public void setConnectionProxyDecorator(
			IPhynixxConnectionProxyDecorator connectionProxyDecorator) {
		this.connectionProxyDecorator = connectionProxyDecorator;
	}

	public ILoggerSystemStrategy getLoggerSystemStrategy() {
		return loggerSystemStrategy;
	}

	public void setLoggerSystemStrategy(
			ILoggerSystemStrategy loggerSystemStrategy) {
		this.loggerSystemStrategy = loggerSystemStrategy;
	}

	public IPhynixxConnection getConnection() {
		return this.instanciateConnection();
	}
	
	protected IPhynixxConnection instanciateConnection() 
	{		
		try {
			IPhynixxConnectionProxy proxy;
			try {
				IPhynixxConnection connection=
					ConnectionFactory.this.getConnectionFactory().getConnection();
				
				proxy = ConnectionFactory.this.connectionProxyFactory.getConnectionProxy();
				
				proxy.setConnection(connection);
				proxy.addConnectionListener(ConnectionFactory.this);
				
				if( ConnectionFactory.this.loggerSystemStrategy!=null) {
					proxy= ConnectionFactory.this.loggerSystemStrategy.decorate(proxy);
				}
				
				if( ConnectionFactory.this.connectionProxyDecorator!=null) {
					proxy= ConnectionFactory.this.connectionProxyDecorator.decorate(proxy);
				}
			} catch (ClassCastException e) {
				e.printStackTrace();
				throw new DelegatedRuntimeException(e);
			}
			
			return proxy;
		} catch (Throwable e) {
			throw new DelegatedRuntimeException("Instanciating new pooled Proxy",e);
		}
	}
	
	
	public Class connectionInterface() {
		return this.getConnectionFactory().connectionInterface();
	}
	
	public void recover() {
		
		// get all recoverable transaction data
		List messageLoggers= this.loggerSystemStrategy.readIncompleteTransactions();
		IPhynixxConnection con=null ;
		for(int i=0; i < messageLoggers.size();i++) {
			try {
				IRecordLogger msgLogger= (IRecordLogger) messageLoggers.get(i);			
				con= this.getConnection();
				if( (con instanceof IRecordLoggerAware)) {
					((IRecordLoggerAware)con).setRecordLogger(msgLogger);
				}
				con.recover();
			} finally {
				if( con!=null) {
					con.close(); 
				}
			}
		}
		
	}
	public void close() {
		try {
			if( this.loggerSystemStrategy!=null) {
				this.loggerSystemStrategy.close(); 
			}
		} catch (Exception e) {
			throw new DelegatedRuntimeException(e);
		}
		
	}
	
	/**
	 * the connection is released to the pool 
	 */
	public void connectionClosed(IPhynixxConnectionProxyEvent event) 
	{
		IPhynixxConnectionProxy proxy= event.getConnectionProxy();
		if( proxy.getConnection()==null) {
			return;
		}
		if( proxy.getConnection()!=null) {
			proxy.getConnection().close();
		}
		if( logger.isDebugEnabled()) {
			logger.debug("Connection "+proxy +" closed");
		}
	}

	public void connectionDereferenced(IPhynixxConnectionProxyEvent event) {
		throw new IllegalStateException("Connection is bound to a proxy and can't be released");
	}
	
	

}
