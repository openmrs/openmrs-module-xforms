package org.openmrs.module.xforms;

/**
 * Represents often fatal errors that occur within the xforms module
 * 
 */
public class XformsException extends RuntimeException {

	public static final long serialVersionUID = 121212344443789L;

	public XformsException() {
	}

	public XformsException(String message) {
		super(message);
	}

	public XformsException(String message, Throwable cause) {
		super(message, cause);
	}

	public XformsException(Throwable cause) {
		super(cause);
	}

}
