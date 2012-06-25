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
import org.openmrs.module.xforms.util.XformsUtil;


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
		
		//try to authenticate users who logon inline (with the request).
		XformsUtil.authenticateInlineUser(request);

		// check if user is authenticated
		if (!XformsUtil.isAuthenticated(request,response,null))
			return;
			
		String source = request.getParameter("ExternalSource");
		String displayField = request.getParameter("DisplayField");
		String valueField = request.getParameter("ValueField");
		String filterField = request.getParameter("FilterField");
		String filterValue = request.getParameter("FilterValue");
		
		String sql = source;
		if(!sql.startsWith("select"))
			sql = "select " + displayField + "," + valueField + " from " + source + 
			" where " + displayField + " is not null and " + valueField + " is not null ";
		
		if((filterField != null && filterField.trim().length() > 0) &&
				filterValue != null && filterValue.trim().length() > 0){
			
			sql += " and " + filterField;
			
			if(filterValue.equalsIgnoreCase("IS NULL"))
				 sql += " is null ";
			else
				sql += "='" + filterValue + "'";
		}
		
		if(source.equalsIgnoreCase("concept"))
			return; //sql = "select name, concat(concept_id,concat(concat('^',name),'^99DCT')) as id from concept_name where locale='"+ Context.getLocale().getLanguage()+"'";
		
		sql += " order by " + displayField;
		
		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		List<Object[]> list = xformsService.getList(sql, displayField, valueField);
		
		String result = null; //"Baganda|1$Bacholi|2$Bagisu|3$Basoga|4$Banyankole|5";
		
		for(Object[] obj : list){
			
			if(obj[0].toString().trim().length() == 0)
				continue;
			
			if(obj[1].toString().trim().length() == 0)
				continue;
			
			if(result != null)
				result += "$";
			else
				result = "";
			
			result += obj[0];
			result += "|" + obj[1];
		}
				
		response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", -1);
        response.setHeader("Cache-Control", "no-store");
        
 		response.setContentType("text/plain; charset=UTF-8");
 		response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
		response.getWriter().print(result);
	}

}
