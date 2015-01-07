package com.appspot.getthatpage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GetThatImageServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String urlString = req.getParameter("image-url-input");
		if(urlString == null || !Utils.isSiteUrlValid(urlString)){
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		URL url = new URL(urlString);
		
		byte[] imageData = Utils.getImageBytesFromURL(url);
		
		if(urlString.toLowerCase().endsWith(".png")){
			resp.setContentType("image/png");
		}else if(urlString.toLowerCase().endsWith(".jpg") || urlString.toLowerCase().endsWith(".jpeg")){
			resp.setContentType("image/jpeg");
		}else if(urlString.toLowerCase().endsWith(".bmp")){
			resp.setContentType("image/bmp");
		}else{
			InputStream is = new BufferedInputStream(new ByteArrayInputStream(imageData));
			String contentType = URLConnection.guessContentTypeFromStream(is);
			resp.setContentType(contentType);
		}
		
		resp.setContentLength(imageData.length);
		
		OutputStream out = resp.getOutputStream();
		out.write(imageData);
		out.close();
	}
	
}
