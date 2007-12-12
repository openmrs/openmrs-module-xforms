package org.openmrs.module.xforms;

import java.util.Date;

import org.openmrs.Form;
import org.openmrs.User;

/**
 * This class holds the XForm of a an openmrs form.
 * 
 * @author Daniel Kayiwa
 * 
 */
public class Xform { 

	/** The xml for the XForm. */
	private String xformData;
	
	/** The formId of the form that this XForms represents. */
	private int formId;
	
	/** The user who submitted this XForm to the database. */
	private User creator;
	
	/** The date this XForm was submitted to the database. */
	private Date dateCreated;
	
	/**
	 * Default constructor
	 */
	public Xform() {
	}
	
	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
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
     * @return the formId
     */
    public int getFormId() {
    	return formId;
    }

	/**
     * @param formId the id of the form to set
     */
    public void setFormId(int formId) {
    	this.formId = formId;
    }

	/**
     * @return the xformData
     */
    public String getXformData() {
    	return xformData;
    }

	/**
     * @param xformData - the xformData to set
     */
    public void setXformData(String xformData) {
    	this.xformData = xformData;
    }
}
