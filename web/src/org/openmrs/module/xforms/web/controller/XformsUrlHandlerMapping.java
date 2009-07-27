package org.openmrs.module.xforms.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;


/**
 * 
 * @author daniel
 *
 */
public class XformsUrlHandlerMapping extends SimpleUrlHandlerMapping{

	public XformsUrlHandlerMapping(){
		super();
	}

	@Override
	protected Object lookupHandler(String arg0, HttpServletRequest arg1) {
		if("/admin/encounters/encounter.form".equals(arg0)){
			if("true".equals(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USE_ENCOUNTER_XFORM,"false")))
				return "formEntry";
		}
		else if("/admin/patients/newPatient.form".equals(arg0)){
			if("true".equals(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USE_PATIENT_XFORM,"false")))
				return "patientReg";
		}

		return super.lookupHandler(arg0, arg1);
	}
}
