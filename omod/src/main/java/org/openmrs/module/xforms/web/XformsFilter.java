package org.openmrs.module.xforms.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.filter.GZIPResponseWrapper;


/**
 * Filter for caching and gzip xforms resources.
 * 
 * @author danielkayiwa
 *
 */
public class XformsFilter implements Filter {

	protected final Log log = LogFactory.getLog(getClass());

	//* 1 day in seconds = 86400
	//* 1 week in seconds = 604800
	//* 1 month in seconds = 2629000
	//* 1 year in seconds = 31536000 (effectively infinite on Internet time)

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) req;
		HttpServletResponse httpResponse = (HttpServletResponse) res;

		if(shouldCache(httpRequest.getRequestURI())){
			httpResponse.setHeader("Cache-Control", "max-age=31536000"); // HTTP 1.1
			httpResponse.setHeader("Pragma", "public"); // HTTP 1.0
			//httpResponse.setDateHeader("Expires", 0); // Proxies.
		}

		//We only gzip xforms resources and only if openmrs is not already doing so.
		if(isXformsResoure(httpRequest.getRequestURI())){
			
			if (isGZIPSupported(httpRequest) && !isOpenmrsZipping()) {			
				GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(httpResponse);

				chain.doFilter(httpRequest, wrappedResponse);
				wrappedResponse.finishResponse();

				return;
			}
		}

		chain.doFilter(httpRequest, httpResponse);
	}

	private boolean shouldCache(String uri){
		if(uri.contains("nocache"))
			return false;

		if(uri.endsWith(".form"))
			return false;

		if(uri.endsWith(".list"))
			return false;

		if(uri.contains("/dwr/"))
			return true;

		if(uri.contains("/scripts/dojo/"))
			return true;

		if(uri.contains("/moduleResources/xforms/"))
			return true;

		if(uri.contains("/module/xforms/"))
			return true;

		if(uri.endsWith("/openmrs.js"))
			return true;

		if(uri.endsWith("/openmrs.css"))
			return true;

		if(uri.endsWith("/style.css"))
			return true;

		if(uri.endsWith("/openmrs_logo_short.gif"))
			return true;

		if(uri.endsWith("/dojoConfig.js"))
			return true;

		return false;
	}

	private boolean isXformsResoure(String uri){
		if(uri.contains("/xforms/"))
			return true;

		if(uri.contains("/dwr/"))
			return true;

		if(uri.contains("/scripts/dojo/"))
			return true;

		if(uri.endsWith("/openmrs.js"))
			return true;

		if(uri.endsWith("/openmrs.css"))
			return true;

		if(uri.endsWith("/style.css"))
			return true;

		if(uri.endsWith("/dojoConfig.js"))
			return true;
		
		return false;
	}

	/**
	 * Convenience method to test for GZIP capabilities
	 * 
	 * @param req The current user request
	 * @return boolean indicating GZIP support
	 */
	private boolean isGZIPSupported(HttpServletRequest req) {

		String browserEncodings = req.getHeader("accept-encoding");
		boolean supported = ((browserEncodings != null) && (browserEncodings.indexOf("gzip") != -1));

		String userAgent = req.getHeader("user-agent");

		if ((userAgent != null) && userAgent.startsWith("httpunit"))
			return false;
		else
			return supported;
	}

	private boolean isOpenmrsZipping(){
		try{
			String gzipEnabled = Context.getAdministrationService().getGlobalProperty(
					OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ENABLED, "");

			return gzipEnabled.toLowerCase().equals("true");
		}
		catch(Exception ex){

		}

		return false;
	}


	public void init(FilterConfig filterConfig) {

	}

	public void destroy() {

	}
}
