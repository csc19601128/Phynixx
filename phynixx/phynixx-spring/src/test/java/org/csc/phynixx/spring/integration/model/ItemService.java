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


import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Named
public class ItemService {

    @Inject
    ItemDataDao itemDAO;


    /**
     * use the current thread for Thread information
     *
     * @return
     */
    @Transactional
    public void createItem(final String value) {

        final ItemData data = new ItemData();
        data.setValue(value);
        data.setThreadName(Thread.currentThread().getName());
        this.itemDAO.save(data);
    }


    @Transactional
    public List<ItemData> findAllItems() {
        return this.itemDAO.findAllInItems();
    }


}
