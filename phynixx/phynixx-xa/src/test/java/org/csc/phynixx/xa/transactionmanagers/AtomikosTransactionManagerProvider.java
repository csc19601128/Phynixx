package org.csc.phynixx.xa.transactionmanagers;

/*
 * #%L
 * phynixx-xa
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

import java.io.File;
import java.util.Properties;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.commons.io.FileUtils;

import com.atomikos.datasource.xa.TemporaryXATransactionalResource;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionManager;

public class AtomikosTransactionManagerProvider implements
		ITransactionManagerProvider {
	UserTransactionManager userTransactionManager = null;

	private static String LOG_DIR = "./tmp/atomikos";

	private static UserTransactionServiceImp userTransactionService() {
		UserTransactionServiceImp srv = new UserTransactionServiceImp();

		Properties props = new Properties();
		props.setProperty("com.atomikos.icatch.service",
				"com.atomikos.icatch.standalone.UserTransactionServiceFactory");
		props.setProperty("initialLogAdministrators",
				"com.atomikos.icatch.admin.imp.LocalLogAdministrator");

		// see http://www.atomikos.com/Documentation/JtaProperties
		// see
		// http://svn.codehaus.org/jetty/jetty/tags/jetty-6.1.26/examples/test-jndi-webapp/src/templates/jta.properties

		long maxJtaTimeOut = 1800;
		// Specifies the maximum timeout (in milliseconds) that can be allowed
		// for transactions.
		props.put("com.atomikos.icatch.default_jta_timeout",
				Long.toString(maxJtaTimeOut));

		// Set the max timeout (in milliseconds) for local transactions
		props.put("com.atomikos.icatch.max_timeout",
				Long.toString(maxJtaTimeOut));
		props.put("com.atomikos.icatch.service",
				"com.atomikos.icatch.standalone.UserTransactionServiceFactory");

		// Set the max number of active local transactions
		props.put("com.atomikos.icatch.max_actives", Long.toString(100));
		props.put("com.atomikos.icatch.log_base_name", "centera");
		props.put("com.atomikos.icatch.console_file_name", "AtomikosLog");

		// Set this to WARN, INFO or DEBUG to control the granularity of output
		// to the console file.
		props.put("com.atomikos.icatch.console_log_level", "WARN");
		props.put("com.atomikos.icatch.output_dir", LOG_DIR + "/log/");
		props.put("com.atomikos.icatch.log_base_dir", LOG_DIR
				+ "/standalone/log/");
		/**
		 * If you want to do explicit resource registration then you need to set
		 * this value to false. See later in this manual for what explicit
		 * resource registration means.
		 * 
		 * Wird benoetigt, um Centera anzubinden, die explizit sich enlistet
		 **/
		props.put("com.atomikos.icatch.automatic_resource_registration",
				"false");

		// this should never be disabled on production or data integrity cannot
		// be guaranteed.
		props.put("com.atomikos.icatch.enable_logging", "true");

		srv.init(props);

		return srv;

	}

	private static final UserTransactionServiceImp userTransactionService = userTransactionService();

	@Override
	public TransactionManager getTransactionManager() {
		if (this.userTransactionManager == null) {
			throw new IllegalStateException("Atomikos is already stopped");
		}
		return this.userTransactionManager;
	}

	@Override
	public void stop() {
		if (userTransactionManager != null) {
			this.userTransactionManager.setForceShutdown(true);
			this.userTransactionManager.close();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
		}
		this.userTransactionManager = null;

		// loesche LOG_DIR
		File logDir = new File(LOG_DIR);

		FileUtils.deleteQuietly(logDir);

	}

	@Override
	public void start() throws Exception {

		this.userTransactionManager = new UserTransactionManager();
		userTransactionManager.setForceShutdown(false);
		userTransactionManager.setTransactionTimeout(1200000);
		userTransactionManager.setStartupTransactionService(false);
	}

	@Override
	public void register(XAResource xaResource) {
		com.atomikos.icatch.system.Configuration
				.addResource(new TemporaryXATransactionalResource(xaResource));
	}

}