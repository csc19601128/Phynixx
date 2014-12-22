/**
 * Copyright (C) 2003-2014 Deutsche Post AG
 * All rights reserved.
 */
package org.csc.phynixx.spring.integration.model;

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
