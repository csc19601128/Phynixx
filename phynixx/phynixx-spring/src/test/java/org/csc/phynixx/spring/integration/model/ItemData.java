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


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class ItemData {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ITEM")
	@SequenceGenerator(name = "SEQ_ITEM", sequenceName = "SEQ_ITEM", allocationSize = 1)

	@Column(name = "ID_ITEM")
	private Integer id;

	@Column(name = "VALUE_ITEM")
	private String value;


	@Column(name = "THREAD_ITEM")
	private String threadName;

	public ItemData() {
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	/**
	 * @return the threadName
	 */
	public String getThreadName() {
		return this.threadName;
	}

	/**
	 * @param threadName
	 *            the threadName to set
	 */
	public void setThreadName(final String threadName) {
		this.threadName = threadName;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return this.id;
	}





}
