package com.evolveum.logviewer.config;

public class OutlineInstruction {

	private int level;
	private String text;
	private String regexp;
	private String label;
	
	public static OutlineInstruction parseFromLine(String line) {
		line = line.trim();
		
		int space1 = line.indexOf(' ');
		if (space1 < 0) {
			System.out.println("Couldn't parse outline instruction: " + line);
			return null;
		}
		int space2 = line.indexOf(' ', space1+1);
		if (space2 < 0) {
			System.out.println("Couldn't parse outline instruction: " + line);
			return null;
		}
		int separator = line.charAt(space2+1);
		int nextSeparator = line.indexOf(separator, space2+2);
		if (nextSeparator < 0) {
			System.out.println("Couldn't parse outline instruction: " + line);
			return null;			
		}
		
		OutlineInstruction rv = new OutlineInstruction();
		
		try {
			rv.level = Integer.parseInt(line.substring(space1+1, space2));
		} catch (NumberFormatException e) {
			System.out.println("Couldn't parse outline instruction: " + line + ": " + e);
			return null;
		}

		String string1 = line.substring(space2+2, nextSeparator);
		if (separator == '/') {
			rv.regexp = string1;
		} else {
			rv.text = string1;
		}
		
		int separator2 = line.charAt(nextSeparator+2);
		int nextSeparator2 = line.indexOf(separator2, nextSeparator+3);
		if (nextSeparator2 < 0) {
			System.out.println("Couldn't parse outline instruction: " + line);
			return null;			
		}
		rv.label = line.substring(nextSeparator+2, nextSeparator2);
				
		return rv;		
	}

	public int getLevel() {
		return level;
	}

	public String getText() {
		return text;
	}

	public String getRegexp() {
		return regexp;
	}

	public String getLabel() {
		return label;
	}
	
}
