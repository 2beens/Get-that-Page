package com.appspot.getthatpage;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.appspot.getthatpage.entities.ClonedWebSite;
import com.appspot.getthatpage.entities.ClonedWebSiteController;

@SuppressWarnings("serial")
public class ScriptServlet extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
			
		String url = req.getParameter("url");
		String host = req.getParameter("host");
		
		if(url == null || host == null) {
			System.out.println("GET SCRIPT ERROR: parameters to get the script are not valid!");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		String urlValidate = url.toString();
		if(urlValidate.charAt(0) != '/')
			urlValidate = "/" + urlValidate;
		
		if(!urlValidate.toLowerCase().startsWith("http")){
			urlValidate = "http://" + host + urlValidate;
		}
		
		if(!Utils.isSiteUrlValid(urlValidate)){
			System.out.println("GET SCRIPT ERROR: script url not valid: " + urlValidate);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		ClonedWebSite webSite = ClonedWebSiteController.getClonedWebSite(host);
		
		if(webSite == null) {
			System.out.println("GET SCRIPT ERROR: Cannot find webSite: " + host);
			System.out.println("GET SCRIPT ERROR script: " + url);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		String script = webSite.getScript(url);
		
		if(script == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			System.out.println("GET SCRIPT ERROR: Cannot find script: " + url);
			return;
		}
		
		resp.setContentType("text/javascript");
		resp.getWriter().println(script);
	}
	
}
