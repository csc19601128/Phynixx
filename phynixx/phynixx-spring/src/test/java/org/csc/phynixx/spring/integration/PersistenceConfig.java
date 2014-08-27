package org.csc.phynixx.spring.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Created by zf4iks2 on 26.08.14.
 */
@Configuration
@ComponentScan(basePackages = {"org.csc.phynixx.spring.integration.model"})

@Deprecated
abstract class PersistenceConfig implements TransactionConfig{


    private final String hibernateHbm2ddlAuto = "create-drop";
    // private String hibernateDialect = "org.hibernate.dialect.OracleDialect";
    private final String hibernateDialect = "org.hibernate.dialect.HSQLDialect";
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
                this.setProperty("hibernate.hbm2ddl.auto", PersistenceConfig.this.hibernateHbm2ddlAuto());
            }
        });
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory()
            throws Exception {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        // em.setJtaDataSource(this.dataSource());
        em.setPersistenceUnitName("test");
        em.setPersistenceXmlLocation("classpath:META-INF/bitronix-persistence.xml");


        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform(this.hibernateDialect());
        vendorAdapter.setShowSql(this.hibernateShowSql());

        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(this.jpaProperties());

        return em;
    }


    public abstract DataSource dataSource() throws Exception;
}
