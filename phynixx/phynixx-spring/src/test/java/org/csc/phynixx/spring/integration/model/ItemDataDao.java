/**
 * Copyright (C) 2003-2014 Deutsche Post AG
 * All rights reserved.
 */
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


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;


@Repository
public class ItemDataDao {

	@PersistenceContext
	private EntityManager em;

	ItemDataDao() {
		super();
	}

	public void save(final ItemData role) {
		this.em.persist(role);
	}

	public void flush() {
		this.em.flush();
	}

	public List<ItemData> findAllInItems() {

        this.flush();
        return this.em.createQuery("from ItemData item").getResultList();
	}

}
