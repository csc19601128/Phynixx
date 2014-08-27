/**
 * Copyright (C) 2003-2014 Deutsche Post AG
 * All rights reserved.
 */
package org.csc.phynixx.spring.integration;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.csc.phynixx.spring.integration.model.ItemData;
import org.csc.phynixx.spring.integration.model.ItemService;
import org.csc.phynixx.spring.integration.model.TestScenario;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import java.util.List;


@ContextConfiguration(classes = { BitronixPersistenceConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@EnableTransactionManagement
public class BitronixJtaTest {

	@Inject
    TestScenario testScenario;


    @Inject
    ItemService itemService;


    @Inject
    PoolingDataSource dataSource;

    @After
    public void tearDown() {
        this.dataSource.close();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }


	@Test
	public void testItem() throws Exception {

		this.testScenario.writeAndRead();

		final List<ItemData> items = this.testScenario.findItems();
		Assert.assertEquals(1, items.size());
	}



}
