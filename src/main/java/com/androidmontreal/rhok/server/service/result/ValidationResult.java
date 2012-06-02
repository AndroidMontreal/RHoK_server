package com.androidmontreal.rhok.server.service.result;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>A generic JSR 303 validation result structure. Holds the name of the 
 * problematic field, with an associated error message. The field name
 * should match the JSON field name, so this should be easy to work with 
 * from the service client's perspective.
 */
@XmlRootElement
public class ValidationResult {
	
	String fieldName ;
	
	/**
	 * <p>I believe we'll eventually want to work with localized messages. 
	 * This way we avoid having to re-deploy new clients if we want to add
	 * validation rules on certain fields. Should make us more flexible.
	 * <p>For now english-only. We'll need the clients to give us desired locale 
	 * info to change this. Probably a good candidate for x-cutting.
	 */
	String message ;
	
	public String getFieldName() {
		return fieldName;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
}

