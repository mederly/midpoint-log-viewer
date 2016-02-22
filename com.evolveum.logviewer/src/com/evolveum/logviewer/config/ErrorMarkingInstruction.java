package com.evolveum.logviewer.config;

public class ErrorMarkingInstruction {

	public enum Type { COLLAPSE, EXPAND };
	public enum When { CONTAINING, NOT_CONTAINING };

	private boolean enable;
	private When when;
	private String what;
	
	// like %error-marking off containing 'ConnectorFactoryIcfImpl): Provided Icf connector path '
	
	public boolean isEnable() {
		return enable;
	}

	public When getWhen() {
		return when;
	}
	
	public String getWhat() {
		return what;
	}

	public static ErrorMarkingInstruction parseFromLine(String line) {
		line = line.trim();
		
		int space1 = line.indexOf(' ');
		if (space1 < 0) {
			System.out.println("Couldn't parse error marking instruction: " + line);
			return null;
		}
		int space2 = line.indexOf(' ', space1+1);
		if (space2 < 0) {
			System.out.println("Couldn't parse error marking instruction: " + line);
			return null;
		}

		ErrorMarkingInstruction rv = new ErrorMarkingInstruction();
		
		String onOffStr = line.substring(space1+1, space2);
		if (onOffStr.equals("on")) {
			rv.enable = true;
		} else if (onOffStr.equals("off")) {
			rv.enable = false;
		} else {
			System.err.println("Unknown error marking instr on/off type: " + line);
			return null;
		}
		
		int space3 = line.indexOf(' ', space2+1);
		if (space3 < 0) {
			return rv;
		}
		
		String whenStr = line.substring(space2+1, space3);
		if (whenStr.equals("containing")) {
			rv.when = When.CONTAINING;
		} else if (whenStr.equals("not-containing")) {
			rv.when = When.NOT_CONTAINING;
		} else {
			System.err.println("Unknown error marking instr when clause: " + line);
			return null;
		}
		
		int separator = line.charAt(space3+1);
		int nextSeparator = line.indexOf(separator, space3+2);
		if (nextSeparator < 0) {
			System.out.println("Couldn't parse error marking instruction: " + line);
			return null;			
		}
		
		rv.what = line.substring(space3+2, nextSeparator);
		if (rv.what.isEmpty()) {
			System.err.println("Empty string to match in error marking instr: " + line);
			return null;			
		}
		
		return rv;		
	}

	public boolean matches(String line) {
		switch (when) {
		case CONTAINING: return line.contains(what);
		case NOT_CONTAINING: return !line.contains(what);
		default: throw new IllegalArgumentException("unknown when: " + when);
		}
	}
	
}
