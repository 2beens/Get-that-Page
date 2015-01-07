package com.appspot.getthatpage.entities;

import static com.appspot.getthatpage.conf.OfyService.ofy;

import java.util.List;

import com.googlecode.objectify.Key;

public class ClonedWebSiteController {
	
	public static ClonedWebSite getClonedWebSite(String hostName){
		ClonedWebSite site = ofy().load().type(ClonedWebSite.class).filter("hostName", hostName).first().now();
		
		if(site == null) {
			site = new ClonedWebSite(hostName);
		}
		
		return site;
	}
	
	public static List<ClonedWebSite> getAllClonedWebSites() {
		return ofy().load().type(ClonedWebSite.class).list();
	}
	
	public static boolean saveClonedWebSite(ClonedWebSite webSite) {
		Key<ClonedWebSite> res = ofy().save().entity(webSite).now();
		
		if(res == null)
			return false;
		
		return true;
	}
}
