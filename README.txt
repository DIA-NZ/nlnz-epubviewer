---------------------------------------------------------------------------------------------------------------
Basic steps to create and deploy Standalone Epubviewer to different environments: - 
---------------------------------------------------------------------------------------------------------------

- For creating a war file for LOCAL/DEV/UAT/PROD: - 

Configurations to be done in /EpubViewer/src/main/resources/log4j.xml to work in local
------------------------------------------------------------------------------------------------
1) Make sure the param "<param name="File" value="${catalina.base}/logs/epubviewer.log"/>" is set
2) Make sure that the logger level is set to '<level value="debug"/>'

Configurations to be done in /EpubViewer/src/main/webapp/WEB-INF/web.xml to work in local
------------------------------------------------------------------------------------------------
1) The value should be "localhost:8080" for context parameter "viewerHostName"
2) Make sure servlet-mapping for 'localepubviewer' is NOT commented.

Configurations to be done in /EpubViewer/src/main/webapp/epubviewer.jsp to work in local (**Commented at the moment as 'Download File' option is only used via MultiRepIEViewer**)
------------------------------------------------------------------------------------------------
1) The 'Download file' link should be ref="http://<%=hostName%>/epubviewer/localviewer?dps_pid=<%=filePid%>&dps_dvs=<%=rosettaSession%>&download=true"

***************************************************************************************************************************************************************************

Configurations to be done in /EpubViewer/src/main/resources/log4j.xml to work in DEV/UAT/PROD
------------------------------------------------------------------------------------------------
1) Make sure the param <param name="File" value="/exlibris/dps/d4_1/system.dir/thirdparty/jboss/server/default/log/epubviewer.log"/> is set
2) Make sure that the logger level is set to '<level value="info"/>'
 
Configurations to be done in /EpubViewer/src/main/webapp/WEB-INF/web.xml to work in DEV/UAT/PROD
------------------------------------------------------------------------------------------------
1) The value should be set according to the environment for context parameter "viewerHostName"
2) Make sure servlet-mapping for 'localepubviewer' is commented STRICTLY.

Configurations to be done in /EpubViewer/src/main/webapp/epubviewer.jsp to work in DEV/UAT/PROD (**Commented at the moment as 'Download File' option is only used via MultiRepIEViewer**)
---------------------------------------------------------------------------------------------------
1) The 'Download file' link should be ref="http://<%=hostName%>/epubviewer/viewer?dps_pid=<%=filePid%>&dps_dvs=<%=rosettaSession%>&download=true"

***************************************************************************************************************************************************************************

- Run 'mvn clean install' which will create 'epubviewer.war' file.
- Copy the file to '/exlibris/dps/d4_1/system.dir/thirdparty/jboss/server/default/deploy' on respective servers (DEV/UAT/PROD) and deploy the war file using JBoss Management jmx-console