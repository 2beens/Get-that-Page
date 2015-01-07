package com.appspot.getthatpage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.appspot.getthatpage.conf.OfyService;
import com.appspot.getthatpage.entities.ClonedWebSite;
import com.appspot.getthatpage.entities.ClonedWebSiteController;
import com.google.appengine.api.datastore.Blob;

@SuppressWarnings("serial")
public class GetThatPageViaJsoupServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String serverHostName = req.getServerName();
		if(serverHostName.toLowerCase().equals("localhost")){
			serverHostName += ":" + req.getServerPort();
		}
		
		String urlString = req.getParameter("url-input");
		if(!Utils.isSiteUrlValid(urlString)){
			req.getSession().setAttribute("ex", "HttpServletResponse.SC_BAD_REQUEST");
			resp.sendRedirect("/errorPage.jsp");
			return;
		}
		
		try {
			URL url = new URL(urlString);
			String hostName = url.getHost();
			
			//get webSite object and find its images
			ClonedWebSite site = ClonedWebSiteController.getClonedWebSite(hostName);
			
			Document html = Jsoup.connect(url.toString()).get();
			Element head = html.getElementsByTag("head").first();
			Element body = html.getElementsByTag("body").first();
			
			//get and fix scripts
			Elements scripts = head.getElementsByTag("script");
			for(Element script : scripts) {
				String scriptSrc = script.attr("src");
				if(scriptSrc.length() == 0)
					continue;				
				
				if(!site.hasScript(scriptSrc)) {
					String scriptData = getResponseFromUrl(script.attr("abs:src"));
					if(scriptData.length() > 0)
						site.addNewScript(scriptSrc, scriptData);
				}
				
				String newSrc = String.format("http://%s/script?host=%s&url=%s", serverHostName, hostName, scriptSrc);
				script.attr("src", newSrc);
			}
			
			//get and fix stylesheets
			Elements links = head.getElementsByTag("link");
			for(Element css : links) {
				if(!css.attr("rel").toLowerCase().equals("stylesheet"))
					continue;
				
				String cssHref = css.attr("href");
				if(cssHref.length() == 0)
					continue;				
				
				if(!site.hasCss(cssHref)) {
					String cssData = getResponseFromUrl(css.attr("abs:href"));
					if(cssData.length() > 0)
						site.addNewCss(cssHref, cssData);
				}
				
				String newCssUrl = String.format("http://%s/css?host=%s&url=%s", serverHostName, hostName, cssHref);
				css.attr("href", newCssUrl);
			}
			
			//fix background images
			Elements internalStyles = head.getElementsByTag("style");
			for(Element css : internalStyles) {
				String cssData = css.data();
				ArrayList<String> backgroundImagesFromHead = Utils.getBackgroundImagesURLsFromSource(cssData);
				
				for(String imgUrl : backgroundImagesFromHead) {
					Blob imgBlob = Utils.getImageBlobByURL(imgUrl);
					if(imgBlob != null && imgBlob.getBytes().length < 1000000) {
						site.addImageBlob(imgUrl, imgBlob);
					}
				}
				
				for(String imgUrl : backgroundImagesFromHead) {
					String newSrc = String.format("http://%s/image?host=%s&url=%s", serverHostName, hostName, imgUrl);
					css.html(cssData.replace(imgUrl, newSrc));
				}
			}
			
			//get and fix images
			Elements images = body.getElementsByTag("img");
			for(Element image : images) {
				String imgSrc = image.attr("src");
				
				if(!site.hasImageBlob(imgSrc)){
					Blob imgBlob = Utils.getImageBlobByURL(image.attr("abs:src"));
					if(imgBlob == null)
						continue;
				
					if(imgBlob.getBytes().length < 1000000)
						site.addImageBlob(imgSrc, imgBlob);
				}
				
				String newImgSrc = String.format("http://%s/image?host=%s&url=%s", serverHostName, hostName, imgSrc);
				image.attr("src", newImgSrc);
			}
			
			//fix links
			Elements allLinks = body.getElementsByTag("a");
			for(Element link : allLinks) {
				String href = link.attr("href");
				String newHref = String.format("http://%s/getjsoupage?url-input=%s", serverHostName, href);
				link.attr("href", newHref);
			}
			
			OfyService.ofy().save().entity(site).now();
			
			resp.getWriter().println(html.html());
			
		}catch(Exception ex) {
			String exMessage = ex.getMessage() + "<br/>";
			for(StackTraceElement ste : ex.getStackTrace()) {
				exMessage += "at " + ste.getLineNumber() + ",\t" + ste.getFileName() + " >\t" + ste.getMethodName() + "<br/>";
			}
			
			req.getSession().setAttribute("ex", exMessage);
			resp.sendRedirect("/errorPage.jsp");
		}
	}
	
	private String getResponseFromUrl(String url) {
		String resp = "";
		
		try {
			resp = Utils.getResponseStringFromURLString(url);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return resp;
	}
	
}