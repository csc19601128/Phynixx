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


import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by Christoph Schmidt-Casdorff on 26.08.14.
 */
@Deprecated
public class JTAPersistenceConfig extends EntityManagerConfig {

    @Inject   Environment environment;

    @Override
    public Properties tailorEntityManagerFactoryProperties(Properties props) {
        return props;
    }

    @Override
    @Bean(initMethod = "init", destroyMethod = "close")
        public DataSource dataSource() throws SQLException {

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
        PoolingDataSource ds = new PoolingDataSource();
        // a h2 in-memory database...make sure to use the XADatasources for other databases
        ds.setClassName("org.h2.jdbcx.JdbcDataSource");
        ds.setUniqueName("ds1");
        ds.setMaxPoolSize(10);
        Properties props = new Properties();
        props.put("URL", "jdbc:h2:mem:ds1");
        props.put("user", "sa");
        props.put("password", "");
        ds.setDriverProperties(props);
        ds.init();

        return  ds;
        }

}
