package org.openmrs.module.xforms.web.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsServer;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.download.PatientDownloadManager;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

//TODO This class may need to be refactored out of the XForms module.

/**
 * Privides patient download services.
 * 
 * @author Daniel
 *
 */
public class PatientDownloadController extends SimpleFormController{

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	private List<Cohort> cohorts;

	@Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

		Map<Object, Object> data = new HashMap<Object, Object>();
		cohorts = Context.getCohortService().getCohorts();
		data.put("cohorts", cohorts);

		return data;
	}


	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {						

		if(request.getParameter(XformConstants.REQUEST_PARAM_DOWNLOAD_COHORTS) != null ||
				request.getParameter(XformConstants.REQUEST_PARAM_DOWNLOAD_PATIENTS) != null){

			String serializerKey = request.getParameter("serializerKey");

			if(serializerKey == null || serializerKey.trim().length() == 0){
				new XformsServer().processConnection(new DataInputStream((InputStream)request.getInputStream()), new DataOutputStream((OutputStream)response.getOutputStream()));
			}
			else{
				//try to authenticate users who logon inline (with the request).
				XformsUtil.authenticateInlineUser(request);

				//check if user is authenticated
				if (!XformsUtil.isAuthenticated(request,response,"/module/xforms/patientDownload.form"))
					return null;

				if(request.getParameter(XformConstants.REQUEST_PARAM_DOWNLOAD_COHORTS) != null)
					PatientDownloadManager.downloadCohorts(response.getOutputStream(),serializerKey);
				else
					PatientDownloadManager.downloadPatients(request.getParameter("cohortId"), response.getOutputStream(), serializerKey, false);
			}
		}
		else{
			//try to authenticate users who logon inline (with the request).
			XformsUtil.authenticateInlineUser(request);

			//check if user is authenticated
			if (!XformsUtil.isAuthenticated(request,response,"/module/xforms/patientDownload.form"))
				return null;

			XformsService xformsService = (XformsService)Context.getService(XformsService.class);

			//check if the user has requested for a given cohort, else use the default.
			String cohortId = request.getParameter(XformConstants.REQUEST_PARAM_COHORT_ID);
			if(cohortId == null)
				cohortId = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_PATIENT_DOWNLOAD_COHORT);

			if(cohortId != null && cohortId.length() > 0){
				if(request.getParameter(XformConstants.REQUEST_PARAM_SET_COHORT) != null){
					Context.getAdministrationService().setGlobalProperty("xforms.patientDownloadCohort", cohortId);
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.setPatientDownloadCohortSuccess");
					return new ModelAndView(new RedirectView(getSuccessView()));
				}
			}

			if(request.getParameter(XformConstants.REQUEST_PARAM_UPLOAD_PATIENT_XFORM) != null){//TODO This needs to be refactored with the XformUploadController
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
				MultipartFile xformFile = multipartRequest.getFile(XformConstants.REQUEST_PARAM_PATIENT_XFORM_FILE);
				if (xformFile != null && !xformFile.isEmpty()) {
					String xml = IOUtils.toString(xformFile.getInputStream(),XformConstants.DEFAULT_CHARACTER_ENCODING);
					Xform xform = new Xform();
					xform.setFormId(XformConstants.PATIENT_XFORM_FORM_ID);
					xform.setXformXml(xml);
					xformsService.saveXform(xform);

					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.patientXformUploadSuccess");
				}
				return new ModelAndView(new RedirectView(getSuccessView()));
			}
			else if(request.getParameter(XformConstants.REQUEST_PARAM_DOWNLOAD_PATIENT_XFORM) != null){
				String filename = XformConstants.PATIENT_XFORM_FORM_ID+XformConstants.XML_FILE_EXTENSION;
				response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + filename);
				response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
				response.getWriter().print(XformBuilder.getNewPatientXform());
			}
		}

		return null;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception { 
		return "";
	}
}
