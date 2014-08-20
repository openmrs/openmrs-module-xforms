package org.openmrs.module.xforms.fragment.controller.formentry;

import org.openmrs.Patient;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;


public class EncountersFragmentController {
	
	public void controller(@RequestParam("patientId") Patient patient, UiUtils ui,
	                       FragmentModel model) {
	}
}
