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
