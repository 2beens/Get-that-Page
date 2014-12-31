package conf;

import java.util.List;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import entities.ClonedImage;
import entities.ClonedStringContent;
import entities.ClonedWebSite;

public class OfyService {

	static {
		factory().register(ClonedWebSite.class);
		factory().register(ClonedImage.class);
		factory().register(ClonedStringContent.class);

		setup();
	}

	public static Objectify ofy() {
		return ObjectifyService.ofy();
	}

	public static ObjectifyFactory factory() {
		return ObjectifyService.factory();
	}    

	public static void setup() {   
		boolean ERASE_ALL_DATA = false;

		if(ERASE_ALL_DATA) {
			List<Key<ClonedWebSite>> keys = ofy().load().type(ClonedWebSite.class).keys().list();
			List<Key<ClonedImage>> imagesKeys = ofy().load().type(ClonedImage.class).keys().list();
			List<Key<ClonedStringContent>> contentsKeys = ofy().load().type(ClonedStringContent.class).keys().list();

			System.out.println("SETUP: deleting " + keys.size() + " cloned sites.");
			System.out.println("SETUP: deleting " + imagesKeys.size() + " cloned images.");
			System.out.println("SETUP: deleting " + contentsKeys.size() + " cloned string contents.");

			ofy().delete().keys(imagesKeys).now();
			ofy().delete().keys(keys).now();
			ofy().delete().keys(contentsKeys).now();
		}
	}

}
