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
