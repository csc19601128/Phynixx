/**
 * Copyright (C) 2003-2014 Deutsche Post AG
 * All rights reserved.
 */
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


import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ComponentScan(basePackages = { "org.csc.phynixx.spring.integration.model"})
public class NonJtaPersistenceConfig {

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
                this.setProperty("hibernate.hbm2ddl.auto", NonJtaPersistenceConfig.this.hibernateHbm2ddlAuto());
            }
        });
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory()
            throws Exception {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(this.dataSource());
        em.setPersistenceUnitName("test");
        em.setPersistenceXmlLocation("classpath:META-INF/nonjta-persistence.xml");


        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform(this.hibernateDialect());
        vendorAdapter.setShowSql(this.hibernateShowSql());

        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(this.jpaProperties());

        return em;
    }


    @Bean(destroyMethod = "shutdown")
	@Named
	public DataSource dataSource() throws Exception {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
				// .addScript("classpath:sql/createDatamodel.sql")
				// .addScript("classpath:jpa/sql/insertDatamodel.sql")
				.build();
	}

    public Properties tailorEntityManagerFactoryProperties(Properties props) {
        return props;
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws Exception {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(this.entityManagerFactory().getObject());
        return transactionManager;
    }



}
