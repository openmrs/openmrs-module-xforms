package org.openmrs.module.xforms.download;


import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.formentry.FormXmlTemplateBuilder;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.module.xforms.SerializableData;

/**
 * Manages xforms download.
 * 
 * @author Daniel
 *
 */
public class XformDownloadManager {
	
	/**
	 * Writes xforms to a stream.
	 * 
	 * @param actionUrl - the URL to post to after data entry.
	 * @param os - the stream.
	 * @throws Exception
	 */
	public static void downloadXforms(String actionUrl, OutputStream os) throws Exception{
		Context.openSession(); //This prevents the bluetooth server from failing with the form field lazy load exception.
		
		String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_XFORM_SERIALIZER);
		if(className == null || className.length() == 0)
			className = XformConstants.DEFAULT_XFORM_SERIALIZER;
		
		SerializableData sr = (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
		sr.serialize(new DataOutputStream(os), getXmlForms(actionUrl));
		
		//Context.closeSession();
	}
	
	/**
	 * Gets xforms as xml text.
	 * 
	 * @param actionUrl
	 * @return a list of xforms xml text.
	 */
	private static List<String> getXmlForms(String actionUrl){
		String useStoredXform = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_STORED_XFORMS);
		boolean createNew = false;
		if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(useStoredXform))
			createNew = true;

		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);

		List<Xform> xforms = xformsService.getXforms();
		List<String> xmlforms = new ArrayList<String>();
		for(Xform xform : xforms){
			if(xform.getFormId() != XformConstants.PATIENT_XFORM_FORM_ID){
				String s = getXform(formEntryService,xformsService,xform.getFormId(),createNew,actionUrl);
				xmlforms.add(s);
			}
		}
		
		String xml = XformBuilder.getNewPatientXform(actionUrl);
		xmlforms.add(xml);
		return xmlforms;
	}
	
	/**
	 * Creates a new xform.
	 * 
	 * @param request - the http request.
	 * @param formEntryService - the formentry service.
	 * @param formId - the form id.
	 * @return - the created xml form.
	 */
	public static String createNewXform(FormEntryService formEntryService, Integer formId,String actionUrl){
		Form form = formEntryService.getForm(formId);
		return createNewXform(formEntryService, form,actionUrl);
	}
	
	
	
	/**
	 * Creates a new xform for an given openmrs form.
	 * 
	 * @param request - the request object.
	 * @param formEntryService - the formentry service.
	 * @param form - the form object.
	 * @return - the xml content of the xform.
	 */
	public static String createNewXform(FormEntryService formEntryService, Form form,String actionUrl){
		String schemaXml = formEntryService.getSchema(form);
		String templateXml = new FormXmlTemplateBuilder(form,FormEntryUtil.getFormAbsoluteUrl(form)).getXmlTemplate(false);
		return XformBuilder.getXform4mStrings(schemaXml, templateXml,actionUrl);
	}
	
	/**
	 * Gets an xform for a given form id.
	 * 
	 * @param formEntryService - the formentry service.
	 * @param xformsService  the xforms service.
	 * @param formId - the form id.
	 * @param createNew - true if you want
	 * @return - the xml content of the xform.
	 */
	public static String getXform(FormEntryService formEntryService,XformsService xformsService,Integer formId,boolean createNew,String actionUrl){
		
		String xformXml = null;
		
		if(!createNew){
			Xform xform = xformsService.getXform(formId);
			if(xform != null)
				xformXml = xform.getXformData();
		}
		
		if(xformXml == null)
			xformXml = createNewXform(formEntryService, formId,actionUrl);
		
		return xformXml;
	}
	
	/**
	 * Gets the xslt of an xform for a given form id.
	 * 
	 * @param xformsService  the xforms service.
	 * @param formId - the form id.
	 * @param createNew - true if you want
	 * @return - the xslt for the xform.
	 */
	public static String getXslt(XformsService xformsService,Integer formId,boolean createNew){
		
		String xslt = null;
		
		if(!createNew)
			xslt = xformsService.getXslt(formId);
		
		if(xslt == null)
			xslt = XformsUtil.getDefaultXSLT();
		
		return xslt;
	}
}
