package org.csc.phynixx.spring.integration.config;

/*
 * #%L
 * phynixx-spring
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


import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

import org.csc.phynixx.spring.integration.model.ItemData;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.inject.Inject;
import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by Christoph Schmidt-Casdorff on 26.08.14.
 */

@EnableTransactionManagement
@Configuration
@ComponentScan(basePackages = { "org.csc.phynixx.spring.integration.model" })
public class BitronixPersistenceConfig implements TransactionConfig{

    @Inject   Environment environment;

    @Bean(destroyMethod = "shutdown")
    public BitronixTransactionManager bitronixManager() {
        return TransactionManagerServices.getTransactionManager();
    }

    @Bean
    public PlatformTransactionManager jtaTransactionManager() {
        JtaTransactionManager jta = new JtaTransactionManager();
        jta.setTransactionManager(bitronixManager());
        jta.setUserTransaction(bitronixManager());
        return jta;
    }

    @Override
    public Properties tailorEntityManagerFactoryProperties(Properties props) {
        return props;
    }

    @Bean( destroyMethod = "close")
        public PoolingDataSource dataSource() throws SQLException {

        /**  no XA support for hsqldb

        JDBCXADataSource hsqlXaDataSource= new JDBCXADataSource();

        hsqlXaDataSource.setUrl("jdbc:hsqldb:mem:" + "Test");
        hsqlXaDataSource.setUser("sa");
        hsqlXaDataSource.setPassword("");


            MysqlXADataSource mysqlXaDataSource = new MysqlXADataSource();
            mysqlXaDataSource.setUrl(this.environment.getProperty("dataSource.url"));
            mysqlXaDataSource.setPinGlobalTxToPhysicalConnection(true);
            mysqlXaDataSource.setPassword(this.environment.getProperty("dataSource.password"));
            mysqlXaDataSource.setUser(this.environment.getProperty("dataSource.password"));
**/
        // Each JVM needs a stable unique identifier for TX recovery
        TransactionManagerServices.getConfiguration().setServerId("myServer1234");

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
                this.setProperty("hibernate.hbm2ddl.auto", BitronixPersistenceConfig.this.hibernateHbm2ddlAuto());
            }
        });
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory()
            throws Exception {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setJtaDataSource(this.dataSource());
        em.setPersistenceUnitName("test");
        em.setPersistenceXmlLocation("classpath:META-INF/bitronix-persistence.xml");
        em.setPackagesToScan(ItemData.class.getPackage().getName());


        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform(this.hibernateDialect());
        vendorAdapter.setShowSql(this.hibernateShowSql());

        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(this.jpaProperties());

        return em;
    }


}
