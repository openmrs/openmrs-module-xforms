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
	
	/** The global property key for the user serializer class.*/
	public static final String GLOBAL_PROP_KEY_USER_SERIALIZER= "xforms.userSerializer";
	
	/** The global property key for the patient serializer class.*/
	public static final String GLOBAL_PROP_KEY_PATIENT_SERIALIZER = "xforms.patientSerializer";
	
	/** The global property key for the xform serializer class.*/
	public static final String GLOBAL_PROP_KEY_XFORM_SERIALIZER = "xforms.xformSerializer";
	
	/** The default value for the user serializer class.*/
	public static final String DEFAULT_USER_SERIALIZER= "org.openmrs.module.xforms.DefaultUserSerializer";
	
	/** The default value for the patient serializer class.*/
	public static final String DEFAULT_PATIENT_SERIALIZER = "org.openmrs.module.xforms.DefaultPatientSerializer";
	
	/** The default value for the xform serializer class.*/
	public static final String DEFAULT_XFORM_SERIALIZER = "org.openmrs.module.xforms.DefaultXformSerializer";
	
	public static final String NODE_SESSION = "session";
	public static final String NODE_UID = "uid";
	public static final String NODE_DATE_ENTERED = "date_entered";
	public static final String NODE_ENTERER = "enterer";
}
