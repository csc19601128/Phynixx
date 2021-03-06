package org.csc.phynixx.spring.integration.model;

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


import org.csc.phynixx.spring.integration.model.ItemData;
import org.csc.phynixx.spring.integration.model.ItemService;
import org.junit.Assert;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by Christoph Schmidt-Casdorff on 26.08.14.
 */
@Named
public class TestScenario {

    @Inject
    ItemService itemService;


    @Transactional
    public void writeAndRead() {
        this.itemService.createItem("Test");

        final List<ItemData> items = this.itemService.findAllItems();
        Assert.assertEquals(1, items.size());
    }


    @Transactional
    public List<ItemData> findItems() {
        return this.itemService.findAllItems();
    }


    /**
     * creates one Items an throw an Exception
     * throw Exception to provoke rollback
     */
    @Transactional
    public void  throwException() {

        this.itemService.createItem("Test");
        throw new IllegalStateException("Do Rollback");
    }
}
