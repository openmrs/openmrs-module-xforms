package org.openmrs.module.xforms;

import org.openmrs.Form;

public class FormModuleHandler extends Form {

	private static final long serialVersionUID = 1L;

	private Form form;
	private String moduleId;
	private boolean appendModuleId;

	public FormModuleHandler(Form form, String moduleId) {
		this.form = form;
		this.moduleId = moduleId;
	}

	public String getName() {
		return form.getName() + (appendModuleId ? " - (" + moduleId + ")" : "");
	}

	public Integer getFormId() {
		return form.getFormId();
	}
	
	public Boolean getPublished() {
		return form.getPublished();
	}
	
	public Boolean getRetired() {
		return form.getRetired();
	}

	public boolean isAppendModuleId() {
		return appendModuleId;
	}

	public void setAppendModuleId(boolean appendModuleId) {
		this.appendModuleId = appendModuleId;
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}
}
