package org.openmrs.module.xforms.formentry;

/**
 * Represents often fatal errors that occur within the form entry module
 * 
 */
public class FormEntryException extends RuntimeException {

	public static final long serialVersionUID = 121212344443122L;

	public FormEntryException() {
	}

	public FormEntryException(String message) {
		super(message);
	}

	public FormEntryException(String message, Throwable cause) {
		super(message, cause);
	}

	public FormEntryException(Throwable cause) {
		super(cause);
	}

}
