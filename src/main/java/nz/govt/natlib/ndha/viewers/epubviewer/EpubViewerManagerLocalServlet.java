package nz.govt.natlib.ndha.viewers.epubviewer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * EpubViewerManagerLocalServlet handles all http requests for epub files from localhost
 * 
 * Input parameters:
 * 		dps_pid - represents Rosetta file pid
 * 		dps_dvs - represents Rosetta session (optional)
 * 		download - represents if the epub file should be returned for download as a file stream (optional)
 * 
 * Retrieves the file path for the particular file with the given pid
 * Invokes the EpubExploder() which gets the streamed epub file from the permanent repository
 * and unzips it within the deployed directory for the web app as per ReadiumJS specs
 * Generate a specific user session and set User IP & Epub Filename as the session variable.
 * 
 */

public class EpubViewerManagerLocalServlet extends BaseAbstractServlet {

	private static final long serialVersionUID = -3560995776291660005L;
	private static Logger logger = Logger.getLogger(EpubViewerManagerLocalServlet.class);
	
	// Location of the epub file if running locally
	private static final String EPUB_FILES_LOCAL_BASE_PATH = "C:\\AppDev\\NDHA\\EPub_files\\";

	public EpubViewerManagerLocalServlet() {
		super();
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServletException, IOException
	 */
	@Override
	protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		logger.debug("processRequest() - Start of method ");
		
		String errorMessage = "";
		if (req != null) {
			String filePid = req.getParameter(FILEPID_REQUEST_PARAMETER);

			logger.info("Processing request for FILEPID:[" + filePid + "]");

			if (filePid != null) {

				String rosettaFilePath = EPUB_FILES_LOCAL_BASE_PATH + filePid + ".epub";

				try {
					// Retrieve the epub file path
					logger.info("processRequest() - Epub file path:[" + rosettaFilePath + "]");
					
					// Handle the epub file download request
					if (req.getParameter(DOWNLOADFILE_REQUEST_PARAMETER) != null &&
							(req.getParameter(DOWNLOADFILE_REQUEST_PARAMETER)).equalsIgnoreCase(REQUEST_PARAMETER_VALUE_TRUE)) {
						
						logger.info("Processing Download file request for FILEPID:[" + filePid + "]");
						
						getFileFromLocal(res, rosettaFilePath, filePid);
						
					} else {
						// Unzip epub file
						String epubFileName = new EpubExploder().explodeFile(rosettaFilePath, getServletContext().getRealPath("/"), filePid);
						
						// Retrieve the client IP Address
						String clientIP = retrieveRemoteIP(req);
						
						if (epubFileName != null && clientIP.isEmpty() == false) {

							HttpSession session = req.getSession(true);
							// Generate user session and set the session attribute
							session.setAttribute("epubaccessid", clientIP + "@" + epubFileName);
							
							req.setAttribute("epubFileName", epubFileName);					
							req.setAttribute("filePid", filePid);
							req.setAttribute("rosettaSession", "678YUT6745FGRT");
							req.setAttribute("hostName", viewerHostName);
							
							// Forward the request to display the epub file
							RequestDispatcher dispatcher = req.getRequestDispatcher("epubviewerpane");
							dispatcher.forward(req, res);
							
						} else {
							errorMessage = "Error occured while processing the epub file for FilePid:[" + filePid + "].";
							reportError(req, res, errorMessage);
						}
					}

				} catch (Exception ex) {
					logger.error("Exception occured while trying to retrieve delivery web service for FilePid:[" + filePid + "]. " + ex.getMessage());
					
					errorMessage = "Exception occured while trying to process request for FilePid:[" + filePid + "].";
					reportError(req, res, errorMessage);
				}

			} else {
				// Report error - Bad Request
				errorMessage = "Bad request. Unable to process the request.";
				reportError(req, res, errorMessage);
			}

		} else {
			// Report error - Invalid Request
			errorMessage = "Invalid request. Unable to process the request.";
			reportError(req, res, errorMessage);
		}
	}

	protected void reportError(HttpServletRequest req, HttpServletResponse res, String errMsg) throws ServletException, IOException {
		// Set the error message in the session attribute and redirect to error page (web.xml) 
		req.setAttribute("errorMessage", errMsg);
		RequestDispatcher dispatcher = req.getRequestDispatcher("errorpage");
		dispatcher.forward(req, res);
	}
	
	/**
	 * This method gets the file from rosetta and streams it. The file stream is then written
	 * to the response output stream so that the file can be downloaded.
	 * 
	 * @param response
	 * @param filePath
	 * @param filePid
	 */
	private void getFileFromLocal(HttpServletResponse res, String epubFilePath, String filePid) {
		// Implement the file out streaming for download file
		String epubFilename = epubFilePath.substring(epubFilePath.lastIndexOf(FILESEPARATOR)+1).replace(".epub", "");
		File epubFile = new File(epubFilePath);
		
		BufferedInputStream bis = null;
		ServletOutputStream sos = null;

		try {
			
			if (epubFile != null && epubFile.exists()) {
				
				logger.info("getFileFromLocal() - Requested filename: " + epubFilename);

				res.setStatus(HttpServletResponse.SC_OK);
				res.setHeader("Content-Disposition", "inline; filename=" + epubFilename + ".epub");
				res.setHeader("Content-Length", String.valueOf(epubFile.length()));
				String mimetype = getServletContext().getMimeType(epubFilePath);
	        
		        // sets response content type
		        if (mimetype == null) {
		            mimetype = "application/epub+zip";
		        }
		        res.setContentType(mimetype);

		        logger.info("getFileFromLocal() - MIME Type set to: " + mimetype);
		        
				bis = new BufferedInputStream(new FileInputStream(epubFile));
				sos = res.getOutputStream();
				byte[] buffer = new byte[1024 * 8];
				int readSize = -1;
				logger.info("getFileFromLocal() - buffering the requested file to servlet output stream...start");
				while ((readSize = bis.read(buffer)) != -1) {
					sos.write(buffer, 0, readSize);
				}
				logger.info("getFileFromLocal() - buffering the requested file to servlet output stream...end");
				if (sos != null) {
					sos.close();
				}
				if (bis != null) {
					bis.close();
				}
				
			} else {
				res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				logger.error("Error reading the epub file from Rosetta for FilePid:[" + filePid + "].");
			}
			
		} catch (Exception ex) {
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Error occured while trying to stream the file from Rosetta. " +  ex.getMessage());
		}
	}
	
	/**
	 * Retrieves the Remote caller's IP Address
	 */
	protected String retrieveRemoteIP(HttpServletRequest httpReq) {
		String remote_IP = "";

        logger.debug("retrieveRemoteIP() - Method entry - HttpServletRequest: " + httpReq);

        try {
            String ipAddress = httpReq.getHeader(X_FORWARED_FOR);

            if ((ipAddress == null) || (ipAddress.isEmpty())) {
                remote_IP = httpReq.getRemoteAddr();

            } else {

                logger.debug("retrieveRemoteIP() - HTTP Request Header (X-Forwarded-For): " + ipAddress);

                if (ipAddress.indexOf(",") > 0) {
                    String[] ips = ipAddress.split(",");
                    remote_IP = ips[0];

                } else {
                    remote_IP = ipAddress;
                }
            }
        } catch (Exception ex) {
            remote_IP = "";
            logger.error("retrieveRemoteIP() - An exception occured while trying" + "to retrieve the client IP from the HTTP Request. " + ex.getMessage());
        }

        logger.debug("retrieveRemoteIP() - Method exit - Client IP Address: " + remote_IP);
        return remote_IP;
	}
}
