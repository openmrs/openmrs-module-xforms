package org.openmrs.module.xforms.model;

import org.openmrs.BaseOpenmrsObject;


/**
 * An Xform remote user. This class provides a set of attributes for user downloads
 * to external applications like mobile devices. These attributes are used to ensure that only
 * authorized openmrs users can do data entry using these devices, as a security measure.
 * The main reason why we are not using the openmrs User class is because it does not expose 
 * the salt and password fields as needed for authentication from these devices.
 * 
 * @author Daniel
 *
 */
public class XformUser extends BaseOpenmrsObject {

	private int userId;
	private String name;
	private String password;
	private String salt;
	
	public XformUser(){
		
	}
	
	public XformUser(int userId,String systemId,String name, String password, String salt){
		this.userId = userId;
		this.name = name;
		this.password = password;
		this.salt = salt;
		
		//Sometimes the name can be empty.
		if(this.name == null || this.name.trim().length() == 0)
			this.name = systemId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	@Override
	public Integer getId() {
		return getUserId();
	}

	@Override
	public void setId(Integer id) {
		setUserId(id);
	}
}
