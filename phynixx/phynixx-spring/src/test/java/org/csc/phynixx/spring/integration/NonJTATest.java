/**
 * Copyright (C) 2003-2014 Deutsche Post AG
 * All rights reserved.
 */
package org.csc.phynixx.spring.integration;

import org.csc.phynixx.spring.integration.NonJtaPersistenceConfig;
import org.csc.phynixx.spring.integration.model.ItemData;
import org.csc.phynixx.spring.integration.model.ItemService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;


@ContextConfiguration(classes = { NonJtaPersistenceConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@EnableTransactionManagement
public class NonJTATest {

	@Inject
    ItemService itemService;


	@Test
	@Transactional
	public void testItem() throws Exception {

		this.itemService.createItem("Test");

		final List<ItemData> items = this.itemService.findAllItems();
		Assert.assertEquals(1, items.size());
	}



}
