/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.xforms.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.api.context.Context;

/**
 * A PersonAttribute is meant as way for implementations to add arbitrary
 * information about a user/patient to their database.
 * 
 * PersonAttributes are essentially just key-value pairs.  However, the 
 * PersonAttributeType can be defined in such a way that the value portion
 * of this PersonAttribute is a foreign key to another database table (like
 * to the location table, or concept table).  This gives a PersonAttribute
 * the ability to link to any other part of the database
 * 
 * A Person can have zero to n PersonAttribute(s).
 * 
 * @see org.openmrs.PersonAttributeType
 * @see org.openmrs.Attributable
 */

public class PersonRepeatAttribute extends BaseOpenmrsObject implements java.io.Serializable {
	
	public static final int VALUE_ID_TYPE_ATTRIBUTE = 1;
	public static final int VALUE_ID_TYPE_CONCEPT = 2;
	public static final int VALUE_ID_TYPE_LOCATION = 3;
	
	private Log log = LogFactory.getLog(getClass());
	public static final long serialVersionUID = 11231211232111L;

	// Fields
	
	private Integer personRepeatAttributeId = 0;
	private Integer personId;
	private Integer attributeTypeId;
	private String value;
	
	private Integer creator;
	private Date dateCreated;

	private Integer changedBy;
	private Date dateChanged;
	
	private Integer voidedBy;
	private Boolean voided = false;
	private Date dateVoided;
	private String voidReason;
	
	private Integer valueIdType;
	private Integer valueId;
	private Integer valueDisplayOrder;
	

	/** default constructor */
	public PersonRepeatAttribute() {
		this.setUuid(UUID.randomUUID().toString());
	}
	
	public PersonRepeatAttribute(Integer personRepeatAttributeId) {
		this();
		this.personRepeatAttributeId = personRepeatAttributeId;
	}
	
	/**
	 * Constructor for creating a basic attribute
	 * @param attributeTypeId
	 * @param value
	 */
	public PersonRepeatAttribute(Integer attributeTypeId, String value) {
		this();
		this.attributeTypeId = attributeTypeId;
		this.value = value;
	}
	
	/**
	 * Shallow copy of this PersonAttribute. Does NOT copy personRepeatAttributeId
	 * 
	 * @return a shallows copy of <code>this</code>
	 */
	public PersonRepeatAttribute copy() {
		return copyHelper(new PersonRepeatAttribute());
	}

	/**
	 * The purpose of this method is to allow subclasses of PersonAttribute to delegate a portion of
	 * their copy() method back to the superclass, in case the base class implementation changes. 
	 * 
	 * @param ret a PersonAttribute that will have the state of <code>this</code> copied into it
	 * @return the PersonAttribute that was passed in, with state copied into it
	 */
	protected PersonRepeatAttribute copyHelper(PersonRepeatAttribute target) {
		target.setPersonId(getPersonId());
		target.setAttributeTypeId(getAttributeTypeId());
		target.setValue(getValue());
		target.setCreator(getCreator());
		target.setDateCreated(getDateCreated());
		target.setChangedBy(getChangedBy());
		target.setDateChanged(getDateChanged());
		target.setVoidedBy(getVoidedBy());
		target.setVoided(getVoided());
		target.setDateVoided(getDateVoided());
		target.setVoidReason(getVoidReason());
		target.setUuid(getUuid());
		return target;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PersonRepeatAttribute) {
			PersonRepeatAttribute attr = (PersonRepeatAttribute)obj;
			return attr.getPersonRepeatAttributeId() != null && attr.getPersonRepeatAttributeId().equals(getPersonRepeatAttributeId());
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getPersonRepeatAttributeId() == null) return super.hashCode();
		int hash = 5;
		hash += 29 * hash + this.getPersonRepeatAttributeId().hashCode();
		return hash;
	}
	
	/**
	 * Compares this PersonAttribute object to the given otherAttribute. This method
	 * differs from {@link #equals(Object)} in that this method compares the
	 * inner fields of each attribute for equality.
	 * 
	 * Note: Null/empty fields on <code>otherAttribute</code> /will not/ cause a
	 * false value to be returned
	 * 
	 * @param otherAttribute PersonAttribute with which to compare
	 * @return boolean true/false whether or not they are the same attributes
	 */
	@SuppressWarnings("unchecked")
    public boolean equalsContent(PersonRepeatAttribute otherAttribute) {
		boolean returnValue = true;

		// these are the methods to compare.
		String[] methods = { "getAttributeTypeId", "getValue", "getVoided"};

		Class attributeClass = this.getClass();

		// loop over all of the selected methods and compare this and other
		for (String methodAttribute : methods) {
			try {
				Method method = attributeClass.getMethod(methodAttribute,
				                                       new Class[] {});

				Object thisValue = method.invoke(this);
				Object otherValue = method.invoke(otherAttribute);

				if (otherValue != null)
					returnValue &= otherValue.equals(thisValue);

			} catch (NoSuchMethodException e) {
				log.warn("No such method for comparison " + methodAttribute, e);
			} catch (IllegalAccessException e) {
				log.error("Error while comparing attributes", e);
			} catch (InvocationTargetException e) {
				log.error("Error while comparing attributes", e);
			}

		}

		return returnValue;
	}
	
	//property accessors

	/**
	 * @return Returns the creator.
	 */
	public Integer getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(Integer creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return Returns the personId.
	 */
	public Integer getPersonId() {
		return personId;
	}

	/**
	 * @param personId The personId to set.
	 */
	public void setPersonId(Integer personId) {
		this.personId = personId;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean isVoided() {
		return voided;
	}
	
	public Boolean getVoided() {
		return isVoided();
	}

	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public Integer getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy The voidedBy to set.
	 */
	
	public void setVoidedBy(Integer voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	/**
	 * @return the attributeTypeId
	 */
	public Integer getAttributeTypeId() {
		return attributeTypeId;
	}

	/**
	 * @param attributeTypeId the attributeTypeId to set
	 */
	public void setAttributeTypeId(Integer attributeTypeId) {
		this.attributeTypeId = attributeTypeId;
	}

	/**
	 * @return the changedBy
	 */
	public Integer getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy the changedBy to set
	 */
	public void setChangedBy(Integer changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return the dateChanged
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged the dateChanged to set
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the personRepeatAttributeId
	 */
	public Integer getPersonRepeatAttributeId() {
		return personRepeatAttributeId;
	}

	/**
	 * @param personRepeatAttributeId the personRepeatAttributeId to set
	 */
	public void setPersonRepeatAttributeId(Integer personRepeatAttributeId) {
		this.personRepeatAttributeId = personRepeatAttributeId;
	}
	
	public Integer getValueIdType() {
		return valueIdType;
	}

	public void setValueIdType(Integer valueIdType) {
		this.valueIdType = valueIdType;
	}

	public Integer getValueId() {
		return valueId;
	}

	public void setValueId(Integer valueId) {
		this.valueId = valueId;
	}

	public Integer getValueDisplayOrder() {
		return valueDisplayOrder;
	}

	public void setValueDisplayOrder(Integer valueDisplayOrder) {
		this.valueDisplayOrder = valueDisplayOrder;
	}

	/**
	 * Convenience method for voiding this attribute
	 * @param reason
	 */
	public void voidAttribute(String reason) {
		setVoided(true);
		setVoidedBy(Context.getAuthenticatedUser().getUserId());
		setVoidReason(reason);
	}
	

	@Override
	public Integer getId() {
		return getPersonRepeatAttributeId();
	}

	@Override
	public void setId(Integer id) {
		setPersonRepeatAttributeId(id);
	}
}