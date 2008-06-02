package org.openmrs.module.xforms;

/**
 * This class holds constants used in more than one class in the xforms module.
 * 
 * @author Daniel
 *
 */
public class XformConstants {
	//TODO More constants need to be put in this class from the various classes where they are scattered.
	
	/** 
	 * The dirrectory where to put xforms that are not submitted to the formentry queue
	 * because of errors.
	 */
	public static final String XFORMS_ERROR_DIR = "xforms.error_dir";
	
	/** The default xforms error dirrectory. */
	public static final String XFORMS_ERROR_DIR_DEFAULT = "xforms/error";
	
	/** The dirrectory for queuing xforms before they are processed. */
	public static final String XFORMS_QUEUE_DIR = "xforms.queue_dir";
	
	/** The default xforms queue dirrectory. */
	public static final String XFORMS_QUEUE_DIR_DEFAULT = "xforms/queue";
	
	/** 
	 * The dirrectory for archiving xforms after submission to the formentry queue.
	 * The reason for archiving xforms, even after knowing that the formentry module
	 * will also archive them is that, some processing is done on these xforms to make
	 * them consumable by the formentry module, and we want users to always be able to
	 * see how the xform looked like at submission, just incase of any issues or bugs
	 * in the xforms processing.
	 */
	public static final String XFORMS_ARCHIVE_DIR = "xforms.archive_dir";
	
	/** The default xforms archive dirrectory. */
	public static final String XFORMS_ARCHIVE_DIR_DEFAULT = "xforms/archive/%Y/%M";

	/** 
	 * XForms are linked to their correponding openmrs forms by the form_id field. 
	 * For clients to create a new patient on say mobile xform clients, the new patient
	 * is also created as an xform which has to be differentiated from the rest of 
	 * the forms. So as for now, we user this form_id on the assumption that no
	 * openmrs form will have it. This may not be the best way for handling this,
	 * and hence may need further thoughts, but works for now.
	 */
	public static final int PATIENT_XFORM_FORM_ID = 0;
	
	/** The default character encoding used when writting and reading bytes to and from streams. */
	public static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
	
	/** The default date format. */
	public static final String DEFAULT_DATE_FORMAT = "dd-mm-yyyy";//yyyy-mm-dd
	
	/** The default value for rejecting forms for patients considered new when they already exist, by virture of patient identifier. */
	public static final String DEFAULT_REJECT_EXIST_PATIENT_CREATE = "true";
	
	/** The global property key for rejecting forms for patients considered new when they already exist, by virture of patient identifier. */
	public static final String GLOBAL_PROP_KEY_REJECT_EXIST_PATIENT_CREATE = "xforms.rejectExistingPatientCreation";
	
	/** The global property key for the date format.*/
	public static final String GLOBAL_PROP_KEY_DATE_FORMAT = "xforms.dateFormat";
	
	/** The global property key for the user serializer class.*/
	public static final String GLOBAL_PROP_KEY_USER_SERIALIZER= "xforms.userSerializer";
		
	/** The global property key for the patient serializer class.*/
	public static final String GLOBAL_PROP_KEY_PATIENT_SERIALIZER = "xforms.patientSerializer";
    
    /** The global property key for the cohort serializer class.*/
    public static final String GLOBAL_PROP_KEY_COHORT_SERIALIZER = "xforms.cohortSerializer";
	
	/** The global property key for the xform serializer class.*/
	public static final String GLOBAL_PROP_KEY_XFORM_SERIALIZER = "xforms.xformSerializer";
	
	/** The global property key for the xform select1 appearance. */
	public static final String GLOBAL_PROP_KEY_SINGLE_SELECT_APPEARANCE = "xforms.singleSelectAppearance";
	
	/** The global property key for determining whether to use stored xforms or build new ones on the fly. */
	public static final String GLOBAL_PROP_KEY_USER_STORED_XFORMS = "xforms.useStoredXform";
	
	/** The global property key for determining whether to include users when downloading xforms. */
	public static final String GLOBAL_PROP_KEY_INCLUDE_USERS_IN_XFORMS_DOWNLOAD = "xforms.includeUsersInXformsDownload";

	/** The global property key for the patient download cohort.*/
	public static final String GLOBAL_PROP_KEY_PATIENT_DOWNLOAD_COHORT = "xforms.patientDownloadCohort";

	/** The default value for the user serializer class.*/
	public static final String DEFAULT_USER_SERIALIZER= "org.openmrs.module.xforms.DefaultUserSerializer";
	
	/** The default value for the patient serializer class.*/
	public static final String DEFAULT_PATIENT_SERIALIZER = "org.openmrs.module.xforms.DefaultPatientSerializer";
	
    /** The default value for the cohort serializer class.*/
    public static final String DEFAULT_COHORT_SERIALIZER = "org.openmrs.module.xforms.DefaultCohortSerializer";

	/** The default value for the xform serializer class.*/
	public static final String DEFAULT_XFORM_SERIALIZER = "org.openmrs.module.xforms.DefaultXformSerializer";
	
	/** The session form node. */
	public static final String NODE_SESSION = "session";
	
	/** The uid form node. */
	public static final String NODE_UID = "uid";
	
	/** The date_entered form node. */
	public static final String NODE_DATE_ENTERED = "date_entered";
	
	/** The enterer form node. */
	public static final String NODE_ENTERER = "enterer";
	
	/** The extension for our xforms. */
	public static final String XFORM_FILE_EXTENSION = ".xml";
	
	/** The extension for xslt documents. */
	public static final String XSLT_FILE_EXTENSION = ".xml";
	
	/** The extension for xml files. */
	public static final String XML_FILE_EXTENSION = ".xml";
	
	/** The content disposition http header. */
	public static final String HTTP_HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	
	/** The content type http header. */
	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
	
	/** The starter xform file name. */
	public static final String STARTER_XFORM = "starter_xform.xml";
	
	/** The starter xform file name. */
	public static final String STARTER_XSLT = "starter_xslt.xml";
	
	/** The empty string constant. */
	public static final String EMPTY_STRING = "";
	
	public static final String HTTP_HEADER_CONTENT_DISPOSITION_VALUE = "attachment; filename=";
	
	/** The application/xhtml+xml http content type. */
	public static final String HTTP_HEADER_CONTENT_TYPE_XHTML_XML = "application/xhtml+xml; charset=utf-8";
	
	/** The text value for boolean true. */
	public static final String TRUE_TEXT_VALUE = "true";
	
	/** The text value for boolean false. */
	public static final String FALSE_TEXT_VALUE = "false";
	
	/** The patientId request parameter. */
	public static final String REQUEST_PARAM_PATIENT_ID = "patientId";
	
	/** The formId request parameter. */
	public static final String REQUEST_PARAM_FORM_ID = "formId";
	
	/** The batchEntry request parameter. */
	public static final String REQUEST_PARAM_BATCH_ENTRY = "batchEntry";
	
	/** The xformentry request parameter. */
	public static final String REQUEST_PARAM_XFORM_ENTRY = "xformentry";
	
	/** The phrase request parameter. */
	public static final String REQUEST_PARAM_PATIENT_SEARCH_PHRASE = "phrase";
	
	/** The xforms request parameter. */
	public static final String REQUEST_PARAM_XFORMS = "xforms";
	
	/** The xform request parameter. */
	public static final String REQUEST_PARAM_XFORM = "xform";
	
	/** The xslt request parameter. */
	public static final String REQUEST_PARAM_XSLT = "xslt";
		
	/** The target request parameter. */
	public static final String REQUEST_PARAM_TARGET = "target";
	
	/** The cohortId request parameter. */
	public static final String REQUEST_PARAM_INCLUDE_USERS = "includeUsers";
	
	/** The cohortId request parameter. */
	public static final String REQUEST_PARAM_COHORT_ID = "cohortId";
	
	/** The downloadPatients request parameter. */
	public static final String REQUEST_PARAM_DOWNLOAD_PATIENTS = "downloadPatients";
    
    /** The downloadCohorts request parameter. */
    public static final String REQUEST_PARAM_DOWNLOAD_COHORTS = "downloadCohorts";
	
	/** The setCohort request parameter. */
	public static final String REQUEST_PARAM_SET_COHORT = "setCohort";
	
	/** The uploadPatientXform request parameter. */
	public static final String REQUEST_PARAM_UPLOAD_PATIENT_XFORM = "uploadPatientXform";
	
	/** The patientXformFile request parameter. */
	public static final String REQUEST_PARAM_PATIENT_XFORM_FILE = "patientXformFile";
	
	/** The downloadPatientXform request parameter. */
	public static final String REQUEST_PARAM_DOWNLOAD_PATIENT_XFORM = "downloadPatientXform";
	
	/** The relative url for xforms data uploads. */
	public static final String XFORM_DATA_UPLOAD_RELATIVE_URL = "/moduleServlet/xforms/xformDataUpload";
}
