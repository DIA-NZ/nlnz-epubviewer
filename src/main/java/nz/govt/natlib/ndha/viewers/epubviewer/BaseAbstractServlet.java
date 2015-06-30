package nz.govt.natlib.ndha.viewers.epubviewer;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/*
 * Class: Servlet class BaseAbstractServlet
 * 
 * Base abstract class for all servlets implemented in EpubViewer web app project
 * 
 * Author: Preeti Badle [01.Jul.2014]
 */

public abstract class BaseAbstractServlet extends HttpServlet {

	private static final long serialVersionUID = 7403990815119233814L;
	private static Logger logger = Logger.getLogger(BaseAbstractServlet.class);
	
	public static String ndhaDeliveryServer;
	public static String viewerHostName;
	
	public static final String X_FORWARED_FOR = "X-Forwarded-For";
	public static final String DELIVERY_WSDL_URL = "/dpsws/delivery/DeliveryAccessWS?wsdl";
	public static final String FILEPID_REQUEST_PARAMETER = "dps_pid";
	public static final String ROSETTA_SESSION_REQUEST_PARAMETER = "dps_dvs";
	public static final String DOWNLOADFILE_REQUEST_PARAMETER = "download";
	public static final String REQUEST_PARAMETER_VALUE_TRUE = "true";
	public static final String FILESEPARATOR = System.getProperty("file.separator");
	

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public BaseAbstractServlet() {
		super();
	}
	
	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {

		// Servlet Initialization details here...
		logger.debug("Servlet Initialization - Initializing the context parameters: START");
		
		super.init(config);
		

		logger.debug("Reading NDHA Delivery Server Base URL from web.xml context param");

		ndhaDeliveryServer = config.getServletContext().getInitParameter("ndhaDeliveryBaseURL");
		if (ndhaDeliveryServer == null) {
			throw new UnavailableException("The ndhaDeliveryBaseURL parameter is not set in web.xml");
		}

		logger.info("NDHA DELIVERY SERVER BASE URL:[" + ndhaDeliveryServer + "]");

		logger.debug("Reading Viewer Host Name from web.xml context param");

		viewerHostName = config.getServletContext().getInitParameter("viewerHostName");
		if (viewerHostName == null) {
			throw new UnavailableException("The viewerHostName parameter is not set in web.xml");
		}
		
		logger.info("The hostname:[" + viewerHostName + "] for the Epub client viewer calls to server");
		
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.debug("doGet() - invoked");
		processRequest(request, response);	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.debug("doPost() - invoking the doGet() implementation");
		doGet(request, response);
	}

	/**
	 * abstract method to be implemented in the subclasses
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException, IOException
	 */
	protected abstract void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException;
	
}
