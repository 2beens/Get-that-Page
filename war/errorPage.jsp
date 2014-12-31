<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
	<script src="scripts/2been.js"></script>
	<link rel="stylesheet" href="css/style.css"/>
	
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<title>Monkeys ate your Page :(</title>
</head>
<body style="height: 90%;">
	<div class="navbar">
	    <h1 class="navbar-content" id="get-that-page-heading">Get that page!</h1>
	    <!--<img src="/images/cable.png" />-->
	    <a class="navbar-content" id="dev-tools-link" href="#">DevTools</a>
	</div>
	
	<div id="dev-tools-menu" class="dev-tools-menu-hidden hidden">
		<form class="dev-tools-menu-item" action="/sudologin" id="dev-tools-login-form" method="get">
		    <input class="dev-tools-menu-input dev-tools-menu-item" placeholder="Username" type="text" id="dev-tools-username" name="dev-tools-username"/>
		    <input class="dev-tools-menu-input dev-tools-menu-item" placeholder="Password" type="password" id="dev-tools-pass" name="dev-tools-pass"/>
	  	</form>
	</div>
	
	<div class="main-container" style="height: 900px;">
		<form action="/index.html">
			<button class="main-container-item btn" type="submit">Go Back</button>
		</form>
		<h3 class="main-container-item">An error happen when tryied to fetch that site. Details below:</h3>
		<p class="main-container-item"><%= pageContext.getSession().getAttribute("ex") %></p>
	</div>
	
	<div class="left-panel">
    <div class="left-panel-item left-panel-heading">
      <h3 class="left-panel-heading-item">Cashed sites:</h3>
      <button class="btn left-panel-heading-item" type="button" onclick="showCashedSitesAjax()">Get</button>
    </div>

    <div id="div-cashed-sites-list">
      
    </div>
  </div>
  
  <div class="footer">
    <hr/>
    <p class="footer-content">Yet another simple proxy server. Still in development...</p>
  </div>
	
</body>
</html>