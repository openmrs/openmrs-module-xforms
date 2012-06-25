package org.openmrs.module.xforms.sms;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;

import org.fcitmuk.epihandy.Condition;
import org.fcitmuk.epihandy.EpihandyConstants;
import org.fcitmuk.epihandy.FormDef;
import org.fcitmuk.epihandy.OptionData;
import org.fcitmuk.epihandy.OptionDef;
import org.fcitmuk.epihandy.PageData;
import org.fcitmuk.epihandy.QuestionData;
import org.fcitmuk.epihandy.QuestionDef;
import org.fcitmuk.epihandy.SkipRule;
import org.fcitmuk.epihandy.ValidationRule;
import org.fcitmuk.epihandy.xform.EpihandyXform;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.util.FormUtil;


/**
 * Responsible for parsing form data submitted as text sms.
 * 
 * @author daniel
 *
 */
public class FormSmsParser {

	/** The seperator for different fields in the sms text. */
	private String FIELD_SEP_CHAR = "=";

	/** Determines if each sms is expected to contain a user name and password. */
	private boolean smsValidateNamePassword = true;

	/** Determines if we should accept only those phone numbers attached to user accounts. */
	private boolean smsValidatePhoneNo = false;

	private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	private org.fcitmuk.epihandy.FormData formData;
	private Xform xform;

	public FormSmsParser(){
		
	}

	public void init(){
		String val = Context.getAdministrationService().getGlobalProperty("xforms.smsFieldSepChar");
		if(val != null)
			FIELD_SEP_CHAR = val;

		val = Context.getAdministrationService().getGlobalProperty("xforms.smsValidateNamePassword");
		if("false".equalsIgnoreCase(val))
			smsValidateNamePassword = false;

		val = Context.getAdministrationService().getGlobalProperty("xforms.smsValidatePhoneNo");
		if("true".equalsIgnoreCase(val))
			smsValidatePhoneNo = true;

		//We some how need to get the user who tries to submt data and hence we need to atleast have one of these properties.
		if(smsValidateNamePassword == false && smsValidatePhoneNo == false)
			smsValidateNamePassword = true;
	}

	/**
	 * Creates a form data item from a text sms.
	 * sample sms= guyzb daniel123 newform 1=Daniel Kayiwa 2=67.8 3=m 4=1,3,4
	 * 
	 * @param sender
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public String sms2FormXml(String sender, String text) throws Exception{

		//Turned on for now to prevent requiring a restart of the sms server after a change in
		//settings or form definition. May introduce an unnecessary performance penalty.
		init();

		//First get off closing spaces if any.
		text = text.trim();

		//Authenticate the user
		text = authenticateUser(sender,text);

		//Create an epihandy form data object.
		text = initFormData(text);

		//Zero or more spaces, followed by one or more digits, followed by zero or more spaces, followed by equal sign
		String[] values = text.split("\\s*\\d+\\s*=");

		//Set the values from the sms
		List<String> errors = new ArrayList<String>();
		int pos,startindex = 0;
		for(int index = 1; index < values.length; index++){
			String value = values[index];
			pos = text.indexOf(value, text.indexOf(FIELD_SEP_CHAR, startindex)+1);
			String key = text.substring(startindex,pos);  //eg 1=,2=,3=,4=
			startindex = pos + value.length();

			QuestionData questionData = getQuestion(key,formData,errors);
			if(questionData != null)
				setQuestionAnswer(questionData,formData,value.trim(),errors);
		}

		//Turn off required attribute for patient id for the sake of thoses patients
		//whose ids are not known and hence we shall search using patient identifier.
		QuestionData patientIdQtn = formData.getQuestion(XformBuilder.BINDING_PATIENT_ID);
		if(patientIdQtn != null)
			patientIdQtn.getDef().setMandatory(false);

		//Start with skip logic because it can make some fields mandatory
		//hence giving validation rules a chance to also catch these
		//fields whose mandatority is conditional.
		Vector<QuestionData> ruleRequiredQtns = new Vector<QuestionData>();
		String errorMsgs = getSkipErrorMsg(formData,ruleRequiredQtns);

		errorMsgs = addErrorMsg(errorMsgs,getValidationErrorMsg(formData,ruleRequiredQtns));

		for(String error : errors)
			errorMsgs = addErrorMsg(errorMsgs, error);

		if(errorMsgs != null)
			throw new Exception(errorMsgs);

		Document doc = EpihandyXform.getDocument(new StringReader(xform.getXformXml()));
		fillPatientId(formData,doc);

		//Set the openmrs required form header values
		if(!XformBuilder.setNodeValue(doc, XformConstants.NODE_SESSION, new java.util.Date().toString()))
			throw new Exception("Form has no session node");

		if(!XformBuilder.setNodeValue(doc, XformConstants.NODE_UID, FormEntryWrapper.generateFormUid()))
			throw new Exception("Form has no uid node");

		if(!XformBuilder.setNodeValue(doc, XformConstants.NODE_DATE_ENTERED, FormUtil.dateToString(new java.util.Date())))
			throw new Exception("Form has no date_entered node");

		if(!XformBuilder.setNodeValue(doc, XformConstants.NODE_ENTERER, XformsUtil.getEnterer()))
			throw new Exception("Form has no enterer node");

		//Get the xform model xml that is filled with data.
		return EpihandyXform.updateXformModel(doc,formData);
	}

	private String authenticateUser(String sender, String text) throws Exception{

		int pos = 0;

		//if(smsValidateNamePassword){
		pos = text.indexOf(' ');
		if(pos < 0)
			throw new Exception("Expected space after username");

		String username = text.substring(0,pos).trim();

		text = text.substring(pos).trim();
		pos = text.indexOf(' ');
		if(pos < 0)
			throw new Exception("Expected space after password");
		String password = text.substring(0,pos).trim();

		try{
			Context.authenticate(username, password);
		}
		catch(Exception ex){
			throw new Exception("Access denied for user "+username);
		}

		//if(smsValidatePhoneNo && !sender.equals(user.getPhoneNo()))
		//	throw new Exception("User "+username+" is not registered for this phone number");
		/*}
		else{
			assert(smsValidatePhoneNo);// Both smsValidateNamePassword and smsValidatePhoneNo cant be false

			User user = Context.getFormDownloadService().getUserByPhoneNo(sender);
			if(user == null)
				throw new Exception("This phone number is not attached to any user account");

			Context.setAuthenticatedUser(user);
		}*/

		return text.substring(pos).trim();
	}

	private String initFormData(String text) throws Exception{

		//Get the form identifier.
		int pos = text.indexOf(' ');
		if(pos < 0)
			throw new Exception("Expected space after form identifier");

		FormDef formDef = null;
		String formid = text.substring(0,pos).trim();
		Integer formId = getFormId(formid);

		if(formId != null){
			xform = ((XformsService)Context.getService(XformsService.class)).getXform(formId);
			if(xform == null)
				throw new Exception("No xform found with id="+formid);
			formDef = EpihandyXform.fromXform2FormDef(new StringReader(xform.getXformXml()));
			/*formDef = EpihandyXform.fromXform2FormDef(new FileReader("testform.xml"));
			xform = new Xform();
			xform.setXformXml(XformsUtil.readFile("testform.xml"));*/
		}

		if(formDef == null)
			throw new Exception("No form found with id="+formid);

		formData = new org.fcitmuk.epihandy.FormData(formDef);

		return text.substring(pos);
	}

	private QuestionData getQuestion(String idtext,org.fcitmuk.epihandy.FormData formData,List<String> errors) throws Exception{
		/*String text = idtext.substring(0,idtext.indexOf('=')).trim();
		int id = Integer.parseInt(text);
		QuestionData questionData = formData.getQuestion((byte)id);
		if(questionData == null)
			throw new Exception("No form question found at position " + id);

		return questionData;*/
		
		String text = idtext.substring(0,idtext.indexOf('=')).trim();
		int id = Integer.parseInt(text);
		QuestionData questionData = formData.getQuestion((byte)id);
		if(questionData == null)
			errors.add("Form has not question at position "+id);
		return questionData;
	}

	private void setQuestionAnswer(QuestionData questionData, org.fcitmuk.epihandy.FormData formData, String answer,List<String> errors){
		//TODO May need to handle dynamic optiondef
		QuestionDef questionDef = questionData.getDef();
		if(questionDef.getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || questionDef.getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC){
			questionData.setAnswer(getOptionData(questionDef,answer,errors));
			formData.updateDynamicOptions(questionData,false);
		}
		else if(questionDef.getType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			Vector<OptionData> optionAnswers = new Vector<OptionData>();
			String values[] = answer.split(" ");
			for(int index = 0; index < values.length; index++)
				optionAnswers.addElement(getOptionData(questionDef,values[index],errors));
			questionData.setAnswer(optionAnswers);
		}
		else if(questionDef.getType() == QuestionDef.QTN_TYPE_BOOLEAN)
			questionData.setAnswer(answer);
		else
			questionData.setTextAnswer(answer);
	}

	private OptionData getOptionData(QuestionDef questionDef,String answer,List<String> errors){
		OptionDef optionDef = questionDef.getOptionWithValue(answer);
		if(optionDef == null){
			try{
				int val = Integer.parseInt(answer) - 1;
				if(val < questionDef.getOptions().size() && val >= 0)
					optionDef = (OptionDef)questionDef.getOptions().elementAt(val);
			}
			catch(Exception ex){}
		}

		if(optionDef == null){
			errors.add(answer + " is out of range for " + questionDef.getText());
			
			//Since we have an out of range error message,we do not need to also report the 
			//required error, hence making the error report sms as small as possible.
			//This will not result into bugs only on condition that on each sms received,
			//a new formdef is constructed.
			questionDef.setMandatory(false);
			
			return null;
		}

		return new OptionData(optionDef);
	}

	/**
	 * Get the validation error messages in a filled form.
	 * 
	 * @param formData the form data to validate.
	 * @param ruleRequiredQtns a list of questions which become required after a firing of some rules.
	 * @return a comma separated list of validation error messages if any, else null.
	 */
	private String getValidationErrorMsg(org.fcitmuk.epihandy.FormData formData,Vector<QuestionData> ruleRequiredQtns){
		String sErrors = null;

		//Check if form has any questions.
		Vector<PageData> pages = formData.getPages();
		if(pages == null || pages.size() == 0){
			sErrors = "Form has no questins";
			return sErrors;
		}

		//First get error messages for required fields which have not been answered
		//and answers not allowed for the data type.
		for(byte i=0; i<pages.size(); i++){
			PageData page = (PageData)pages.elementAt(i);
			for(byte j=0; j<page.getQuestions().size(); j++){
				QuestionData qtn = (QuestionData)page.getQuestions().elementAt(j);
				if(!ruleRequiredQtns.contains(qtn) && !qtn.isValid())
					sErrors = addErrorMsg(sErrors,"An answer is required for "+qtn.getDef().getText());

				//Do data type validation
				String msg = getTypeErrorMsg(qtn);
				if(msg != null)
					sErrors = addErrorMsg(sErrors,msg);
			}
		}

		//Check if form has any validation rules.
		Vector<ValidationRule> rules = formData.getDef().getValidationRules();
		if(rules == null)
			return sErrors;

		//Deal with the user supplied validation rules
		for(int index = 0; index < rules.size(); index++){
			ValidationRule rule = (ValidationRule)rules.elementAt(index);
			rule.setFormData(formData);
			if(!rule.isValid())
				sErrors = addErrorMsg(sErrors,rule.getErrorMessage());
		}

		return sErrors;
	}

	private String addErrorMsg(String errorMsgs, String msg){
		if(msg != null){
			if(errorMsgs == null)
				errorMsgs = "";
			else
				errorMsgs += ", ";
			errorMsgs += msg;
		}

		return errorMsgs;
	}

	private String getTypeErrorMsg(QuestionData questionData){
		if(questionData.getAnswer() == null)
			return null;

		QuestionDef questionDef = questionData.getDef();
		switch(questionDef.getType()){
		case QuestionDef.QTN_TYPE_BOOLEAN:
			if(questionData.getAnswer() == null)
				return null;
			else if("1".equals(questionData.getAnswer()) || "yes".equals(questionData.getAnswer()) || "y".equals(questionData.getAnswer())){
				questionData.setTextAnswer(QuestionData.TRUE_VALUE);
				return null;
			}
			else if("2".equals(questionData.getAnswer()) || "no".equals(questionData.getAnswer()) || "n".equals(questionData.getAnswer())){
				questionData.setTextAnswer(QuestionData.FALSE_VALUE);
				return null;
			}
			else
				return questionData.getAnswer() + " " + questionDef.getText() + " should be in list {1,2,y,n,yes,no}";
		case QuestionDef.QTN_TYPE_TEXT:
			return null;
		case QuestionDef.QTN_TYPE_DATE:
			try{
				Date date = null;
				if(questionData.isDateFunction(questionData.getAnswer()))
					date = new Date();
				else
					date = XformsUtil.fromDisplayString2Date(questionData.getAnswer().toString());
				questionData.setTextAnswer(XformsUtil.fromDate2SubmitString(date));
				return null;
			}
			catch(Exception ex){
				return questionData.getAnswer() + " " + questionDef.getText() + " should be a date";
			}
		case QuestionDef.QTN_TYPE_DATE_TIME:
			try{
				Date date = null;
				if(questionData.isDateFunction(questionData.getAnswer()))
					date = new Date();
				else
					date = XformsUtil.fromDisplayString2DateTime(questionData.getAnswer().toString());
				questionData.setTextAnswer(XformsUtil.fromDateTime2SubmitString(date));
				return null;
			}
			catch(Exception ex){
				return questionData.getAnswer() + " " + questionDef.getText() + " should be a date and time";
			}
		case QuestionDef.QTN_TYPE_TIME:
			try{
				Date date = null;
				if(questionData.isDateFunction(questionData.getAnswer()))
					date = new Date();
				else
					date = XformsUtil.fromDisplayString2Time(questionData.getAnswer().toString());
				questionData.setTextAnswer(XformsUtil.fromTime2SubmitString(date));
				return null;
			}
			catch(Exception ex){
				return questionData.getAnswer() + " " + questionDef.getText() + " should be time";
			}
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
			return null;
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
			return null;
		case QuestionDef.QTN_TYPE_DECIMAL:
		case QuestionDef.QTN_TYPE_NUMERIC:
			try{
				Double.parseDouble(questionData.getAnswer().toString());
				return null;
			}
			catch(Exception ex){
				return questionData.getAnswer() + " " + questionDef.getText() + " should be a number";
			}
		}

		return null;
	}

	private Integer getFormId(String id){
		try{
			return Integer.parseInt(id);
		}catch(Exception ex){}

		return null;
	}

	/**
	 * Get the skip logic error messages in a filled form. (eg pregnant males)
	 * 
	 * @param formData the form data to validate.
	 * @param ruleRequiredQtns a list of questions which become required after rule firing.
	 * @return a comma separated list of validation error messages if any, else null.
	 */
	private String getSkipErrorMsg(org.fcitmuk.epihandy.FormData formData,Vector<QuestionData> ruleRequiredQtns){
		String sErrors = null;

		//Check if form has any questions.
		Vector<PageData> pages = formData.getPages();
		if(pages == null || pages.size() == 0){
			sErrors = "Form has no questins";
			return sErrors;
		}

		//Check if form has any skip rules.
		Vector<SkipRule> rules = formData.getDef().getSkipRules();
		if(rules == null)
			return sErrors;

		//Deal with the user supplied validation rules
		for(int index = 0; index < rules.size(); index++){
			SkipRule rule = (SkipRule)rules.elementAt(index);
			Vector<QuestionData> answeredQtns = getAnsweredQuestions(formData,rule.getActionTargets());

			rule.fire(formData);

			//Get the question text of the first condition. This could be imporved with a user supplied skip logic error message, in future.
			String qtnText = formData.getQuestion(((Condition)rule.getConditions().elementAt(0)).getQuestionId()).getText();

			boolean mandatoryRule = (rule.getAction() & EpihandyConstants.ACTION_MAKE_MANDATORY) != 0;

			Vector<Byte> ids = rule.getActionTargets();
			for(byte i=0; i<ids.size(); i++){
				QuestionData questionData = formData.getQuestion(Byte.parseByte(ids.elementAt(i).toString()));

				//Check if the user answered a question they were supposed to skip.
				if(!questionData.isAnswered() && answeredQtns.contains(questionData))
					sErrors = addErrorMsg(sErrors,"Due the answer to " + qtnText + ", no answer is expected for "+questionData.getDef().getText());

				//Check is the user has not answered a question which has become required after an answer to some other question.
				if(mandatoryRule && questionData.getDef().isMandatory() && !questionData.isAnswered())
					sErrors = addErrorMsg(sErrors,"Due the answer to " + qtnText + ", an answer is required for "+questionData.getDef().getText());

				if(mandatoryRule)
					ruleRequiredQtns.add(questionData);
			}
		}

		return sErrors;
	}

	private Vector<QuestionData> getAnsweredQuestions(org.fcitmuk.epihandy.FormData formData, Vector<Byte> ids){
		Vector<QuestionData> qtns = new Vector<QuestionData>();

		for(byte i=0; i<ids.size(); i++){
			QuestionData questionData = formData.getQuestion(Byte.parseByte(ids.elementAt(i).toString()));
			if(questionData.isAnswered())
				qtns.add(questionData);
		}

		return qtns;
	}

	private void fillPatientId(org.fcitmuk.epihandy.FormData formData, Document doc) throws Exception{
		//Get the patientId question
		QuestionData patientIdQtn = formData.getQuestion(XformBuilder.BINDING_PATIENT_ID);
		if(patientIdQtn == null)
			throw new Exception("Form has no patientId field");
		//Check if patientId already filled by the user.
		String patientId = patientIdQtn.getValueAnswer();
		if(patientId != null){
			if(!patientExists(patientId.toString()))
				throw new Exception("No patient found with patientId "+patientId.toString());
			return; 
		}

		//Patient Identifier
		QuestionData identifierQtn = getPatientIdentifierQtn(formData,doc);
		if(identifierQtn == null)
			throw new Exception("Form has no patient identifier field");
		//Check if patient identifier is filled.
		String patientIdentifier = identifierQtn.getValueAnswer();
		if(patientIdentifier == null)
			throw new Exception("Expected patient identifier value");

		List<Patient> patients = Context.getPatientService().getPatients(null, patientIdentifier, null);
		if(patients != null && patients.size() > 1)
			throw new Exception("More than one patient was found with identifier " + patientIdentifier);

		if(patients != null && patients.size() == 1){
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_PATIENT_ID, patients.get(0).getPatientId().toString());
			return;
		}

		//Identifier Type
		QuestionData identifierTypeQtn = formData.getQuestion(XformBuilder.BINDING_IDENTIFIER_TYPE);
		if(identifierTypeQtn == null)
			throw new Exception("Form has no patient identifier type field");
		//Check if patient identifier type is filled
		String identifierType = identifierTypeQtn.getValueAnswer();
		if(identifierType == null)
			throw new Exception("Expected patient identifier type value");


		//Family Name
		QuestionData familyNameQtn = formData.getQuestion(XformBuilder.BINDING_FAMILY_NAME);
		if(familyNameQtn == null)
			throw new Exception("Form has no family name field");
		//Check if family name is filled.
		String familyName = familyNameQtn.getValueAnswer();
		if(familyName == null)
			throw new Exception("Expected patient family name value");

		//Gender
		QuestionData genderQtn = formData.getQuestion(XformBuilder.BINDING_GENDER);
		if(genderQtn == null)
			throw new Exception("Form has no gender field");
		//Check if gender is filled
		String gender = genderQtn.getValueAnswer();
		if(gender == null)
			throw new Exception("Expected patient gender value");

		//Birth Date
		QuestionData birthDateQtn = formData.getQuestion(XformBuilder.BINDING_BIRTH_DATE);
		if(birthDateQtn == null)
			throw new Exception("Form has no birth date field");
		//Check if birth date is filled
		Object birthDate = birthDateQtn.getAnswer();
		if(birthDate == null)
			throw new Exception("Expected patient birth date value");


		QuestionData givenNameQtn = formData.getQuestion(XformBuilder.BINDING_GIVEN_NAME);
		QuestionData midleNameQtn = formData.getQuestion(XformBuilder.BINDING_MIDDLE_NAME);

		Patient patient = new Patient();
		patient.setCreator(Context.getAuthenticatedUser());
		patient.setDateCreated(new Date());	
		patient.setGender(gender);

		PersonName pn = new PersonName();

		pn.setFamilyName(familyName);

		if(givenNameQtn.getAnswer() != null)
			pn.setGivenName(givenNameQtn.getValueAnswer());

		if(midleNameQtn.getAnswer() != null)
			pn.setMiddleName(midleNameQtn.getValueAnswer());

		pn.setCreator(patient.getCreator());
		pn.setDateCreated(patient.getDateCreated());
		patient.addName(pn);

		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setCreator(patient.getCreator());
		identifier.setDateCreated(patient.getDateCreated());
		identifier.setIdentifier(patientIdentifier.toString());

		int id = Integer.parseInt(identifierType);
		PatientIdentifierType idtfType = Context.getPatientService().getPatientIdentifierType(id);

		identifier.setIdentifierType(idtfType);
		identifier.setLocation(getLocation(formData));
		patient.addIdentifier(identifier);

		patient.setBirthdate(XformsUtil.fromSubmitString2Date(birthDate.toString()));

		Context.getPatientService().savePatient(patient);
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_PATIENT_ID, patient.getPatientId().toString());
	}

	private boolean patientExists(String patientid){

		try{
			return Context.getPatientService().getPatient(Integer.parseInt(patientid)) != null;
		}catch(Exception ex){}

		return false;
	}

	private QuestionData getPatientIdentifierQtn(org.fcitmuk.epihandy.FormData formData,Document doc){
		Element patientNode = XformBuilder.getElement(doc.getRootElement(), "patient");
		if(patientNode == null)
			return null;

		String binding = null;

		for(int i=0; i<patientNode.getChildCount(); i++){
			if(patientNode.getType(i) != Element.ELEMENT)
				continue;

			Element child = (Element)patientNode.getChild(i);
			if("patient_identifier".equalsIgnoreCase(child.getAttributeValue(null, "openmrs_table")) &&
					"identifier".equalsIgnoreCase(child.getAttributeValue(null, "openmrs_attribute"))){
				binding  = "/form/patient/" + child.getName();
				break;
			}
			//identifier
		}

		if(binding != null)
			return formData.getQuestion(binding);

		return null;
	}

	private Location getLocation(org.fcitmuk.epihandy.FormData formData) throws Exception{
		//Get the patientId question
		QuestionData locationIdQtn = formData.getQuestion(XformBuilder.BINDING_LOCATION_ID);
		if(locationIdQtn == null)
			throw new Exception("Form has no locationId field");
		//Check if locationId already filled by the user.
		String locationId = locationIdQtn.getValueAnswer();
		if(locationId == null)
			throw new Exception("Expected patient locationId value");

		try{
			Location location = Context.getLocationService().getLocation(Integer.parseInt(locationId));
			if(location != null)
				return location;
		}
		catch(Exception ex){}

		throw new Exception("Invalid location " + locationId);
	}

}
