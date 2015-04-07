/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.model;

import org.openmrs.BaseOpenmrsObject;


/**
 * 
 * @author daniel
 *
 */
public class XformCohort extends BaseOpenmrsObject {

    private int cohortId;
    private String name;
    
    public XformCohort() {
        super();
    }

    public XformCohort(int cohortId, String name) {
        super();
        this.cohortId = cohortId;
        this.name = name;
    }

    public int getCohortId() {
        return cohortId;
    }

    public void setCohortId(int cohortId) {
        this.cohortId = cohortId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    

	@Override
	public Integer getId() {
		return getCohortId();
	}

	@Override
	public void setId(Integer id) {
		setCohortId(id);
	}
 }
