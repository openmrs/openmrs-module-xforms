/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.aop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.Extension;

public class AppFrameworkServiceAdvisor implements MethodInterceptor {
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		Object object = invocation.proceed();
		;
		
		if (invocation.getMethod().getName().equals("getExtensionsForCurrentUser")) {
			String extensionPointId = (String) invocation.getArguments()[0];
			if ("org.openmrs.referenceapplication.encounterTemplate".equals(extensionPointId)) {
				
				List<EncounterType> encounterTypes = Context.getEncounterService().getAllEncounterTypes();
				
				List<Extension> extensions = (List<Extension>) object;
				for (Extension extension : extensions) {
					Map<String, Object> extensionParams = extension.getExtensionParams();
					Map<String, Object> types = (Map<String, Object>) extensionParams.get("supportedEncounterTypes");
					
					if (!"defaultEncounterTemplate".equals(extensionParams.get("templateId"))) {
						continue;
					}
					
					for (EncounterType type : encounterTypes) {
						if ("ca3aed11-1aa4-42a1-b85c-8332fc8001fc".equals(type.getUuid())
						        || "25a042b2-60bc-4940-a909-debd098b7d82".equals(type.getUuid())) {
							continue;
						}
						
						if (types.containsKey(type.getUuid())) {
							continue;
						}
						
						HashMap map = new HashMap();
						map.put("icon", "icon-file-alt");
						map.put("editable", "true");
						
						types.put(type.getUuid(), map);
					}
				}
				
				return extensions;
			}
		}
		
		return object;
	}
}
