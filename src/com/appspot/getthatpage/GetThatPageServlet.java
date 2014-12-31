package com.appspot.getthatpage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.Blob;

import conf.OfyService;
import entities.ClonedWebSite;
import entities.ClonedWebSiteController;

@SuppressWarnings("serial")
public class GetThatPageServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			String serverHostName = req.getServerName();
			if(serverHostName.toLowerCase().equals("localhost")){
				serverHostName += ":" + req.getServerPort();
			}

			String urlString = req.getParameter("url-input");
			if(!Utils.isValidUrl(urlString)){
				//resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				req.getSession().setAttribute("ex", "HttpServletResponse.SC_BAD_REQUEST");
				resp.sendRedirect("/errorPage.jsp");
				return;
			}

			URL url = new URL(urlString);
			String hostName = url.getHost();

			if(serverHostName.toLowerCase().equals(hostName.toLowerCase())) {
				resp.sendError(HttpServletResponse.SC_CONFLICT);
				return;
			}

			//get html string
			String htmlString = getResponseStringFromURL(url);
			StringBuilder sbHtmlString = new StringBuilder(htmlString);

			String head = Utils.getHtmlHeadPart(sbHtmlString.toString());

			//search for IMG tags and store their URLs
			ArrayList<String> imgTags = Utils.getTagsFromHtml("img", sbHtmlString.toString());
			ArrayList<String> img2src = new ArrayList<String>();
			for(String imgTag : imgTags){
				String src = Utils.getAttributeValueFromTag("src", imgTag);
				String srcValidation = src.toString();

				if(!srcValidation.toLowerCase().startsWith("http")) {
					if(!srcValidation.startsWith("/"))
						srcValidation = "/" + srcValidation;

					srcValidation = "http://" + hostName + srcValidation;
				}

				if(Utils.isValidUrl(srcValidation))
					img2src.add(src);
			}

			//TODO: get images from CSSs


			//get webSite object and find its images
			ClonedWebSite site = ClonedWebSiteController.getClonedWebSite(hostName);
			HashSet<String> falseImageURLs = new HashSet<String>();
			if(site != null){
				//get existing site Blob images and insert new ones
				boolean newImagesAdded = false;
				for (String imgSrc : img2src) {
					if(site.hasImageBlob(imgSrc))
						continue;

					newImagesAdded = true;

					//add new image
					String fullImgUrl = imgSrc.toString();
					if(!fullImgUrl.toLowerCase().startsWith("http")) {
						if(!fullImgUrl.startsWith("/"))
							fullImgUrl = "/" + fullImgUrl;

						fullImgUrl = "http://" + hostName + fullImgUrl;
					}

					Blob imgBlob = Utils.getImageBlobByURL(fullImgUrl);
					if(imgBlob != null && imgBlob.getBytes().length < 1000000) {
						site.addImageBlob(imgSrc, imgBlob);
					}else{
						falseImageURLs.add(imgSrc);
					}
				}

				if(newImagesAdded) {
					try{
						ClonedWebSiteController.saveClonedWebSite(site);
					}catch(Exception ex){
						System.out.println(" >>> NEW IMAGES ARE NOT SAVED FOR: " + hostName);					
						System.out.println(ex.getMessage());
					}
				}
			}else{
				//get Blobs images from URLs and store them in new webSite object
				//then save new webSite object
				HashMap<String, Blob> url2imageMap = new HashMap<String, Blob>();

				for (String imgSrc : img2src) {
					String fullImgUrl = imgSrc.toString();
					if(!fullImgUrl.toLowerCase().startsWith("http")) {
						if(!fullImgUrl.startsWith("/"))
							fullImgUrl = "/" + fullImgUrl;

						fullImgUrl = "http://" + hostName + fullImgUrl;
					}

					Blob img = Utils.getImageBlobByURL(fullImgUrl);
					if(img != null && img.getBytes().length < 1000000) {
						url2imageMap.put(imgSrc, img);
					}else{
						falseImageURLs.add(imgSrc);
					}
				}

				site = new ClonedWebSite(hostName, url2imageMap);			
			}

			//get javaScripts
			ArrayList<String> scriptTags = Utils.getTagsFromHtml("script", sbHtmlString.toString());
			for(String scriptTag : scriptTags) {
				String scriptUrl = Utils.getAttributeValueFromTag("src", scriptTag);
				if(scriptUrl.length() == 0)
					continue;

				if(site.hasScript(scriptUrl))
					continue;

				String srcWithHostname = scriptUrl.toString();
				if(!srcWithHostname.toLowerCase().startsWith("http"))
					srcWithHostname = "http://" + hostName + srcWithHostname;

				try{
					String script = getResponseStringFromURLString(srcWithHostname);
					site.addNewScript(scriptUrl, script);
				}catch(Exception ex) {
					System.out.println("Cannot download script: " + scriptUrl);
				}
			}

			//fix css links
			ArrayList<String> linkTags = Utils.getTagsFromHtml("link", head);	// iz ovog koraka se uzimaju styleesheet-ovi
			ArrayList<String> stylesUrls = Utils.getStylesheetUrlsFromLinks(linkTags);
			for (String cssUrl : stylesUrls) {
				if(site.hasCss(cssUrl))
					continue;

				String cssUrlWithHostName = cssUrl.toString();
				if(!cssUrlWithHostName.toLowerCase().startsWith("http"))
					cssUrlWithHostName = "http://" + hostName + "/" + cssUrl;

				try{
					String css = getResponseStringFromURLString(cssUrlWithHostName);
					site.addNewCss(cssUrl, css);
				}catch(Exception ex) {
					System.out.println("Cannot download css: " + cssUrl);
				}
			}

			//////////////////////////////////////////////////////////////////////////////////////		
			//////////////////      store new site version           /////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////
			OfyService.ofy().save().entity(site).now();
			//site.saveAllData();

			//fix javaScript URLs
			String html = sbHtmlString.toString();
			for (Map.Entry<String, String> entry : site.getUrl2ScriptEntrySet()) {
				String src = entry.getKey();
				String newSrc = String.format("http://%s/script?host=%s&url=%s", serverHostName, hostName, src);
				html = html.replace(src, newSrc);
			}

			//fix css URLs
			for (Map.Entry<String, String> entry : site.getUrl2CssEntrySet()) {
				String cssUrl = entry.getKey();
				String newCssUrl = String.format("http://%s/css?host=%s&url=%s", serverHostName, hostName, cssUrl);
				html = html.replace(cssUrl, newCssUrl);
			}

			//fix img URLs
			for (String src : img2src) {	
				if(falseImageURLs.contains(src))
					continue;

				String newSrc = String.format("http://%s/image?host=%s&url=%s", serverHostName, hostName, src);			
				html = html.replace(src, newSrc);
			}

			sbHtmlString = new StringBuilder(html);

			//fix "internal" URLs
			fixInternalUrls(sbHtmlString, serverHostName);

			//display the page
			resp.getWriter().println(sbHtmlString.toString());
			
		}catch(Exception ex) {
			req.getSession().setAttribute("ex", ex.getMessage());
			resp.sendRedirect("/errorPage.jsp");
		}
	}

	private void fixInternalUrls(StringBuilder sbHtmlString, String serverHostName){
		String fullServerHostName = "http://" + serverHostName;

		int bodyTagStartIndex = Utils.getTagStartIndex("<body", sbHtmlString.toString());
		int aTagLength = 3;
		String aTag = "<a ";
		for(int i = bodyTagStartIndex; i < sbHtmlString.length() - aTagLength; i++){
			char[] tagChars = new char[aTagLength];
			sbHtmlString.getChars(i, i + aTagLength, tagChars, 0);

			String tagStr = new String(tagChars).toLowerCase();
			if(tagStr.equals(aTag)){
				//<a tag found
				for(int j = i + aTagLength; j < sbHtmlString.length() - 5; j++){					
					char[] hrefChars = new char[6];
					sbHtmlString.getChars(j, j + 6, hrefChars, 0);
					String hrefStr = new String(hrefChars).toLowerCase();

					if(hrefStr.equals("href=\"") || hrefStr.equals("\'")){
						String hrefValue = sbHtmlString.substring(j + 6, j + 6 + fullServerHostName.length());

						//if this "<a href" value already stards with my servers URL, dont replace it
						if(!hrefValue.startsWith(fullServerHostName)) {
							sbHtmlString.insert(j + 6, fullServerHostName + "/getthatpage?url-input=");
							break; //this <a tag is fixed, move on to next ones
						}
					}
				}
			}
		}
	}

	private String getResponseStringFromURLString(String urlString) throws MalformedURLException, IOException{
		return getResponseStringFromURL(new URL(urlString));
	}

	private String getResponseStringFromURL(URL url) throws MalformedURLException, IOException{
		URLConnection urlConnection = url.openConnection();
		BufferedReader urlReader = new BufferedReader(
				new InputStreamReader(
						urlConnection.getInputStream(), "UTF-8"));

		StringBuilder sbHtml = new StringBuilder();
		String htmlLine;
		while ((htmlLine = urlReader.readLine()) != null){ 
			sbHtml.append(htmlLine + "\n");
		}

		urlReader.close();

		return sbHtml.toString();
	}
}
