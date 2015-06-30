<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<html>
	<head>
		<title>NDHA Epub Viewer</title>	
	</head>
	<body>
	<%
		String epubFileName = null;
		String filePid = null;
		String rosettaSession = null;
		String hostName = null;
		
		if (request != null) { 
		
			if (request.getAttribute("epubFileName") != null) {
				epubFileName = (String) request.getAttribute("epubFileName");
				if (request.getAttribute("filePid") != null) {
					filePid = (String)request.getAttribute("filePid");
				}
				if (request.getAttribute("rosettaSession") != null) {
					rosettaSession = (String)request.getAttribute("rosettaSession");
				}
				if (request.getAttribute("hostName") != null) {
					hostName = (String)request.getAttribute("hostName");
				}
	%>
		<iframe  width="100%" height="100%" src="http://<%=hostName%>/epubviewer/readiumepubviewer?epub=epubContent/<%=epubFileName%>" style="border:1px #ddd solid;"></iframe>
	<%
			}
		}
	%>
	</body>
</html>