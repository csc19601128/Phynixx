package org.csc.phynixx.spring.integration;

import java.util.Properties;

/**
 * Created by zf4iks2 on 26.08.14.
 */
public interface TransactionConfig {

    Properties tailorEntityManagerFactoryProperties(Properties props);
}
