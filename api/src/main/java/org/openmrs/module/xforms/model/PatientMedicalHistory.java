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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.xforms.serialization.Persistent;
import org.openmrs.module.xforms.serialization.SerializationUtils;


/**
 * 
 * Holds the medical history of a patient.
 * 
 * @author daniel
 *
 */
public class PatientMedicalHistory implements Persistent{

	private int patientId;
	private List<MedicalHistoryFieldData> history;
	
	
	public List<MedicalHistoryFieldData> getHistory() {
		return history;
	}

	public void setHistory(List<MedicalHistoryFieldData> history) {
		this.history = history;
	}

	public int getPatientId() {
		return patientId;
	}

	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}
	
	public void addHistory(MedicalHistoryFieldData history){
		if(history == null)
			return;
		
		if(this.history == null)
			this.history = new ArrayList<MedicalHistoryFieldData>();
		this.history.add(history);
	}
	
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setPatientId(dis.readInt());
		setHistory(SerializationUtils.read(dis,new MedicalHistoryFieldData().getClass()));
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getPatientId());
		SerializationUtils.write(getHistory(), dos);
	}
}
