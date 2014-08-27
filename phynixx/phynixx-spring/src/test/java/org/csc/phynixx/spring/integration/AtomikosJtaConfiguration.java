package org.csc.phynixx.spring.integration;

import com.atomikos.datasource.RecoverableResource;
import com.atomikos.icatch.*;
import com.atomikos.icatch.admin.LogAdministrator;
import com.atomikos.icatch.config.TSInitInfo;
import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.icatch.jta.hibernate3.TransactionManagerLookup;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.inject.Inject;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by christoph on 23.08.2014.
 */

public class AtomikosJtaConfiguration implements TransactionConfig  {

    @Inject
    private Environment environment;

    public Properties tailorEntityManagerFactoryProperties(Properties properties) {
        properties.setProperty("hibernate.transaction.manager_lookup_class",
                TransactionManagerLookup.class.getName());
        return properties;
    }

    @Bean(destroyMethod = "shutdownForce")
    public UserTransactionService userTransactionService() {

        UserTransactionService service= new UserTransactionServiceImp() ;
        Properties props= new Properties();

        props.put("com.atomikos.icatch.service","com.atomikos.icatch.standalone.UserTransactionServiceFactory");
        props.put("com.atomikos.icatch.log_base_name","aaaaaa");
        props.put("com.atomikos.icatch.output_dir","../standalone/log/");
        props.put("com.atomikos.icatch.log_base_dir","../standalone/log/");

        service.init(props);

        return service;

    }

    @Bean
    public PlatformTransactionManager platformTransactionManager() throws Throwable {
        UserTransaction userTransaction = this.userTransaction();
        TransactionManager transactionManager = this.transactionManager();
        return new JtaTransactionManager(userTransaction, transactionManager);
    }

    @Bean
    public UserTransaction userTransaction() throws Throwable {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(1000);
        return userTransactionImp;
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public TransactionManager transactionManager() throws Throwable {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        userTransactionManager.setStartupTransactionService(false);
        return userTransactionManager;
    }


    /**
     @Bean(initMethod = "init", destroyMethod = "close")
     public ConnectionFactory connectionFactory() {
     ActiveMQXAConnectionFactory activeMQXAConnectionFactory = new ActiveMQXAConnectionFactory();
     activeMQXAConnectionFactory.setBrokerURL(this.environment.getProperty( "jms.broker.url")  );
     AtomikosConnectionFactoryBean atomikosConnectionFactoryBean = new AtomikosConnectionFactoryBean();
     atomikosConnectionFactoryBean.setUniqueResourceName("xamq");
     atomikosConnectionFactoryBean.setLocalTransactionMode(false);
     atomikosConnectionFactoryBean.setXaConnectionFactory(activeMQXAConnectionFactory);
     return atomikosConnectionFactoryBean;
     }
     */

}

