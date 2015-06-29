# nlnz-epubviewer
Epub Viewer (referred as "viewer") is a stand-alone java web application for viewing
epub files using a web browser (Chrome/Firefox/IE).

The primary intent for this application is to be used as a file viewer within
Rosetta (Exlibris). It can also be used as an IE viewer with some additional
code within the Rosetta environment which has not been implemented yet.

The viewer receives http requests through Rosetta (DeliveryManagerServlet)
with the IE PID as the request parameter, checks for all the rules and identifies
the epub file from the requested IE PID. The viewer then explodes the epub file
into the deployed (war) location of the viewer within Rosetta where it has
write permissions. The ReadiumJS script which is part of the viewer is then used to
access the exploded content on the server from the client's web page.

If any restricted item/content were exploded on the server for viewing, then it
would pose the risk of being made available to the public as the service is accessible
to everyone on the web thus violating the restriction terms placed on the item.

To handle the security concerns while accessing restricted items, a filter rule
has been added within the viewer (web.xml) to handle the http requests to access
the epub content from the exploded site. The filter servlet checks for a valid
session value before forwarding the request for delivering the content and
ignores requests which do not have a valid session value. 

If the viewer received a request for the same IE PID and if the epub file
was exploded already, it would simply use the exploded copy rather than using
a new copy.

There is a disk maintenance model built into the viewer to ensure that the exploded
epub contents does not stay there for too long and consume disk space. Whenever a new
request is received, a check is made to clear all the previously exploded files
older than 1 or more days. If the cleanup process has been completed once per day,
then the viewer does not perform the cleanup process again for that day.

Alternatively, the application can be deployed independently to service the
viewing of epub files by removing the dependencies to Rosetta and also by
referencing the location of the epub files and adding an identifier to locate
the epub file requested.

Example of the viewer independent of Rosetta:
	- http://localhost:8080/epubviewer/localviewer?dps_pid=<filename>
	  (where filename is the EPUB file without .epub extension)

The web request to the viewer defines 3 parameters:
	- dps_pid : parameter to indicate the IE PID or epub file identifier
	- dps_dvs : parameter to indicate the Rosetta session (optional)
	- download : parameter to indicate if the file should be available for download
	  (value=true|false)(optional)

Examples of the viewer within National Library of New Zealand:
	- http://ndhadeliver.natlib.govt.nz/delivery/DeliveryManagerServlet?dps_pid=IE18254929
	- http://ndhadeliver.natlib.govt.nz/delivery/DeliveryManagerServlet?dps_pid=IE18223617

# Dependencies:
	ReadiumJS (Version 0.14.1, Released 01.May.2014)
	DeliveryAccessWS-3.1.jar (Rosetta Delivery Access Web Services)
	log4j-1.2.15.jar

# Built using:
	Maven
	JDK 6.0

# Tested with:
	- Apache Tomcat
	- JBoss

# Best viewed with:
	- Chrome
	- Firefox 24+
	- Internet Explorer 10+
	- Opera

# Developer Documentation:
	- Refer the included Developer_Guide.txt for more information

# Authors and Contributors include:
	- Mathachan Kulathinal
	- Preeti Badle
	- Lithil Kuriakose

# Instituition:
	National Library of New Zealand,
	Department of Internal Affairs, New Zealand

# Created:
	22.Jul.2014

# Last Updated:
	29.Jun.2015
