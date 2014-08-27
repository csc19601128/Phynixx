package org.csc.phynixx.spring.integration;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by zf4iks2 on 26.08.14.
 *
 * @see http://www.atomikos.com/Documentation/SpringIntegration
 */

@EnableTransactionManagement
@Configuration
@ComponentScan(basePackages = { "org.csc.phynixx.spring.integration.model" })
public class AtomikosPersistenceConfig extends PersistenceConfig implements TransactionConfig{

    @Inject   Environment environment;


    @Named("userTransactionService")
    @Bean(destroyMethod = "shutdownForce")
    UserTransactionServiceImp userTransactionService() {
        UserTransactionServiceImp srv= new UserTransactionServiceImp();
        Properties props= new Properties();
        props.setProperty("com.atomikos.icatch.service", "com.atomikos.icatch.standalone.UserTransactionServiceFactory");
        props.setProperty("initialLogAdministrators","com.atomikos.icatch.admin.imp.LocalLogAdministrator");

        props.put("com.atomikos.icatch.service","com.atomikos.icatch.standalone.UserTransactionServiceFactory");
        props.put("com.atomikos.icatch.log_base_name","aaaaaa");
        props.put("com.atomikos.icatch.output_dir","../standalone/log/");
        props.put("com.atomikos.icatch.log_base_dir","../standalone/log/");

        srv.init(props);

        return srv;

    }

    @Bean(destroyMethod = "close")
    @DependsOn("userTransactionService")
    public UserTransactionManager atomikosTransactionManager() {
        UserTransactionManager mgr= new UserTransactionManager();
        mgr.setStartupTransactionService(false);
        mgr.setForceShutdown(false);
        return mgr;
    }

    @DependsOn("userTransactionService")
    public UserTransactionImp atomikosTransaction() throws Exception {
        UserTransactionImp ta= new UserTransactionImp();
        ta.setTransactionTimeout(5000);
        return ta;
    }




    @Bean
    public PlatformTransactionManager jtaTransactionManager() {
        JtaTransactionManager jta = new JtaTransactionManager();
        jta.setTransactionManager(atomikosTransactionManager());
        jta.setUserTransaction(atomikosTransactionManager());
        return jta;
    }

    @Override
    public Properties tailorEntityManagerFactoryProperties(Properties props) {
        return props;
    }

    @Override
    @Bean( destroyMethod = "close")
        public PoolingDataSource dataSource() throws SQLException {

        PoolingDataSource ds = new PoolingDataSource();

        // a h2 in-memory database...make sure to use the XADatasources for other databases
        ds.setClassName("org.h2.jdbcx.JdbcDataSource");
        ds.setUniqueName("ds1");
        ds.setMaxPoolSize(10);

        // Ansonsten keine automatische Enlistement als XAResource
        ds.setAllowLocalTransactions(true);
        Properties props = new Properties();

        // muss gross geschrieben werden; Propertery heisst URL statt url
        props.put("URL", "jdbc:h2:mem:ds1");
        props.put("user", "sa");
        props.put("password", "");
        ds.setUniqueName("ds1");
        ds.setDriverProperties(props);
        // ds.init();

        return  ds;
        }



    private final String hibernateHbm2ddlAuto = "create-drop";
    // private String hibernateDialect = "org.hibernate.dialect.OracleDialect";
    private final String hibernateDialect = "org.hibernate.dialect.H2Dialect";
    private final Boolean hibernateShowSql = true;

    public String hibernateHbm2ddlAuto() {
        return this.hibernateHbm2ddlAuto;
    }

    public String hibernateDialect() {
        return this.hibernateDialect;
    }

    public Boolean hibernateShowSql() {
        return this.hibernateShowSql;
    }

    private Properties jpaProperties() {
        return this.tailorEntityManagerFactoryProperties(new Properties() {
            private static final long serialVersionUID = -4475596706494273349L;
            {
                this.setProperty("hibernate.hbm2ddl.auto", AtomikosPersistenceConfig.this.hibernateHbm2ddlAuto());
            }
        });
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory()
            throws Exception {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setJtaDataSource(this.dataSource());
        em.setPersistenceUnitName("test");
        em.setPersistenceXmlLocation("classpath:META-INF/atomikos-persistence.xml");


        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform(this.hibernateDialect());
        vendorAdapter.setShowSql(this.hibernateShowSql());

        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(this.jpaProperties());

        return em;
    }


}
