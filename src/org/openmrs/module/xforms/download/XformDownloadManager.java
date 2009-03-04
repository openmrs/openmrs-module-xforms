package org.openmrs.module.xforms.download;


import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;

/**
 * Manages xforms download.
 * 
 * @author Daniel
 *
 */
public class XformDownloadManager {
	
    public static final long serialVersionUID = 123427878343561L;
    
    private static Log log = LogFactory.getLog(XformDownloadManager.class);
    
    
	/**
	 * Writes xforms to a stream.
	 * 
	 * @param actionUrl - the URL to post to after data entry.
	 * @param os - the stream.
	 * @throws Exception  
	 */
	public static void downloadXforms(OutputStream os) throws Exception{
		//Context.openSession(); //This prevents the bluetooth server from failing with the form field lazy load exception.
		
        XformsUtil.invokeSerializationMethod("serializeForms",os, XformConstants.GLOBAL_PROP_KEY_XFORM_SERIALIZER, XformConstants.DEFAULT_XFORM_SERIALIZER, getXmlForms());
        
		/*String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_XFORM_SERIALIZER);
		if(className == null || className.length() == 0)
			className = XformConstants.DEFAULT_XFORM_SERIALIZER;
        
        Object obj = OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
        Method method = obj.getClass().getMethod("serialize", new Class[]{DataOutputStream.class,Object.class});
        method.invoke(obj, new Object[]{new DataOutputStream(os), getXmlForms(actionUrl)});*/
        
		//SerializableData sr = (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
		//sr.serialize(new DataOutputStream(os), getXmlForms(actionUrl));
	}
	
	/**
	 * Gets xforms as xml text.
	 * 
	 * @param actionUrl
	 * @return a list of xforms xml text.
	 */
	private static List<String> getXmlForms(){
		/*String useStoredXform = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_STORED_XFORMS);
		boolean createNew = false;
		if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(useStoredXform))
			createNew = true;*/

		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		//FormService formService = (FormService)Context.getService(FormService.class);

		/*List<Xform> xforms = xformsService.getXforms();
		List<String> xmlforms = new ArrayList<String>();
		for(Xform xform : xforms){
			if(xform.getFormId() != XformConstants.PATIENT_XFORM_FORM_ID){
				String s = getXform(formService,xformsService,xform.getFormId(),createNew);
				if(s != null) //could fail parsing xform.
				    xmlforms.add(s);
			}
		}
		
		String xml = XformBuilder.getNewPatientXform();
		xmlforms.add(xml);*/
		
		List<Xform> xforms = xformsService.getXforms();
		List<String> xmlforms = new ArrayList<String>();
		for(Xform xform : xforms){
			String xml = xform.getXformXml();
			if(xml != null)
				xmlforms.add(xml);
			//if(xform.getFormId() == 18)
			//	System.out.println(xml);
		}
		
		return xmlforms;
	}
	
	/**
	 * Creates a new xform.
	 * 
	 * @param request - the http request.
	 * @param formService - the form service.
	 * @param formId - the form id.
	 * @return - the created xml form.
	 */
	public static String createNewXform(FormService formService, Integer formId){
		Form form = formService.getForm(formId);
		return createNewXform(formService, form);
	}
	
	
	
	/**
	 * Creates a new xform for an given openmrs form.
	 * 
	 * @param request - the request object.
	 * @param formService - the form service.
	 * @param form - the form object.
	 * @return - the xml content of the xform.
	 */
	public static String createNewXform(FormService formService, Form form){
		String schemaXml = XformsUtil.getSchema(form);
		String templateXml = FormEntryWrapper.getFormTemplate(form);//new FormXmlTemplateBuilder(form,FormEntryUtil.getFormAbsoluteUrl(form)).getXmlTemplate(false);
		return XformBuilder.getXform4mStrings(schemaXml, templateXml);
	}
	
	/**
	 * Gets an xform for a given form id.
	 * 
	 * @param formService - the form service.
	 * @param xformsService  the xforms service.
	 * @param formId - the form id.
	 * @param createNew - true if you want
	 * @return - the xml content of the xform.
	 */
	public static String getXform(FormService formService,XformsService xformsService,Integer formId,boolean createNew){
		
		String xformXml = null;
		
        try{
    		if(!createNew){
    			Xform xform = xformsService.getXform(formId);
    			if(xform != null)
    				xformXml = xform.getXformXml();
    		}
    		
    		if(xformXml == null)
    			xformXml = createNewXform(formService, formId);
        }
        catch(Exception e){
            log.error(e.getMessage(),e);
        }
		
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
