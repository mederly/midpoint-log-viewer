package com.evolveum.logviewer.config;

public class KillInstruction {

	public enum When { CONTAINING, NOT_CONTAINING, LOG_LINE_CONTAINING, LOG_LINE_NOT_CONTAINING };
	
	When when;
	String string;
	
	// like %kill containing "(com.evolveum.midpoint.provisioning.impl.ResourceManager)"
	
	public static KillInstruction parseFromLine(String line) {
		line = line.trim();
		
		int space1 = line.indexOf(' ');
		if (space1 < 0) {
			System.out.println("Couldn't parse kill instruction: " + line);
			return null;
		}
		int space2 = line.indexOf(' ', space1+1);
		if (space2 < 0) {
			System.out.println("Couldn't parse kill instruction: " + line);
			return null;
		}
		int separator = line.charAt(space2+1);
		int nextSeparator = line.indexOf(separator, space2+2);
		if (nextSeparator < 0) {
			System.out.println("Couldn't parse kill instruction: " + line);
			return null;			
		}
		
		KillInstruction rv = new KillInstruction();
		
		String typeStr = line.substring(0, space1);
		if (!typeStr.equals("%kill")) {
			System.err.println("Unknown kill instr type: " + line);
			return null;
		}
		
		String whenStr = line.substring(space1+1, space2);
		if (whenStr.equals("containing")) {
			rv.when = When.CONTAINING;
		} else if (whenStr.equals("not-containing")) {
			rv.when = When.NOT_CONTAINING;
		} else if (whenStr.equals("log-line-containing")) {
			rv.when = When.LOG_LINE_CONTAINING;
		} else if (whenStr.equals("log-line-not-containing")) {
			rv.when = When.LOG_LINE_NOT_CONTAINING;
		} else {
			System.err.println("Unknown kill instr when clause: " + line);
			return null;
		}
		
		rv.string = line.substring(space2+2, nextSeparator);
		if (rv.string.isEmpty()) {
			System.err.println("Empty string to match in kill instr: " + line);
			return null;			
		}
		
		return rv;		
	}

	public boolean appliesTo(String logLine, String wholeEntry) {
		switch (when) {
			case CONTAINING: return wholeEntry.contains(string);
			case NOT_CONTAINING: return !wholeEntry.contains(string);
			case LOG_LINE_CONTAINING: return logLine.contains(string);
			case LOG_LINE_NOT_CONTAINING: return !logLine.contains(string);
			default:
				System.err.println("Unknown when: " + when);
				return false;
		}
	}	
	
}
