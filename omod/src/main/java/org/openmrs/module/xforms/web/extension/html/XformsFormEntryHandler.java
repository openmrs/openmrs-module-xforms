package org.openmrs.module.xforms.web.extension.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.web.FormEntryContext;
import org.openmrs.module.web.extension.FormEntryHandler;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.util.OpenmrsConstants;


/**
 * 
 * @author danielkayiwa
 *
 */
public class XformsFormEntryHandler extends FormEntryHandler {
	
	/**
     * @see org.openmrs.module.web.extension.FormEntryModuleExtension#getFormEntryUrl()
     */
    public String getFormEntryUrl() {
	    return "module/xforms/formEntry.form?target=xformentry";
    }
    
	/**
     * @see org.openmrs.module.web.extension.FormEntryModuleExtension#getViewFormUrl()
     */
    public String getViewFormUrl() {
    	if ("true".equals(Context.getAdministrationService().getGlobalProperty("xforms.viewEncounterAsXform", "false"))) {
    		return getEditFormUrl();
    	}
    	
    	return "admin/encounters/encounterDisplay.list";
    }

	/**
     * @see org.openmrs.module.web.extension.FormEntryHandler#getEditFormUrl()
     */
    public String getEditFormUrl() {
    	return "admin/encounters/encounter.form";
    }
    
    /**
     * @see org.openmrs.module.web.extension.FormEntryModuleExtension#getFormList()
     */
    public List<Form> getFormsModuleCanEnter(FormEntryContext formEntryContext) {
    	return addAllXForms(new ArrayList<Form>());
    }

	/**
     * @see org.openmrs.module.web.extension.FormEntryModuleExtension#getFormsModuleCanView()
     */
    public Set<Form> getFormsModuleCanView() {
    	return addAllXForms(new HashSet<Form>());
    }

	/**
     * @see org.openmrs.module.web.extension.FormEntryHandler#getFormsModuleCanEdit()
     */
    public Set<Form> getFormsModuleCanEdit() {
    	return addAllXForms(new HashSet<Form>());
    }

    private <C extends Collection<Form>> C addAllXForms(C collection) {
    	boolean showUnpublished = Context.getAuthenticatedUser().hasPrivilege(OpenmrsConstants.PRIV_VIEW_UNPUBLISHED_FORMS);
    	Set<Form> ret = new LinkedHashSet<Form>();
    	
    	FormService formService = Context.getFormService();
    	
    	List<Xform> xforms = ((XformsService)Context.getService(XformsService.class)).getXforms();
	    for (Xform xform : xforms) {
	    	Form form = formService.getForm(xform.getFormId());
	    	if (form != null && (showUnpublished || form.getPublished()))
	    		ret.add(form);
	    }
	    collection.addAll(ret);
	    return collection;
    }
}
