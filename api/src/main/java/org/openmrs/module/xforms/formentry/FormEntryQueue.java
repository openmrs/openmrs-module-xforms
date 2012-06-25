package org.openmrs.module.xforms.formentry;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

public class FormEntryQueue  extends BaseOpenmrsObject {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private int formEntryQueueId;
	private String formData;
	private User creator;
	private Date dateCreated;
	
	private String fileSystemUrl;

	/**
	 * @return Returns the formEntryQueueId.
	 */
	public int getFormEntryQueueId() {
		return formEntryQueueId;
	}

	/**
	 * @param formEntryQueueId
	 *            The formEntryQueueId to set.
	 */
	public void setFormEntryQueueId(int formEntryQueueId) {
		this.formEntryQueueId = formEntryQueueId;
	}

	/**
	 * Gets the xml that this queue item holds.  If formData is null
	 * and fileSystemUrl is not null, the data is "lazy loaded" from
	 * the filesystem
	 * 
	 * @return Returns the formData.
	 */
	public String getFormData() {
		
		if (formData == null && fileSystemUrl != null) {
			// lazy load the form data from the filesystem
			
			File file = new File(fileSystemUrl);
			
			if (file.exists()) {
				try {
					formData = OpenmrsUtil.getFileAsString(file);
					return formData;
				}
				catch (IOException io) {
					log.warn("Unable to lazy load the formData from: " + fileSystemUrl, io);
				}
			}
			else {
				log.warn("File system url does not exist for formentry queue item.  Url: '" + fileSystemUrl + "'");
			}
				
		}
		
		return formData;
	}

	/**
	 * @param formData
	 *            The formData to set.
	 */
	public void setFormData(String formData) {
		this.formData = formData;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
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
	 * @param dateCreated
	 *            The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * Used when the queue items are stored on the server filesystem.
	 * 
     * @return the fileSystemUrl
     */
    public String getFileSystemUrl() {
    	return fileSystemUrl;
    }

	/**
	 * Used when the queue items are stored on the server filesystem
     * @param fileSystemUrl the fileSystemUrl to set
     */
    public void setFileSystemUrl(String fileSystemUrl) {
    	this.fileSystemUrl = fileSystemUrl;
    }
	
	
    @Override
	public Integer getId() {
		return getFormEntryQueueId();
	}

	@Override
	public void setId(Integer id) {
		setFormEntryQueueId(id);
	}
}
