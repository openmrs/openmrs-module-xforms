package org.purc.purcforms.client.service;

import com.google.gwt.user.client.rpc.RemoteService;


/**
 * 
 * @author daniel
 *
 */
public interface FormDesignerService extends RemoteService {

	String[] getForm(int formId);
	void saveForm(int formId, String[] form);
	String[] getFormNames();
}
