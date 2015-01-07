package com.appspot.getthatpage;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.appspot.getthatpage.conf.OfyService;
import com.appspot.getthatpage.entities.ClonedImage;
import com.appspot.getthatpage.entities.ClonedStringContent;
import com.appspot.getthatpage.entities.ClonedWebSite;
import com.googlecode.objectify.Key;

@SuppressWarnings("serial")
public class DeleteAllCachedContent extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		List<Key<ClonedWebSite>> keys = OfyService.ofy().load().type(ClonedWebSite.class).keys().list();
		List<Key<ClonedImage>> imagesKeys = OfyService.ofy().load().type(ClonedImage.class).keys().list();
		List<Key<ClonedStringContent>> contentsKeys = OfyService.ofy().load().type(ClonedStringContent.class).keys().list();

		String log = "deleting " + keys.size() + " cloned sites. <br/>";
		log += "deleting " + imagesKeys.size() + " cloned images.  <br/>";
		log += "deleting " + contentsKeys.size() + " cloned string contents. <br/>";
		
		OfyService.ofy().delete().keys(imagesKeys).now();
		OfyService.ofy().delete().keys(keys).now();
		OfyService.ofy().delete().keys(contentsKeys).now();
		
		req.getSession().setAttribute("ex", log);
		resp.sendRedirect("/errorPage.jsp");
	}
}
