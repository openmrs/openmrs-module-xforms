/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.page.controller.formentry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.module.Extension;
import org.openmrs.module.Extension.MEDIA_TYPE;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.FormEntryContext;
import org.openmrs.module.web.extension.FormEntryHandler;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.bind.annotation.RequestParam;

public class PatientPageController {
	
	public void controller(@RequestParam("patientId") Patient patient, UiUtils ui,
	                       @RequestParam(value = "returnUrl", required = false) String returnUrl,
	                       PageModel model) {
		
		model.addAttribute("returnUrl", returnUrl);
		model.addAttribute("patient", patient);
		model.put("formToEntryUrlMap", getForms(patient));
	}
	
	private Map<Form, FormEntryHandler> getForms(Patient patient) {
		List<String> excludFormUuids =  new ArrayList<String>();
		excludFormUuids.add("a000cb34-9ec1-4344-a1c8-f692232f6edd");
		excludFormUuids.add("c75f120a-04ec-11e3-8780-2b40bef9a44b");
		excludFormUuids.add("d2c7532c-fb01-11e2-8ff2-fd54ab5fdb2a");
		excludFormUuids.add("b5f8ffd8-fbde-11e2-8ff2-fd54ab5fdb2a");
		excludFormUuids.add("a007bbfe-fbe5-11e2-8ff2-fd54ab5fdb2a");
		
		FormEntryContext fec = new FormEntryContext(patient);
		Map<Form, FormEntryHandler> entryUrlMap = new TreeMap<Form, FormEntryHandler>(new Comparator<Form>() {
			
			public int compare(Form left, Form right) {
				int temp = left.getName().toLowerCase().compareTo(right.getName().toLowerCase());
				if (temp == 0)
					temp = OpenmrsUtil.compareWithNullAsLowest(left.getVersion(), right.getVersion());
				if (temp == 0)
					temp = OpenmrsUtil.compareWithNullAsGreatest(left.getId(), right.getId());
				return temp;
			}
		});
		
		List<Extension> handlers = ModuleFactory.getExtensions("org.openmrs.module.web.extension.FormEntryHandler",
		    MEDIA_TYPE.html);
		
		if (handlers != null) {
			for (Extension ext : handlers) {
				FormEntryHandler handler = (FormEntryHandler) ext;
				Collection<Form> toEnter = handler.getFormsModuleCanEnter(fec);
				if (toEnter != null) {
					for (Form form : toEnter) {
						if (excludFormUuids.contains(form.getUuid())) {
							continue;
						}
						
						entryUrlMap.put(form, handler);
					}
				}
			}
		}
		
		return entryUrlMap;
	}
}
