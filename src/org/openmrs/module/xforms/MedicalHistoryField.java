package org.openmrs.module.xforms;




/**
 * 
 * @author daniel
 *
 */
public class MedicalHistoryField implements Comparable<MedicalHistoryField> {

	public static final long serialVersionUID = 4454345322324L;

	// Fields

	private Integer fieldId;
	private String name;
	private Integer tabIndex = 0;
	private boolean isNew = false;
	
	
	public MedicalHistoryField(){
		
	}
	
	public MedicalHistoryField(Integer fieldId, String name, Integer tabIndex, boolean isNew){
		this.fieldId = fieldId;
		this.name = name;
		this.tabIndex = tabIndex;
		this.isNew = isNew;
	}
	
	public Integer getFieldId() {
		return fieldId;
	}
	
	public void setFieldId(Integer fieldId) {
		this.fieldId = fieldId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getTabIndex() {
		return tabIndex;
	}
	
	public void setTabIndex(Integer tabIndex) {
		this.tabIndex = tabIndex;
	}
	
	public boolean isNew() {
		return isNew;
	}
	
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	
	public int compareTo(MedicalHistoryField field) {
		if(getFieldId() == field.getFieldId())
			return 0;
		
		if(getTabIndex() < field.getTabIndex())
			return -1;
		
		return 1;
	}
}
