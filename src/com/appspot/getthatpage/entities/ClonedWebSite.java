package com.appspot.getthatpage.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.appspot.getthatpage.conf.OfyService;
import com.google.appengine.api.datastore.Blob;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class ClonedWebSite {

	@Id 
	Long id;

	@Index
	String hostName;

	HashMap<String, Ref<ClonedImage>> url2imageMap;
	
	HashMap<String, Ref<ClonedStringContent>> url2scriptMap;
	
	HashMap<String, Ref<ClonedStringContent>> url2cssMap;

	public ClonedWebSite() { 
		this.url2imageMap = new HashMap<String, Ref<ClonedImage>>();
		this.url2scriptMap = new HashMap<String, Ref<ClonedStringContent>>();
		this.url2cssMap = new HashMap<String, Ref<ClonedStringContent>>();
	}
	
	public ClonedWebSite(String hostName, HashMap<String, Blob> url2imageMap) {
		this.hostName = hostName;
		this.url2imageMap = new HashMap<String, Ref<ClonedImage>>();
		this.url2scriptMap = new HashMap<String, Ref<ClonedStringContent>>();
		this.url2cssMap = new HashMap<String, Ref<ClonedStringContent>>();
		
		//TODO: saving images should be moved to some kind of data-controller-layer maybe...
		HashMap<String, ClonedImage> clonedImagesToSave = new HashMap<String, ClonedImage>();
		for (Map.Entry<String, Blob> entry : url2imageMap.entrySet()) {
			ClonedImage ci = new ClonedImage(entry.getValue());
			clonedImagesToSave.put(entry.getKey(), ci);
		}		
		
		OfyService.ofy().save().entities(clonedImagesToSave.values()).now();
		for (Map.Entry<String, ClonedImage> entry : clonedImagesToSave.entrySet()) {
			this.url2imageMap.put(entry.getKey(), Ref.create(entry.getValue()));
		}
	}
	
	public Set<Entry<String, String>> getUrl2ScriptEntrySet() {
		HashMap<String, String> url2scriptMap = new HashMap<String, String>();
		
		for (Map.Entry<String, Ref<ClonedStringContent>> entry : this.url2scriptMap.entrySet()) {
			url2scriptMap.put(entry.getKey(), entry.getValue().get().getContent());
		}
		
		return url2scriptMap.entrySet();
	}

	public Set<Entry<String, String>> getUrl2CssEntrySet() {
		HashMap<String, String> url2cssMap = new HashMap<String, String>();
		
		for (Map.Entry<String, Ref<ClonedStringContent>> entry : this.url2cssMap.entrySet()) {
			url2cssMap.put(entry.getKey(), entry.getValue().get().getContent());
		}
		
		return url2cssMap.entrySet();
	}

	public boolean hasScript(String scriptUrl) {
		return this.url2scriptMap.containsKey(scriptUrl);
	}

	public boolean hasCss(String cssUrl) {
		return this.url2cssMap.containsKey(cssUrl);
	}
	
	public String getScript(String scriptUrl) {
		return this.url2scriptMap.get(scriptUrl).get().getContent();
	}

	public String getCss(String cssUrl) {
		return this.url2cssMap.get(cssUrl).get().getContent();
	}
	
	public void addNewScript(String scriptUrl, String script) {
		ClonedStringContent scriptContent = new ClonedStringContent(script);
		OfyService.ofy().save().entity(scriptContent).now();
		
		this.url2scriptMap.put(scriptUrl, Ref.create(scriptContent));
		OfyService.ofy().save().entity(this).now();
	}

	public void addNewCss(String cssUrl, String css) {
		ClonedStringContent cssContent = new ClonedStringContent(css);
		OfyService.ofy().save().entity(cssContent).now();
		
		this.url2cssMap.put(cssUrl, Ref.create(cssContent));
		OfyService.ofy().save().entity(this).now();
	}
	
	public int getNumberOfScripts() {
		return this.url2scriptMap.size();
	}
	
	public String getHostName() {
		return this.hostName;
	}

	public HashMap<String, Blob> getUrl2ImageMap() {
		HashMap<String, Blob> url2ImageMap = new HashMap<String, Blob>();
		
		for (Map.Entry<String, Ref<ClonedImage>> entry : url2imageMap.entrySet()) {
			url2ImageMap.put(entry.getKey(), entry.getValue().get().imageBlob);
		}
		
		return url2ImageMap;
	}
	
	public boolean hasImageBlob(String url) {
		return url2imageMap.containsKey(url);
	}
	
	public void addImageBlob(String url, Blob imgBlob) {
		ClonedImage ci = new ClonedImage(imgBlob);
		OfyService.ofy().save().entity(ci).now();
		
		url2imageMap.put(url, Ref.create(ci));
		
		OfyService.ofy().save().entity(this).now();
	}
	
	public Blob getImageBlob(String url){
		if(url2imageMap.containsKey(url)){
			return url2imageMap.get(url).get().imageBlob;
		}
		
		return null;
	}
	
	/*
	public void saveAllData() {
		ArrayList<ClonedImage> clonedImages = new ArrayList<ClonedImage>();
		for (Map.Entry<String, Ref<ClonedImage>> entry : url2imageMap.entrySet()) {
			clonedImages.add(entry.getValue().get());
		}
		OfyService.ofy().save().entities(clonedImages).now();
		
		ArrayList<ClonedStringContent> clonedScripts = new ArrayList<ClonedStringContent>();
		for(Map.Entry<String, Ref<ClonedStringContent>> entry : url2scriptMap.entrySet()) {
			clonedScripts.add(entry.getValue().get());
		}
		OfyService.ofy().save().entities(clonedScripts).now();
		
		ArrayList<ClonedStringContent> clonedStylesheets = new ArrayList<ClonedStringContent>();
		for(Map.Entry<String, Ref<ClonedStringContent>> entry : url2cssMap.entrySet()) {
			clonedStylesheets.add(entry.getValue().get());
		}
		OfyService.ofy().save().entities(clonedStylesheets).now();
		
		OfyService.ofy().save().entity(this).now();
	}
	*/
}
