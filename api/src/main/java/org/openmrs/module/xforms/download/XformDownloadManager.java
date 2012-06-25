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
import org.openmrs.module.xforms.XformBuilderEx;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.util.XformsUtil;

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
	public static void downloadXforms(OutputStream os, String serializerKey) throws Exception{		
		if(serializerKey == null || serializerKey.trim().length() == 0)
			serializerKey = XformConstants.GLOBAL_PROP_KEY_XFORM_SERIALIZER;
		
        XformsUtil.invokeSerializationMethod("serializeForms",os, serializerKey, XformConstants.DEFAULT_XFORM_SERIALIZER, getXmlForms());
	}
	
	/**
	 * Gets xforms as xml text.
	 * 
	 * @param actionUrl
	 * @return a list of xforms xml text.
	 */
	private static List<String> getXmlForms(){
		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		
		List<Xform> xforms = xformsService.getXforms();
		List<String> xmlforms = new ArrayList<String>();
		for(Xform xform : xforms){
			String xml = xform.getXformXml();
			if(xml != null)
				xmlforms.add(xml);
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
	public static String createNewXform(FormService formService, Integer formId) throws Exception {
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
	public static String createNewXform(FormService formService, Form form) throws Exception {
		//String schemaXml = XformsUtil.getSchema(form);
		//String templateXml = FormEntryWrapper.getFormTemplate(form);
		//XformBuilder.getXform4mStrings(schemaXml, templateXml);
		return XformBuilderEx.buildXform(form); 
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
	public static String getXform(FormService formService,XformsService xformsService,Integer formId,boolean createNew) throws Exception {
		
		String xformXml = null;
		
        //try{ //we want exception to propagate to the ui
    		if(!createNew){
    			Xform xform = xformsService.getXform(formId);
    			if(xform != null)
    				xformXml = xform.getXformXml();
    		}
    		
    		if(xformXml == null)
    			xformXml = createNewXform(formService, formId);
        /*}
        catch(Exception e){
            log.error(e.getMessage(),e);
        }*/
		
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
