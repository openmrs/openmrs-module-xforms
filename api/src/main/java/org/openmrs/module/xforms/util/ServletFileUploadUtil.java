package org.openmrs.module.xforms.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


/**
 * Handles upload of xforms as files. Where a submission can consist of multiple files like 
 * images files, pictures files, sound files, video files, and more. (multipart content)
 * 
 * @author daniel
 *
 */
public class ServletFileUploadUtil {
	
	public static String getXformsInstanceData(HttpServletRequest request, HttpServletResponse response, PrintWriter writer) throws IOException, Exception {
		
		String serverLocation = request.getServerName();
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		upload.setSizeMax(50000000);
		try {
			List<FileItem> items = upload.parseRequest(request);
			// find the xform that user wants to submit to OpenMrs
			String xml = findXformFromItems(items);
			if(xml.compareTo("") == 0) { // send error if user sends no xform to submit
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request contains no form");
				return null;
			}
			if(items.size() > 0) 
				xml = fillBinaryDataToXform(xml, items);
			
			// send success signal
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader("Location", serverLocation);
			writer.println("Data submitted successfully");
			return xml;
		// any exception occurs, fire up error message(exception for exceeding image file size)
		} catch (FileUploadException e2) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request contains more than 10M bytes file");
			return null;
		}
	}
	
	
	/**
	 * return xform in string format after finding it from the item list and after removing
	 * it from the list. if there is no xform in the list, then return ""
	 * 
	 * @param items - fileItem list from request
	 * @return xform found in string format
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static String findXformFromItems(List<FileItem> items) throws IOException {
		Iterator iterator = items.iterator();
		while(iterator.hasNext()) {
			FileItem item = (FileItem) iterator.next();
			if(item.getFieldName().compareTo("xml_submission_file") == 0) {
				InputStream in = item.getInputStream();
				String xml = IOUtils.toString(in);
				iterator.remove();
				return xml;
			}
		}
		return "";
	}
	
	
	/**
	 * return xform passed in after replacing binary file names in the xform with its actual data in 
	 * Base64 format encoded
	 * 
	 * @param xml - xform in string format
	 * @param items - list of binary type files
	 * @return xform in string format
	 * @throws IOException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static String fillBinaryDataToXform(String xml, List<FileItem> items) throws IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder();
	    Document document = builder.build(new ByteArrayInputStream(xml.getBytes()));
		Iterator iterator = items.iterator();
		while(iterator.hasNext()) {
			FileItem item = (FileItem) iterator.next();
			String fileName = item.getFieldName();
			XPath xPath = XPath.newInstance("/form/obs//value[. = \"" + fileName + "\"]");
			Element value = (Element) xPath.selectSingleNode(document);
			InputStream in = item.getInputStream();
			byte[] bytes = IOUtils.toByteArray(in);
			value.setText(Base64.encode(bytes));
		}
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        return out.outputString(document);
	}
}
