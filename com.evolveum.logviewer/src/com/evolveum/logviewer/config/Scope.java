package com.evolveum.logviewer.config;

public enum Scope { 
	
	LINE, ENTRY, HEADER;

	public static Scope parse(String text) {
		if (text == null || text.isEmpty()) {
			return null;
		} else if ("line".equals(text)) {
			return LINE;
		} else if ("entry".equals(text)) {
			return ENTRY;
		} else if ("header".equals(text)) {
			return HEADER;
		} else {
			throw new IllegalArgumentException("Scope: " + text);
		}
	} 
}