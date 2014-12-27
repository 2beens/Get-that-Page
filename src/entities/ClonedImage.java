package entities;

import com.google.appengine.api.datastore.Blob;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class ClonedImage {
	@Id 
	Long id;
	
	Blob imageBlob;
	
	public ClonedImage() { }
	
	public ClonedImage(Blob imageBlob) { 
		this.imageBlob = imageBlob;
	}
	
	public ClonedImage(byte[] imageData) {
		this.imageBlob = new Blob(imageData);
	}
}
