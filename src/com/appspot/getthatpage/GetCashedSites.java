package com.appspot.getthatpage;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.appspot.getthatpage.entities.ClonedWebSite;
import com.appspot.getthatpage.entities.ClonedWebSiteController;

@SuppressWarnings("serial")
public class GetCashedSites extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		List<ClonedWebSite> clonedWebSites = ClonedWebSiteController.getAllClonedWebSites();
		
		String retVal;
		if(clonedWebSites.size() == 0) {
			retVal = "No sites cashed yet...";
		}else {
			retVal = "<ul>";
			for(ClonedWebSite site : clonedWebSites) {
				retVal += "<li>" + site.getHostName() + "</li>";
			}
			retVal += "</ul>";
		}
		
		resp.getWriter().println(retVal);
	}
	
}
