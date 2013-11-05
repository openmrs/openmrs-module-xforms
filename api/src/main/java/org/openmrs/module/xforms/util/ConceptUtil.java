package org.openmrs.module.xforms.util;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;

public class ConceptUtil {
	
	public ConceptName getConceptName(String conceptId) {
		if (StringUtils.isBlank(conceptId)) {
			return null;
		}
		
		Concept concept = Context.getConceptService().getConcept(conceptId);
		if (concept == null) {
			return null;
		}
		
		return concept.getName();
	}
	
	public String getName(String conceptId) {
		ConceptName conceptName = getConceptName(conceptId);
		
		if (conceptName != null) {
			return conceptName.getName();
		}
		
		return null;
	}
}
