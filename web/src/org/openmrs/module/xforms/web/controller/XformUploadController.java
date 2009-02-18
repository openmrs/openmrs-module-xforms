package org.openmrs.module.xforms.web.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;



/**
 * Provides XForms upload services.
 * 
 * @author Daniel
 *
 */
public class XformUploadController extends SimpleFormController{

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    @Override
    protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
        return new HashMap<String,Object>();
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception { 
        return "Not Yet";
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {						

        //try to authenticate users who logon inline (with the request).
        XformsUtil.authenticateInlineUser(request);

        //check if user is authenticated
        if (!XformsUtil.isAuthenticated(request,response,"/module/xforms/xformUpload.form"))
            return null;

        Integer formId = Integer.parseInt(request.getParameter("formId"));

        String target = request.getParameter(XformConstants.REQUEST_PARAM_TARGET);

        if (XformConstants.REQUEST_PARAM_XFORM.equals(target)){
            String xml = getRequestAsString(request);
            XformsService xformsService = (XformsService)Context.getService(XformsService.class);
            Xform xform = new Xform();
            xform.setFormId(formId);
            
            String xformXml, layoutXml = null;
            
            int pos = xml.indexOf(XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR);
            if(pos > 0){
                xformXml = xml.substring(0,pos);
                layoutXml = xml.substring(pos+XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR.length(), xml.length());
            }
            else
                xformXml = xml;
            
            xform.setXformXml(xformXml);
            xform.setLayoutXml(layoutXml);
            
            xformsService.saveXform(xform);
            return null;
        }
        else{
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;

            if (XformConstants.REQUEST_PARAM_XSLT.equals(target)){
                MultipartFile xsltFile = multipartRequest.getFile("xsltFile");
                if (xsltFile != null && !xsltFile.isEmpty()) {
                    XformsService xformsService = (XformsService)Context.getService(XformsService.class);
                    String xml = IOUtils.toString(xsltFile.getInputStream());
                    xformsService.saveXslt(formId,xml);
                    request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.xsltUploadSuccess");
                }
            }
            else{
                MultipartFile xformFile = multipartRequest.getFile("xformFile");
                if (xformFile != null && !xformFile.isEmpty()) {
                    XformsService xformsService = (XformsService)Context.getService(XformsService.class);
                    String xml = IOUtils.toString(xformFile.getInputStream());
                    Xform xform = new Xform();
                    xform.setFormId(formId);
                    xform.setXformXml(xml);
                    xformsService.saveXform(xform);
                    request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.xformUploadSuccess");
                }
            }

            return new ModelAndView(new RedirectView(request.getContextPath() + "/admin/forms/formSchemaDesign.form?formId=" + formId));
        }
    }

    private String getRequestAsString(HttpServletRequest request) throws java.io.IOException {
        BufferedReader requestData = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuffer stringBuffer = new StringBuffer();
        String line;
        try{
            while ((line = requestData.readLine()) != null)
                stringBuffer.append(line);
        } 
        catch (Exception e){e.printStackTrace();}

        return stringBuffer.toString();
        //return IOUtils.toString(request.getInputStream());
    }
}
