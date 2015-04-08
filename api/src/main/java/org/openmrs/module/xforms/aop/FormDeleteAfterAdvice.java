/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.aop;

import java.util.Date;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.util.DOMUtil;
import org.openmrs.module.xforms.util.XformsUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Advice for deleting an xform attached to a form which has been deleted.
 * Also added support for duplicating an xform when a form is duplicated.
 * This class should be renamed to reflect these two functionalities instead of only form delete.
 * 
 * @since 4.0.3
 */
public class FormDeleteAfterAdvice implements MethodInterceptor {
	
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		Object object = null;
		
		if (invocation.getMethod().getName().equals("purgeForm") || invocation.getMethod().getName().equals("deleteForm")) {
			object = invocation.proceed();
			Context.getService(XformsService.class).deleteXform((Form) invocation.getArguments()[0]);
		}
		else if(invocation.getMethod().getName().equals("duplicateForm")) {

			XformsService xformsService = Context.getService(XformsService.class);
			Xform oldXform = xformsService.getXform((Form)invocation.getArguments()[0]);

			object = invocation.proceed();
			
			Form newForm = (Form)object;
			
			//check if xform is found for the duplicated form.
			if(oldXform != null) {
				Xform newXform = new Xform();
				newXform.setFormId(newForm.getFormId());
				newXform.setJavaScriptSrc(oldXform.getJavaScriptSrc());
				newXform.setLayoutXml(oldXform.getLayoutXml());
				newXform.setLocaleXml(oldXform.getLocaleXml());
				newXform.setCreator(Context.getAuthenticatedUser());
				newXform.setDateCreated(new Date());
				
				Document doc = XformsUtil.fromString2Doc(oldXform.getXformXml());
				Element formElement = DOMUtil.getElement(doc, "form");
				formElement.setAttribute("id", newForm.getFormId().toString());
				formElement.setAttribute("name", newForm.getName());
				formElement.setAttribute("uuid", newForm.getUuid());
				formElement.setAttribute("version", newForm.getVersion());
								
				newXform.setXformXml(XformsUtil.doc2String(doc));
				
				xformsService.saveXform(newXform);
			}
		}
		else {
			object = invocation.proceed();
		}
		
        return object;
    }
}
