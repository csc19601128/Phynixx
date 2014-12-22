package org.csc.phynixx.spring.integration.model;

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
