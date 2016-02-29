package com.evolveum.logviewer.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Operator {

	CONTAINING, NOT_CONTAINING, MATCHING, NOT_MATCHING;

	public static Operator parse(String text) {
		if ("containing".equals(text)) {
			return CONTAINING;
		} else if ("not-containing".equals(text)) {
			return NOT_CONTAINING;
		} else if ("matching".equals(text)) {
			return MATCHING;
		} else if ("not-matching".equals(text)) {
			return NOT_MATCHING;
		} else {
			throw new IllegalArgumentException("Operator: " + text);
		}
	}

	public boolean matches(String block, String substring, Pattern pattern) {
		if (block == null) {
			return false;		// TODO (or 'true' for negative conditions?)
		}
		switch (this) {
		case CONTAINING: return block.contains(substring);
		case NOT_CONTAINING: return !block.contains(substring);
		case MATCHING: return pattern.matcher(block).matches();
		case NOT_MATCHING: return !pattern.matcher(block).matches();
		default: throw new IllegalStateException();
		}
	}
	
	public Matcher matchingMatcher(String block, String substring, Pattern pattern) {
		if (block == null) {
			return null;
		}
		switch (this) {
		case CONTAINING: return null;
		case NOT_CONTAINING: return null;
		case MATCHING: 
			Matcher matcher = pattern.matcher(block);
			if (matcher.matches()) {
				return matcher;
			} else {
				return null;
			}
		case NOT_MATCHING: return null;
		default: throw new IllegalStateException();
		}
	}

	
	public boolean isRegex() {
		return this == MATCHING || this == NOT_MATCHING;
	}

}