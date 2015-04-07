/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.web;

import org.openmrs.api.context.Context;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class RefAppConfiguration implements BeanFactoryPostProcessor {
	
	/**
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			//http://stackoverflow.com/questions/11606504/registering-beansprototype-at-runtime-in-spring
			Class cls = Context.loadClass("org.openmrs.ui.framework.StandardModuleUiConfiguration");
			BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(cls);
			builder.addPropertyValue("moduleId", "xforms"); // set property value
			((DefaultListableBeanFactory) beanFactory).registerBeanDefinition("xformsStandardModuleUiConfiguration",
			    builder.getBeanDefinition());
		}
		catch (ClassNotFoundException ex) {
			//ignore as this means we are not running under the reference app
		}
	}
}
