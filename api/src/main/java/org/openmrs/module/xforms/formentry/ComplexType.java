/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.formentry;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.openmrs.Field;
import org.openmrs.FormField;
import org.openmrs.util.FormUtil;

public class ComplexType {
	Map<Integer, TreeSet<FormField>> formStructure;
	FormField formField;
	String token;
	boolean rendered = false;

	ComplexType(Map<Integer, TreeSet<FormField>> formStructure,
			FormField formField) {
		this(formStructure, formField, null);
	}

	ComplexType(Map<Integer, TreeSet<FormField>> formStructure,
			FormField formField, String token) {
		this.formStructure = formStructure;
		this.formField = formField;
		this.token = token;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ComplexType) {
			ComplexType ct = (ComplexType) obj;
			return equivalent(this.formField, ct.formField);
		}
		return false;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public boolean isRendered() {
		return rendered;
	}

	public Field getField() {
		return formField.getField();
	}

	public boolean isRequired() {
		return formField.isRequired();
	}

	public int hashCode() {
		return this.formField.getField().getFieldId();
	}

	// true if form fields are structurally equivalent in schema (can share
	// a type definition)
	public boolean equivalent(FormField a, FormField b) {
		if (a.equals(b))
			return true;
		if (!a.getField().getFieldId().equals(b.getField().getFieldId())
				|| a.isRequired() != b.isRequired())
			return false;
		TreeSet<FormField> aBranch = formStructure.get(a.getFormFieldId());
		TreeSet<FormField> bBranch = formStructure.get(b.getFormFieldId());
		if (aBranch == null || bBranch == null)
			return (aBranch == bBranch);
		Iterator aIter = aBranch.iterator();
		Iterator bIter = bBranch.iterator();
		// modified by CA on 2006-12-12 - found situations where this was throwing NoSuchElementException because bIter had no "next" element
		while (aIter.hasNext() && bIter.hasNext()) {
			FormField aFormField = (FormField) aIter.next();
			FormField bFormField = (FormField) bIter.next();
			if (!equivalent(aFormField, bFormField))
				return false;
		}
		return true;
	}

	public static ComplexType getComplexType(
			Map<Integer, TreeSet<FormField>> formStructure,
			Vector<ComplexType> list, FormField formField, String token,
			Vector<String> tagList) {
		ComplexType ct = new ComplexType(formStructure, formField);
		int index = list.indexOf(ct);
		if (index >= 0)
			return list.get(index);
		ct.setToken(FormUtil.getNewTag(token, tagList));
		list.add(ct);
		return ct;
	}
}
