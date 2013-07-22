package org.openmrs.module.xforms.formentry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.User;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Source;
import org.openmrs.module.xforms.BasicFormBuilder;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * These routines have been copied from the formentry module to remove the dependency of the xforms
 * module on the formentry module.
 * 
 * @author daniel
 */
public class FormEntryQueueProcessor {
	
	private static final Log log = LogFactory.getLog(FormEntryQueueProcessor.class);
	
	private DocumentBuilderFactory documentBuilderFactory;
	
	private XPathFactory xPathFactory;
	
	private TransformerFactory transformerFactory;
	
	/**
	 * Transform a FormEntryQueue entry (converts the XML data into HL7 and places it into the HL7
	 * inbound queue for further processing). Once transformed, then FormEntryQueue entry is flagged
	 * as completed (the status is updated). The XSLT from the appropriate form (the form used to
	 * generate the FormEntryQueue data in the first place) is used to perform the transformation
	 * into HL7.
	 * 
	 * @param formEntryQueue entry to be transformed
	 * @should transform xml data with a serialized complex obs 
	 */
	public HL7InQueue transformFormEntryQueue(FormEntryQueue formEntryQueue, boolean propagateErrors) throws Exception {
		log.debug("Transforming form entry queue");
		String formData = formEntryQueue.getFormData();
		FormService formService = Context.getFormService();
		Integer formId = null;
		String hl7SourceKey = null;
		HL7Source hl7Source = null;
		String errorDetails = null;
		
		// First we parse the FormEntry xml data to obtain the formId of the
		// form that was used to create the xml data
		try {
			DocumentBuilderFactory dbf = getDocumentBuilderFactory();
			DocumentBuilder db = dbf.newDocumentBuilder();
			XPathFactory xpf = getXPathFactory();
			XPath xp = xpf.newXPath();
			Document doc = db.parse(new InputSource(new StringReader(formData)));
			formId = Integer.parseInt(xp.evaluate("/form/@id", doc));
			hl7SourceKey = xp.evaluate("/form/header/uid", doc);
		}
		catch (Exception e) {
			errorDetails = e.getMessage();
			log.error("Error while parsing formentry (" + formEntryQueue.getFormEntryQueueId() + ")", e);
			setFatalError(formEntryQueue, "Error while parsing the formentry xml", errorDetails, propagateErrors);
			
			if (propagateErrors)
				throw e;
		}
		
		// If we failed to obtain the formId, move the queue entry into the
		// error bin and abort
		if (formId == null) {
			setFatalError(formEntryQueue, "Error retrieving form ID from data", errorDetails, propagateErrors);
			
			if (propagateErrors)
				throw new Exception("Error retrieving form ID from data");
			
			return null;
		}
		
		// If we can't get a form object for this formId, throw this to the error bin
		Form form = formService.getForm(formId);
		if (form == null) {
			setFatalError(formEntryQueue, "The form id: " + formId + " does not exist in the form table!", errorDetails,
			    propagateErrors);
			
			if (propagateErrors)
				throw new Exception("The form id: " + formId + "does not exist in the form table!");
			
			return null;
		}
		
		// Get the HL7 source based on the form's encounter type
		hl7Source = Context.getHL7Service().getHL7SourceByName(
		    Context.getAdministrationService().getGlobalProperty(FormEntryConstants.FORMENTRY_GP_DEFAULT_HL7_SOURCE,
		        FormEntryConstants.FORMENTRY_DEFAULT_HL7_SOURCE_NAME));
		
		// If source key not provided, use FormEntryQueue.formEntryQueueId
		if (hl7SourceKey == null || hl7SourceKey.length() < 1)
			hl7SourceKey = String.valueOf(formEntryQueue.getFormEntryQueueId());
		
		// Now that we've determined the form used to create the XML data,
		// we can obtain the associated XSLT to perform the transform to HL7.
		String xsltDoc = null;
		
		try {
			xsltDoc = form.getXslt();
		}
		catch (UnsupportedOperationException ex) {
			//The service method will invoke the appropriate logic according to the openmrs version
			xsltDoc = Context.getService(XformsService.class).getXslt(formId);
		}
		
		if (StringUtils.isBlank(xsltDoc))
			xsltDoc = BasicFormBuilder.getFormXslt();
		
		StringWriter outWriter = new StringWriter();
		Source source = new StreamSource(new StringReader(formData), XformConstants.DEFAULT_CHARACTER_ENCODING);
		Source xslt = new StreamSource(IOUtils.toInputStream(xsltDoc));
		Result result = new StreamResult(outWriter);
		
		TransformerFactory tf = getTransformerFactory();
		String out = null;
		errorDetails = null;
		try {
			Transformer t = tf.newTransformer(xslt);
			t.transform(source, result);
			out = outWriter.toString();
		}
		catch (TransformerConfigurationException e) {
			errorDetails = e.getMessage();
			log.error(errorDetails, e);
			
			if (propagateErrors)
				throw e;
			
		}
		catch (TransformerException e) {
			errorDetails = e.getMessage();
			log.error(errorDetails, e);
			
			if (propagateErrors)
				throw e;
		}
		
		// If the transform failed, move the queue entry into the error bin
		// and exit
		if (out == null) {
			setFatalError(formEntryQueue, "Unable to transform to HL7", errorDetails, propagateErrors);
			
			if (propagateErrors)
				throw new Exception("Unable to transform to HL7");
			
			return null;
		}
		
		// At this point, we have successfully transformed the XML data into
		// HL7. Create a new entry in the HL7 inbound queue and move the
		// current FormEntry queue item into the archive.
		HL7InQueue hl7InQueue = new HL7InQueue();
		hl7InQueue.setHL7Data(out.toString());
		hl7InQueue.setHL7Source(/*hl7Source*/Context.getHL7Service().getHL7Source(1));
		hl7InQueue.setHL7SourceKey(hl7SourceKey);
		//Context.getHL7Service().saveHL7InQueue(hl7InQueue);
		
		//Move the current FormEntry queue item into the archive.
		FormEntryArchive formEntryArchive = new FormEntryArchive(formEntryQueue);
		createFormEntryArchive(formEntryArchive);
		deleteFormEntryQueue(formEntryQueue);
		
		// clean up memory
		garbageCollect();
		
		return hl7InQueue;
	}
	
	/**
	 * @return DocumentBuilderFactory to be used for parsing XML
	 */
	private DocumentBuilderFactory getDocumentBuilderFactory() {
		if (documentBuilderFactory == null)
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
		return documentBuilderFactory;
	}
	
	/**
	 * @return XPathFactory to be used for obtaining data from the parsed XML
	 */
	private XPathFactory getXPathFactory() {
		if (xPathFactory == null)
			xPathFactory = XPathFactory.newInstance();
		return xPathFactory;
	}
	
	/**
	 * @return TransformerFactory used to perform the transform to HL7
	 */
	private TransformerFactory getTransformerFactory() {
		if (transformerFactory == null) {
			System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
			transformerFactory = TransformerFactory.newInstance();
		}
		return transformerFactory;
	}
	
	/**
	 * Convenience method to handle fatal errors. In this case, a FormEntryError object is built and
	 * stored based on the current queue entry and then the current queue entry is removed from the
	 * queue.
	 * 
	 * @param formEntryQueue queue entry with fatal error
	 * @param error name and/or brief description of the error
	 * @param errorDetails specifics for the fatal error
	 */
	private void setFatalError(FormEntryQueue formEntryQueue, String error, String errorDetails, boolean propagateErrors) {
		
		if (!propagateErrors) {
			XformsFormEntryError formEntryError = new XformsFormEntryError();
			formEntryError.setFormData(formEntryQueue.getFormData());
			formEntryError.setError(error);
			formEntryError.setErrorDetails(errorDetails);
			createFormEntryError(formEntryError);
		}
		
		deleteFormEntryQueue(formEntryQueue);
	}
	
	public void createFormEntryError(XformsFormEntryError formEntryError) {
		formEntryError.setCreator(Context.getAuthenticatedUser());
		formEntryError.setDateCreated(new Date());
		((XformsService) Context.getService(XformsService.class)).createFormEntryError(formEntryError);
	}
	
	public void deleteFormEntryQueue(FormEntryQueue formEntryQueue) {
		if (formEntryQueue == null || formEntryQueue.getFileSystemUrl() == null)
			throw new FormEntryException("Unable to load formEntryQueue with empty file system url");
		
		File file = new File(formEntryQueue.getFileSystemUrl());
		
		if (file.exists()) {
			file.delete();
		}
	}
	
	public void createFormEntryArchive(FormEntryArchive formEntryArchive) {
		User creator = Context.getAuthenticatedUser();
		
		File queueDir = FormEntryUtil.getFormEntryArchiveDir(formEntryArchive.getDateCreated());
		
		File outFile = FormEntryUtil.getOutFile(queueDir, formEntryArchive.getDateCreated(), creator);
		
		// write the queue's data to the file
		try {
			FormEntryUtil.stringToFile(formEntryArchive.getFormData(), outFile);
		}
		catch (IOException io) {
			throw new FormEntryException("Unable to save formentry archive", io);
		}
		
	}
	
	public void garbageCollect() {
		Context.clearSession();
	}
	
	/**
	 * Utility method that adds the 'provider_id_type' attribute to the 'encounter.provider_id' tag
	 * and set its value to 'PROIVDER.ID'
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	private static String addProviderAttribute(String xml) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		        .parse(new ByteArrayInputStream(xml.getBytes()));
		NodeList providerTags = doc.getElementsByTagName(XformBuilder.NODE_ENCOUNTER_PROVIDER_ID);
		for (int i = 0; i < providerTags.getLength(); i++) {
			//when we start supporting multiple providers, this should still work
			Element providerElement = (Element) providerTags.item(i);
			providerElement.setAttribute(XformBuilder.ATTRIBUTE_PROVIDER_ID_TYPE,
			    XformBuilder.VALUE_PROVIDER_ID_TYPE_PROV_ID);
		}
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StreamResult result = new StreamResult(new StringWriter());
		transformer.transform(new DOMSource(doc), result);
		
		return result.getWriter().toString();
	}
}
