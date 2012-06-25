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
