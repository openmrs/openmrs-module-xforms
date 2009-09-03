package org.openmrs.module.xforms.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;


/**
 * 
 * @author daniel
 *
 */
public class WidgetValueServlet extends HttpServlet {

	public static final long serialVersionUID = 12342787837723432L;

	private Log log = LogFactory.getLog(this.getClass());
	
	
	/**
	 * This just delegates to the doGet()
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String source = request.getParameter("ExternalSource");
		String displayField = request.getParameter("DisplayField");
		String valueField = request.getParameter("ValueField");
		
		String sql = source;
		if(!sql.startsWith("select"))
			sql = "select " + displayField + "," + valueField + " from " + source;
		
		if(source.equalsIgnoreCase("concept"))
			sql = "select name, concat(concept_id,concat(concat('^',name),'^99DCT')) as id from concept_name where locale='"+ Context.getLocale().getLanguage()+"'";
		
		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		List<Object[]> list = xformsService.getList(sql, displayField, valueField);
		
		String result = null; //"Baganda|1$Bacholi|2$Bagisu|3$Basoga|4$Banyankole|5";
		
		for(Object[] obj : list){
			if(result != null)
				result += "$";
			else
				result = "";
			
			result += obj[0];
			result += "|" + obj[1];
		}
		
		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);
		response.getOutputStream().print(result);
	}

}
