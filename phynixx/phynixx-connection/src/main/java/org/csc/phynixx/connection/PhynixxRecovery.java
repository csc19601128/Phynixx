package org.csc.phynixx.connection;

import org.csc.phynixx.common.cast.ImplementorUtils;
import org.csc.phynixx.connection.loggersystem.IPhynixxLoggerSystemStrategy;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

import java.util.Collections;
import java.util.List;

/**
 * Created by zf4iks2 on 26.02.14.
 */
public class PhynixxRecovery<C extends IPhynixxConnection> implements IPhynixxRecovery<C>{

    IPhynixxLoggerSystemStrategy<C> loggerSystemStrategy=null;
    private final PhynixxManagedConnectionFactory managedConnectionFactory;

    public PhynixxRecovery(IPhynixxConnectionFactory<C> connectionFactory ){
        managedConnectionFactory = new PhynixxManagedConnectionFactory(connectionFactory);
    }

    public IPhynixxLoggerSystemStrategy<C> getLoggerSystemStrategy() {
        return loggerSystemStrategy;
    }

    public void setLoggerSystemStrategy(IPhynixxLoggerSystemStrategy<C> loggerSystemStrategy) {

        this.loggerSystemStrategy = loggerSystemStrategy;
    }

    private IPhynixxManagedConnection<C> getManagedConnection() {
        return this.managedConnectionFactory.getManagedConnection();
    }

    public List<IPhynixxConnectionProxyDecorator<C>> getConnectionProxyDecorators() {
        return this.managedConnectionFactory.getConnectionProxyDecorators();
    }

    public void addConnectionProxyDecorator(
            IPhynixxConnectionProxyDecorator<C> connectionProxyDecorator) {
        this.managedConnectionFactory.addConnectionProxyDecorator(connectionProxyDecorator);
    }


    @Override
    public void recover(IPhynixxRecovery.IRecoveredManagedConnection<C> recoveredManagedConnectionCallback) {

        if(this.loggerSystemStrategy==null) {
            throw new IllegalStateException("LoggerSystem must be reset to recover from this System");
        }
        if( this.loggerSystemStrategy!=null) {
            this.loggerSystemStrategy.close();
        }
        // get all recoverable transaction data
        List<IXADataRecorder> messageLoggers = this.loggerSystemStrategy.readIncompleteTransactions();
        IPhynixxManagedConnection<C> con = null;
        for (int i = 0; i < messageLoggers.size(); i++) {
            try {
                IXADataRecorder msgLogger = messageLoggers.get(i);
                con = this.getManagedConnection();
                if (!ImplementorUtils.isImplementationOf(con, IXADataRecorderAware.class)) {
                    throw new IllegalStateException("Connection does not support " + IXADataRecorderAware.class + " and can't be recovered");
                } else {

                    // Falls Connection zugeordneten DataLogger hat, so wird dieser freigegeben

                    IXADataRecorderAware xaDataRecorderAware = ImplementorUtils.cast(con, IXADataRecorderAware.class);
                    IXADataRecorder dataRecorder= xaDataRecorderAware.getXADataRecorder();
                    if( dataRecorder!=null) {
                        dataRecorder.destroy();
                    }
                    xaDataRecorderAware.setXADataRecorder(msgLogger);
                }

                con.recover();

                if (recoveredManagedConnectionCallback != null) {
                    recoveredManagedConnectionCallback.managedConnectionRecovered(con.toConnection());
                }
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        }

    }
}
