/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ModuleActivator;

/**
 * The xforms activator as required by the openmrs module spec.
 * 
 * @author Daniel
 *
 */
public class XformsActivator implements ModuleActivator {

	private Log log = LogFactory.getLog(this.getClass());


	@Override
	public void willRefreshContext() {
		
	}

	@Override
	public void contextRefreshed() {
		
	}

	@Override
	public void willStart() {
		
	}

	@Override
	public void started() {
		log.info("Started Xforms Module");
	}

	@Override
	public void willStop() {
		
	}

	@Override
	public void stopped() {
		log.info("Stopped Xforms Module");
	}
}
