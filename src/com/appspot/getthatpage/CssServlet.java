package com.appspot.getthatpage;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import entities.ClonedWebSite;
import entities.ClonedWebSiteController;

@SuppressWarnings("serial")
public class CssServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String url = req.getParameter("url");
		String host = req.getParameter("host");
		
		if(url == null || host == null) {
			System.out.println("GET CSS ERROR: parameters to get the css are not valid!");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		String urlValidate = url.toString();
		if(urlValidate.charAt(0) != '/')
			urlValidate = "/" + urlValidate;
		
		if(!urlValidate.toLowerCase().startsWith("http")){
			urlValidate = "http://" + host + urlValidate;
		}
		
		if(!Utils.isValidUrl(urlValidate)){
			System.out.println("GET CSS ERROR: css url not valid: " + urlValidate);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		ClonedWebSite webSite = ClonedWebSiteController.getClonedWebSite(host);
		
		if(webSite == null) {
			System.out.println("GET CSS ERROR: Cannot find webSite: " + host);
			System.out.println("GET CSS ERROR css: " + url);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		String css = webSite.getCss(url);
		
		if(css == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			System.out.println("GET CSS ERROR: Cannot find css: " + url);
			return;
		}
		
		resp.setContentType("text/css");
		resp.getWriter().println(css);
	}
	
}
