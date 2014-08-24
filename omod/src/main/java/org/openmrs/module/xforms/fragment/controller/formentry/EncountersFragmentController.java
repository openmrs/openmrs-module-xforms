package org.openmrs.module.xforms.fragment.controller.formentry;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.Extension.MEDIA_TYPE;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.extension.FormEntryHandler;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;


public class EncountersFragmentController {
	
	public void controller(@RequestParam("patientId") Patient patient, UiUtils ui,
	                       FragmentModel model) {
		
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(patient);
		Collections.sort(encounters, new EncounterComparator());
		model.put("encounters", encounters);
		
		Map<Form, String> viewUrlMap = new HashMap<Form, String>();
		Map<Form, String> editUrlMap = new HashMap<Form, String>();
		
		List<Extension> handlers = ModuleFactory.getExtensions("org.openmrs.module.web.extension.FormEntryHandler",
		    MEDIA_TYPE.html);
		if (handlers != null) {
			for (Extension ext : handlers) {
				FormEntryHandler handler = (FormEntryHandler) ext;
				{ // view
					Collection<Form> toView = handler.getFormsModuleCanView();
					if (toView != null) {
						if (handler.getViewFormUrl() == null) {
							throw new IllegalArgumentException("form entry handler " + handler.getClass()
							        + " is trying to handle viewing forms but specifies no URL");
						}
						for (Form form : toView) {
							viewUrlMap.put(form, handler.getViewFormUrl());
						}
					}
				}
				{ // edit
					Collection<Form> toEdit = handler.getFormsModuleCanEdit();
					if (toEdit != null) {
						if (handler.getEditFormUrl() == null) {
							throw new IllegalArgumentException("form entry handler " + handler.getClass()
							        + " is trying to handle editing forms but specifies no URL");
						}
						for (Form form : toEdit) {
							editUrlMap.put(form, handler.getEditFormUrl());
						}
					}
				}
			}
		}
		
		model.put("formToViewUrlMap", viewUrlMap);
		model.put("formToEditUrlMap", editUrlMap);
	}
	
	private class EncounterComparator implements Comparator<Encounter> {
	    @Override
	    public int compare(Encounter e1, Encounter e2) {
	        return e1.getEncounterDatetime().compareTo(e2.getEncounterDatetime());
	    }
	}
}
