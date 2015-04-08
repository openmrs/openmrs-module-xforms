/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.model;


/**
 * This class encapsulates a patient database field
 * 
 * @author Daniel
 *
 */
public class PatientTableField {

	private int id;
	private String name;
	private String tableName;
	private String columnName;
	
	public PatientTableField(){
		
	}

	public PatientTableField(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public PatientTableField(int id, String name, String tableName, String columnName) {
		super();
		this.id = id;
		this.name = name;
		this.tableName = tableName;
		this.columnName = columnName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String toString() {
		return name;
	}
}
