<html>
<head>
<title>Error - National Library of New Zealand</title>
<style>
p {
  color:red;
  text-align:center;
  font-size:20
}
</style>
</head>

<body>
<img src="images/ndha_logo.gif" alt="logo">
<br /><br /><br /><br />
<%
if(request != null && request.getAttribute("errorMessage") != null){
%>
<p>
	<b>Error: </b><%=request.getAttribute("errorMessage")%>
</p>
<%
}
%>
</body>
</html>