/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.xforms.aop;

import java.lang.reflect.Method;

import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformsService;
import org.springframework.aop.AfterReturningAdvice;

/**
 * Advice for deleting an xform attached to a form which has been deleted.
 * 
 * @since 4.0.3
 */
public class FormDeleteAfterAdvice implements AfterReturningAdvice {
	
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		if (method.getName().equals("purgeForm") || method.getName().equals("deleteForm")) {
			Context.getService(XformsService.class).deleteXform((Form) args[0]);
		}
	}
}
