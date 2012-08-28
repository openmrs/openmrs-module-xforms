package org.openmrs.module.xforms.web.controller;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.module.Extension;
import org.openmrs.module.Extension.MEDIA_TYPE;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.FormEntryContext;
import org.openmrs.module.web.extension.FormEntryHandler;
import org.openmrs.module.xforms.FormModuleHandler;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.controller.PortletController;

public class XformsPersonFormEntryPortletController extends PortletController {

	/**
	 * 
	 * @see org.openmrs.web.controller.PortletController#populateModel(javax.servlet.http.HttpServletRequest,
	 *      java.util.Map)
	 */
	@Override
	protected void populateModel(HttpServletRequest request,
			Map<String, Object> model) {
		if (model.containsKey("formToEntryUrlMap")) {
			return;
		}
		Person person = (Person) model.get("person");
		if (person == null)
			throw new IllegalArgumentException(
					"This portlet may only be used in the context of a Person");
		FormEntryContext fec = new FormEntryContext(person);
		Map<FormModuleHandler, FormEntryHandler> entryUrlMap = new TreeMap<FormModuleHandler, FormEntryHandler>(
				new Comparator<FormModuleHandler>() {

					public int compare(FormModuleHandler left, FormModuleHandler right) {
						int temp = left.getName().toLowerCase()
								.compareTo(right.getName().toLowerCase());
						if (temp == 0)
							temp = OpenmrsUtil.compareWithNullAsLowest(
									left.getVersion(), right.getVersion());
						if (temp == 0)
							temp = OpenmrsUtil.compareWithNullAsGreatest(
									left.getId(), right.getId());
						return temp;
					}
				});
		List<Extension> handlers = ModuleFactory.getExtensions(
				"org.openmrs.module.web.extension.FormEntryHandler",
				MEDIA_TYPE.html);
		
		HashMap<Form, Integer> formHandlerCount = new HashMap<Form, Integer>();
		
		if (handlers != null) {
			for (Extension ext : handlers) {
				FormEntryHandler handler = (FormEntryHandler) ext;
				Collection<Form> toEnter = handler.getFormsModuleCanEnter(fec);
				if (toEnter != null) {
					for (Form form : toEnter) {
						entryUrlMap.put(
								new FormModuleHandler(form, ext.getModuleId()),
								handler);
						
						Integer count = formHandlerCount.get(form);
						if(count == null)
							count = 1;
						else
							count++;
						
						formHandlerCount.put(form, count);
					}
				}
			}
		}
		
		//If the formfilter module is installed, filter forms.
		filterForms(entryUrlMap, (Patient)model.get("patient"));
		
		//Make sure we do not append module id when displaying forms that are not handled by more than one module.
		Set<FormModuleHandler> keys = entryUrlMap.keySet();
		for(FormModuleHandler key : keys) {
			key.setAppendModuleId(formHandlerCount.get(key.getForm()) > 1);
		}
		
		model.put("formToEntryUrlMap", entryUrlMap);
		model.put("anyUpdatedFormEntryModules",
				handlers != null && handlers.size() > 0);
	}
	
	private void filterForms(Map<FormModuleHandler, FormEntryHandler> entryUrlMap, Patient patient) {
		try {
			Object instance = OpenmrsClassLoader.getInstance().loadClass("org.openmrs.module.formfilter.web.controller.PersonFormFilterEntryPortletController").newInstance();
			Method method = instance.getClass().getDeclaredMethod("filterForm", new Class[]{Form.class, Patient.class});
			method.setAccessible(true);
			
			for (Iterator<FormModuleHandler> iterator = entryUrlMap.keySet().iterator(); iterator.hasNext();) {
				Form form = ((FormModuleHandler) iterator.next()).getForm();
				if (!(Boolean)method.invoke(instance, new Object[]{form, patient})) {
					iterator.remove();
				}
			}
		}
		catch(Exception ex) {
			//ignore if module is not installed
		}
	}
}
