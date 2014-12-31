package com.appspot.getthatpage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;

public class Utils {
	public static final String URL_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	public static Pattern urlValidationPattern = null;
	
	public static boolean isValidUrl(String url){
		if(urlValidationPattern == null){
			urlValidationPattern = Pattern.compile(URL_REGEX);
		}

	    Matcher m = urlValidationPattern.matcher(url); 
	    
	    return m.find();
	}
	
	public static ArrayList<String> getStylesheetUrlsFromLinks(ArrayList<String> links){
		ArrayList<String> stylesheets = new ArrayList<String>();
		
		for(int i = 0; i < links.size(); i++){
			String link = links.get(i);
			if(getAttributeValueFromTag("rel", link).toLowerCase().equals("stylesheet")){
				stylesheets.add(getAttributeValueFromTag("href", link));
			}
		}
		
		return stylesheets;
	}
	
	public static String getAttributeValueFromTag(String att, String tag){
		String val = "";
		tag = tag.trim();
		
		String attToSearch1 = att.toLowerCase() + "=\"";
		String attToSearch2 = att.toLowerCase() + "=\'";
		int attToSearchLen = attToSearch1.length();
		int attToSearchStartIndex = -1;
		for(int i = 0; i < tag.length(); i++){
			//we reached the end of the tag ?
			if(i < tag.length() - attToSearchLen){
				char[] attChars = new char[attToSearchLen];
				tag.getChars(i, i + attToSearchLen, attChars, 0);			
				
				String attStr = new String(attChars).toLowerCase();
				if(attStr.equals(attToSearch1) || attStr.equals(attToSearch2)){
					attToSearchStartIndex = i + attToSearchLen;
				}else if(attToSearchStartIndex >= 0 && i >= attToSearchStartIndex + attToSearchLen && 
						(attStr.startsWith("\"") || attStr.startsWith("\'"))){
					val = tag.substring(attToSearchStartIndex, i);
					break;
				}
			}else if(attToSearchStartIndex >= 0 && i >= attToSearchStartIndex + attToSearchLen && 
					(tag.charAt(i) == '\"' || tag.charAt(i) == '\'')){
				val = tag.substring(attToSearchStartIndex, i);
				break;
			}		
		}
		
		return val;
	}
	
	public static ArrayList<String> getTagsFromHtml(String tag, String html){
		ArrayList<String> tags = new ArrayList<String>();
		
		tag = tag.toLowerCase();
		int tagBeginIndex = -1;
		int tagEndIndex = -1;
		int tagLength = tag.length() + 2;
		for(int i = 0; i < html.length() - tagLength; i++){		
			char[] tagChars = new char[tagLength];
			html.getChars(i, i + tagLength, tagChars, 0);
			
			String tagStr = new String(tagChars).toLowerCase();
			if(tagStr.equals("<" + tag + " ")){
				tagBeginIndex = i + tagLength;
			}else if(tagBeginIndex >= 0 && tagStr.startsWith(">")){
				tagEndIndex = i;
				tags.add(html.substring(tagBeginIndex, tagEndIndex));
				tagBeginIndex = -1;
			}
		}
			
		return tags;
	}
	
	public static String getHtmlHeadPart(String html){
		return getHtmlPartByTag("head", html);
	}
	
	public static String getHtmlBodyPart(String html){
		return getHtmlPartByTag("body", html);
	}
	
	private static String getHtmlPartByTag(String tag, String html){
		int tagBeginIndex = -1;
		int tagEndIndex = -1;			
		
		for(int i = 0; i < html.length() - 6; i++){		
			char[] tagChars = new char[6];
			html.getChars(i, i + 6, tagChars, 0);			
			
			String tagStr = new String(tagChars);
			//if(headStr.equals("<head>")){
			if(tagStr.equals("<" + tag + ">")){
				tagBeginIndex = i + 6;
			}else if(tagStr.equals("</" + tag)){
				tagEndIndex = i;
				break;
			}
		}
		
		return html.substring(tagBeginIndex, tagEndIndex);
	}
	
	public static int getTagStartIndex(String tag, String html){
		int tagContentStartIndex = -1;
		
		int headStringLen = tag.length();
		for(int i = 0; i < html.length() - headStringLen; i++){
			char[] tagChars = new char[headStringLen];
			html.getChars(i, i + headStringLen, tagChars, 0);
			
			String tagString = new String(tagChars).toLowerCase();
			if(tagString.equals(tag)){
				tagContentStartIndex = i + headStringLen;
				break;
			}
		}
		
		return tagContentStartIndex;
	}
	
	public static byte[] getImageBytesFromURL(URL url) throws IOException {
		InputStream is = null;
		byte[] imageBytes = null;
		
		try {
			is = url.openStream();
			imageBytes = IOUtils.toByteArray(is);
			
			Image img = ImagesServiceFactory.makeImage(imageBytes);
			System.out.println(String.format("---> Image received [%s] size [%d] [%s]", img.getFormat().name(), img.getImageData().length, url.toString()));
		}
		catch (IOException e) {
			System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
			e.printStackTrace ();
		}
		finally {
			if (is != null) { 
				is.close(); 
			}
		}
		
		return imageBytes;
	}


	public static Blob getImageBlobByURL(String imgSrc) {
		Blob imgBlob = null;
		
		try{
			byte[] imgBytes = getImageBytesFromURL(new URL(imgSrc));
			imgBlob = new Blob(imgBytes);			
		}catch(Exception ex){			
			if(ex.getClass().equals(IOException.class)) {
				IOException ioEx = (IOException)ex;
				System.out.println(ioEx.getMessage());
			}else{
				System.out.println(ex.getMessage());
			}
		}
		
		return imgBlob;
	}
}
