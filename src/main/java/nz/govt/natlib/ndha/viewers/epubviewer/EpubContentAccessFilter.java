package nz.govt.natlib.ndha.viewers.epubviewer;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Filter class to check and validate the epub content access requests.
 * The class verifies the user session that has been set by the manager servlet,
 * allows valid requests and rejects illegal or invalid requests.
 * 
 * @author Preeti Badle [01.Jul.2014]
 *
 */
public class EpubContentAccessFilter implements Filter {
	
	private static Logger logger = Logger.getLogger(EpubContentAccessFilter.class);

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		logger.debug("doFilter() invoked");
		
		HttpServletRequest httpReq = (HttpServletRequest) req;
		HttpServletResponse httpRes = (HttpServletResponse) res;

		HttpSession session = httpReq.getSession(false);
		
		if (session != null) {
			// Get the session attribute
			String epubAccessValue = (String) session.getAttribute("epubaccessid");
		
			if (epubAccessValue != null) {
				
				String clientIP = new EpubViewerManagerServlet().retrieveRemoteIP(httpReq);
				
				logger.info("Client IP Address:[" + clientIP + "] retrieved from the http servlet request");
				logger.info("Epub Session ID value:[" + epubAccessValue + "] retrieved from the user session");
	
				// Extract the IP Address and epub filename from the session value
				String[] sessionValues = epubAccessValue.split("@");
				if (sessionValues.length > 0) {
					// Compare the IP addresses to ensure that the request is from an authentic user
					if (clientIP.equals(sessionValues[0])) {
						// Valid and genuine user - carry on
						chain.doFilter(req, res);
						
					} else {
						// Redirect to unauthorized page - Illegal request
						logger.info("Client IP does not match. Redirecting to unauthorised page.");
						httpRes.sendRedirect(httpReq.getContextPath() + "/unauthorized.jsp");
					}
				}
			
			} else {
				// Redirect to unauthorized page - Illegal request
				logger.info("No legal user session. Redirecting to unauthorised page.");
				httpRes.sendRedirect(httpReq.getContextPath() + "/unauthorized.jsp");
			}
			
		} else {
			// Redirect to unauthorized page - Illegal request
			logger.info("No session found. Redirecting to unauthorised page.");
			httpRes.sendRedirect(httpReq.getContextPath() + "/unauthorized.jsp");
		}
	}

	public void init(FilterConfig config) throws ServletException {
	}
	
}