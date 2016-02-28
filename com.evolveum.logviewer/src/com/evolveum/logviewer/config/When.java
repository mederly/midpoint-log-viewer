package com.evolveum.logviewer.config;

public enum When {
	CONTAINING, NOT_CONTAINING, LOG_LINE_CONTAINING, LOG_LINE_NOT_CONTAINING, REGEXP;

	public static When fromString(String text) {
		if (text == null || "".equals(text)) {
			return null;
		} else if ("containing".equalsIgnoreCase(text)) {
			return CONTAINING;
		} else if ("not-containing".equalsIgnoreCase(text)) {
			return NOT_CONTAINING;
		} else if ("log-line-containing".equalsIgnoreCase(text)) {
			return LOG_LINE_CONTAINING;
		} else if ("log-line-not-containing".equalsIgnoreCase(text)) {
			return LOG_LINE_NOT_CONTAINING;
		} else if ("regexp".equalsIgnoreCase(text)) {
			return REGEXP;
		} else {
			throw new IllegalArgumentException("Unsupported value: " + text);
		}
	}
}
