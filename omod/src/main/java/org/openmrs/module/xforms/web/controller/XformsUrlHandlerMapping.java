package org.openmrs.module.xforms.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
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
	protected Object lookupHandler(String arg0, HttpServletRequest arg1) throws Exception {
		if("/admin/encounters/encounter.form".equals(arg0)){
			if("true".equals(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USE_ENCOUNTER_XFORM,"false"))){

				//Make sure we are not from Admininstration/Manage Encounters/Add New Encounter
				if(arg1.getParameter("encounterId") != null){
					Encounter encounter = Context.getEncounterService().getEncounter(Integer.parseInt(arg1.getParameter("encounterId")));
					Form form = encounter.getForm();
					if(form != null){ //Some encounters may not have forms attached to them.
						Xform xform = ((XformsService)Context.getService(XformsService.class)).getXform(form);
						if(xform != null && xform.getLayoutXml() != null)
							return "formEntry";
					}
				}
			}
		}
		else if("/admin/patients/newPatient.form".equals(arg0) || "/admin/patients/shortPatientForm.form".equals(arg0)
				/*|| "/admin/patients/patient.form".equals(arg0)*/){
			if("true".equals(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USE_PATIENT_XFORM,"false")))
				return "patientReg";
		}
		else if("/portlets/personFormEntry.portlet".equals(arg0)) {
			return "personFormEntryPortlet";
		}

		return super.lookupHandler(arg0, arg1);
	}
}
