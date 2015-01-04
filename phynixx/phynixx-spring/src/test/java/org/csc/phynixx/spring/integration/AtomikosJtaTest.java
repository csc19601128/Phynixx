/**
 * Copyright (C) 2003-2014 Deutsche Post AG
 * All rights reserved.
 */
package org.csc.phynixx.spring.integration;

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

import org.csc.phynixx.spring.integration.config.AtomikosPersistenceConfig;
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


@ContextConfiguration(classes = { AtomikosPersistenceConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@EnableTransactionManagement
public class AtomikosJtaTest {

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



    @Test
    public void testRollback() throws Exception {

        try {
            this.testScenario.throwException();
        } catch(Exception e) {};

        final List<ItemData> items = this.testScenario.findItems();
        Assert.assertEquals(0, items.size());
    }



}
