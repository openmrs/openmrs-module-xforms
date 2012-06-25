/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.xforms.formentry;

import java.util.Hashtable;
import org.openmrs.util.FormConstants;

/**
 * Constants used by the formentry module
 */
public class FormEntryConstants {
	
	public static final String FORMENTRY_GP_DEFAULT_HL7_SOURCE = "formentry.default_hl7_source";
    public static final String FORMENTRY_DEFAULT_HL7_SOURCE_NAME = "local";

	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.FIELD_TYPE_CONCEPT
	 */
	public static final Integer FIELD_TYPE_CONCEPT = FormConstants.FIELD_TYPE_CONCEPT;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.FIELD_TYPE_DATABASE
	 */
	public static final Integer FIELD_TYPE_DATABASE = FormConstants.FIELD_TYPE_DATABASE;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.FIELD_TYPE_TERM_SET
	 */
	public static final Integer FIELD_TYPE_TERM_SET = FormConstants.FIELD_TYPE_TERM_SET;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.FIELD_TYPE_MISC_SET
	 */
	public static final Integer FIELD_TYPE_MISC_SET = FormConstants.FIELD_TYPE_MISC_SET;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.FIELD_TYPE_SECTION
	 */
	public static final Integer FIELD_TYPE_SECTION = FormConstants.FIELD_TYPE_SECTION;

	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.HL7_TEXT
	 */
	public static final String HL7_TEXT = FormConstants.HL7_TEXT;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.HL7_CODED
	 */
	public static final String HL7_CODED = FormConstants.HL7_CODED;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.HL7_CODED_WITH_EXCEPTIONS
	 */
	public static final String HL7_CODED_WITH_EXCEPTIONS = FormConstants.HL7_CODED_WITH_EXCEPTIONS;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.HL7_NUMERIC
	 */
	public static final String HL7_NUMERIC = FormConstants.HL7_NUMERIC;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.HL7_DATE
	 */
	public static final String HL7_DATE = FormConstants.HL7_DATE;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.HL7_TIME
	 */
	public static final String HL7_TIME = FormConstants.HL7_TIME;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.HL7_DATETIME
	 */
	public static final String HL7_DATETIME = FormConstants.HL7_DATETIME;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.HL7_BOOLEAN
	 */
	public static final String HL7_BOOLEAN = FormConstants.HL7_BOOLEAN;

	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.CLASS_DRUG
	 */
	public static final Integer CLASS_DRUG = FormConstants.CLASS_DRUG;

	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.HL7_LOCAL_CONCEPT
	 */
	public static final String HL7_LOCAL_CONCEPT = FormConstants.HL7_LOCAL_CONCEPT;
	
	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.HL7_LOCAL_DRUG
	 */
	public static final String HL7_LOCAL_DRUG = FormConstants.HL7_LOCAL_DRUG;

	// List of datatypes that do not require complex definitions
	public static final Hashtable<String, String> simpleDatatypes = new Hashtable<String, String>();
	static {
		simpleDatatypes.put(HL7_TEXT, "xs:string");
		simpleDatatypes.put(HL7_DATE, "xs:date");
		simpleDatatypes.put(HL7_TIME, "xs:time");
		simpleDatatypes.put(HL7_DATETIME, "xs:dateTime");

		// We make a special boolean type with an extra attribute
		// to get InfoPath to treat booleans properly
		simpleDatatypes.put(HL7_BOOLEAN, "_infopath_boolean");
		
		simpleDatatypes.put("ED", "xs:base64Binary");
		//simpleDatatypes.put(HL7_COMPLEX, "xs:base64Binary");
	}

	/**
	 * @deprecated Use org.openmrs.util.form.FormConstants.INDENT_SIZE
	 */
	public static final int INDENT_SIZE = FormConstants.INDENT_SIZE;

	/* FormEntry Queue baked-in prileges */
	public static final String PRIV_VIEW_FORMENTRY_QUEUE = "View FormEntry Queue";
	public static final String PRIV_ADD_FORMENTRY_QUEUE = "Add FormEntry Queue";
	public static final String PRIV_EDIT_FORMENTRY_QUEUE = "Edit FormEntry Queue";
	public static final String PRIV_DELETE_FORMENTRY_QUEUE = "Delete FormEntry Queue";
	public static final String PRIV_VIEW_FORMENTRY_ARCHIVE = "View FormEntry Archive";
	public static final String PRIV_ADD_FORMENTRY_ARCHIVE = "Add FormEntry Archive";
	public static final String PRIV_EDIT_FORMENTRY_ARCHIVE = "Edit FormEntry Archive";
	public static final String PRIV_DELETE_FORMENTRY_ARCHIVE = "Delete FormEntry Archive";
	public static final String PRIV_VIEW_FORMENTRY_ERROR = "View FormEntry Error";
	public static final String PRIV_ADD_FORMENTRY_ERROR = "Add FormEntry Error";
	public static final String PRIV_EDIT_FORMENTRY_ERROR = "Edit FormEntry Error";
	public static final String PRIV_DELETE_FORMENTRY_ERROR = "Delete FormEntry Error";
	public static final String PRIV_MANAGE_FORMENTRY_XSN = "Manage FormEntry XSN";
	public static final String PRIV_VIEW_UNPUBLISHED_FORMS = "View Unpublished Forms";
	
	/* FormEntry Queue status values for entries in the queue */
	public static final int FORMENTRY_QUEUE_STATUS_PENDING = 0;
	public static final int FORMENTRY_QUEUE_STATUS_PROCESSING = 1;
	public static final int FORMENTRY_QUEUE_STATUS_PROCESSED = 2;
	public static final int FORMENTRY_QUEUE_STATUS_ERROR = 3;

	/* Default name for InfoPath components */
	public static final String FORMENTRY_DEFAULT_SCHEMA_NAME = "FormEntry.xsd";
	public static final String FORMENTRY_DEFAULT_TEMPLATE_NAME = "template.xml";
	public static final String FORMENTRY_DEFAULT_SAMPLEDATA_NAME = "sampledata.xml";
	public static final String FORMENTRY_DEFAULT_DEFAULTS_NAME = "defaults.xml";
	public static final String FORMENTRY_DEFAULT_JSCRIPT_NAME = "openmrs-infopath.js";
	public static final String FORMENTRY_SERVER_URL_VARIABLE_NAME = "SERVER_URL";
	public static final String FORMENTRY_TASKPANE_URL_VARIABLE_NAME = "TASKPANE_URL";
	public static final String FORMENTRY_SUBMIT_URL_VARIABLE_NAME = "SUBMIT_URL";
	
	public static final String PRIV_FORM_ENTRY = "Form Entry";
	
	// These variables used to be non-final and editable by runtime properties.
	// Users should not need to modify these settings.
	public static final String FORMENTRY_INFOPATH_PUBLISH_PATH = "/moduleServlet/formentry/forms/";
	public static final String FORMENTRY_INFOPATH_SUBMIT_PATH = "/moduleServlet/formentry/formUpload";
	public static final String FORMENTRY_INFOPATH_TASKPANE_INITIAL_PATH = "/formTaskpane.htm";
	public static final String FORMENTRY_STARTER_XSN_FOLDER_PATH = "/org/openmrs/module/formentry/forms/starter/";
	
	// Global properties used in the formentry module
	public static final String FORMENTRY_GP_SERVER_URL = "formentry.infopath_server_url";
	public static final String FORMENTRY_GP_TASKPANE_KEEPALIVE = "formentry.infopath_taskpane_keepalive_min";
	
	public static final String FORMENTRY_GP_QUEUE_DIR = "formentry.queue_dir";
	public static final String FORMENTRY_GP_QUEUE_DIR_DEFAULT = "formentry/queue";
	public static final String FORMENTRY_GP_QUEUE_ARCHIVE_DIR = "formentry.queue_archive_dir";
	public static final String FORMENTRY_GP_QUEUE_ARCHIVE_DIR_DEFAULT = "formentry/archive/%Y/%M";
	
	/**
	 * @deprecated As of 2.6, xsns are stored in the formentry_xsn table
	 */
	public static final String FORMENTRY_GP_INFOPATH_OUTPUT_DIR = "formentry.infopath_output_dir";
	
	/**
	 * @deprecated As of 2.6, xsns are stored in the formentry_xsn table
	 */
	public static final String FORMENTRY_GP_INFOPATH_ARCHIVE_DIR = "formentry.infopath_archive_dir";
	
	/**
	 * @deprecated As of 2.6, xsns are stored in the formentry_xsn table
	 */
	public static final String FORMENTRY_GP_ARCHIVE_DATE_FORMAT = "formentry.infopath_archive_date_format";
	
	public static final String STARTUP_USERNAME = "formentry.startup_username";
	public static final String STARTUP_PASSWORD = "formentry.startup_password";

	// runtime properties for the cabextract and lcab locations
	// these are not global properties as they could be a security risk for demo sites
	public static final String FORMENTRY_RP_CABEXTRACT_LOCATION = "formentry.cabextract_location";
	public static final String FORMENTRY_RP_LCAB_LOCATION = "formentry.lcab_location";
	public static String FORMENTRY_CABEXTRACT_LOCATION = null; // value of the runtime property loaded at startup
	public static String FORMENTRY_LCAB_LOCATION = null; // value of the runtime property loaded at startup
	
}
