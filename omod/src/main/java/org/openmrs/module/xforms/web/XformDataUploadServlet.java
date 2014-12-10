package org.openmrs.module.xforms.web;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsServer;
import org.openmrs.module.xforms.download.XformDataUploadManager;
import org.openmrs.module.xforms.util.ServletFileUploadUtil;
import org.openmrs.module.xforms.util.XformsUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


/**
 * Provides XForm data upload services.
 * 
 * @author Daniel
 *
 */
public class XformDataUploadServlet extends HttpServlet{

	public static final long serialVersionUID = 1234278783771156L;

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * This just delegates to the doGet()
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter writer = response.getWriter();
		
		try{
			//check if external client sending multiple filled forms.
			if(XformConstants.TRUE_TEXT_VALUE.equalsIgnoreCase(request.getParameter(XformConstants.REQUEST_PARAM_BATCH_ENTRY)))                        
				new XformsServer().processConnection(new DataInputStream((InputStream)request.getInputStream()), new DataOutputStream((OutputStream)response.getOutputStream()));
			else{
				//try to authenticate users who logon inline (with the request).
				XformsUtil.authenticateInlineUser(request);

				// check if user is authenticated
				if (XformsUtil.isAuthenticated(request,response,"/moduleServlet/xforms/xformDataUpload")){
					response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);

					request.setAttribute(XformConstants.REQUEST_ATTRIBUTE_ID_ERROR_MESSAGE, null);
					request.setAttribute(XformConstants.REQUEST_ATTRIBUTE_ID_PATIENT_ID, null);
					
					String xml = null;

					// check if request has multipart content
					if(ServletFileUpload.isMultipartContent(request)) {
						xml = ServletFileUploadUtil.getXformsInstanceData(request, response, writer);

						Object acceptPatientUuid = request.getHeader(XformConstants.REQUEST_ATTRIBUTE_ACCEPT_PATIENT_UUID);

						if(acceptPatientUuid != null) {
							DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
							DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
							InputSource is = new InputSource(new StringReader(xml));
							Document doc = dBuilder.parse(is);
							String id = doc.getElementsByTagName("patient.patient_id").item(0).getTextContent();

							Patient patient = null;
							try {
								int idInt = Integer.parseInt(id);
								patient = Context.getPatientService().getPatient(idInt);
							} catch (NumberFormatException e) {
								//if id is not a number patient will be null
							}
							if (patient == null) {
								patient = Context.getPatientService().getPatientByUuid(id);
								if (patient != null) {
									doc.getElementsByTagName("patient.patient_id").item(0).setTextContent(patient.getId().toString());
									StringWriter sw = new StringWriter();
									TransformerFactory tf = TransformerFactory.newInstance();
									Transformer transformer = tf.newTransformer();
									transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
									transformer.setOutputProperty(OutputKeys.METHOD, "xml");
									transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
									transformer.transform(new DOMSource(doc), new StreamResult(sw));

									xml = sw.toString();
								}
							}
						}
					} else {
						xml = IOUtils.toString(request.getInputStream(),XformConstants.DEFAULT_CHARACTER_ENCODING);

						Object id = request.getAttribute(XformConstants.REQUEST_ATTRIBUTE_ID_PATIENT_ID);
						if (id != null) {
							writer.print(id.toString());
						}

						response.setStatus(HttpServletResponse.SC_OK);
						response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
						writer.println("Data submitted successfully");
					}

					XformDataUploadManager.processXform(xml,request.getSession().getId(),XformsUtil.getEnterer(),true, request);
				}
				else
					System.out.println("...........Data upload user not authenticated.................");
			}
		}
		catch(Exception e){
			XformsUtil.reportDataUploadError(e, request, response, writer);
		}
	}
}
