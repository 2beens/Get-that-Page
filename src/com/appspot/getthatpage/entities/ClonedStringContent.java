package com.appspot.getthatpage.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class ClonedStringContent {
	@Id 
	Long id;
	
	String content;
	
	public ClonedStringContent() { }
	
	public ClonedStringContent(String content) {
		this.content = content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}
}
