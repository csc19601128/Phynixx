package org.csc.phynixx.spring.integration.config;

import java.util.Properties;

/**
 * Created by Christoph Schmidt-Casdorff on 26.08.14.
 */
public interface TransactionConfig {

    Properties tailorEntityManagerFactoryProperties(Properties props);
}
