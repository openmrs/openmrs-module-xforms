package org.openmrs.module.xforms;

import org.openmrs.Form;

public class FormModuleHandler extends Form {

	private static final long serialVersionUID = 1L;

	private Form form;
	private String moduleId;

	public FormModuleHandler(Form form, String moduleId) {
		this.form = form;
		this.moduleId = moduleId;
	}

	public String getName() {
		return form.getName() + " - (" + moduleId + ")";
	}

	public Integer getFormId() {
		return form.getFormId();
	}
}
