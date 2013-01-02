package org.openmrs.module.xforms;

import org.openmrs.patient.impl.LuhnIdentifierValidator;

public class DanielLuhnIdentifierValidator extends LuhnIdentifierValidator {

	/**
	 * @see org.openmrs.patient.IdentifierValidator#getName()
	 */
	@Override
	public String getName() {
		return "Testing Kayiwa Daniel";
	}
}
