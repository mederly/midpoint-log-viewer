package com.evolveum.logviewer.config;

public class FoldingInstruction {

	public enum Type { COLLAPSE, EXPAND };
	public enum When { CONTAINING, NOT_CONTAINING };
	
	public Type type;
	public When when;
	public String string;
	
	// like %collapse containing "(com.evolveum.midpoint.provisioning.impl.ResourceManager)"
	
	public static FoldingInstruction parseFromLine(String line) {
		line = line.trim();
		
		int space1 = line.indexOf(' ');
		if (space1 < 0) {
			System.out.println("Couldn't parse folding instruction: " + line);
			return null;
		}
		int space2 = line.indexOf(' ', space1+1);
		if (space2 < 0) {
			System.out.println("Couldn't parse folding instruction: " + line);
			return null;
		}
		int separator = line.charAt(space2+1);
		int nextSeparator = line.indexOf(separator, space2+2);
		if (nextSeparator < 0) {
			System.out.println("Couldn't parse folding instruction: " + line);
			return null;			
		}
		
		FoldingInstruction rv = new FoldingInstruction();
		
		String typeStr = line.substring(0, space1);
		if (typeStr.equals("%collapse")) {
			rv.type = Type.COLLAPSE;
		} else if (typeStr.equals("%expand")) {
			rv.type = Type.EXPAND;
		} else {
			System.err.println("Unknown folding instr type: " + line);
			return null;
		}
		
		String whenStr = line.substring(space1+1, space2);
		if (whenStr.equals("containing")) {
			rv.when = When.CONTAINING;
		} else if (whenStr.equals("not-containing")) {
			rv.when = When.NOT_CONTAINING;
		} else {
			System.err.println("Unknown folding instr when clause: " + line);
			return null;
		}
		
		rv.string = line.substring(space2+2, nextSeparator);
		if (rv.string.isEmpty()) {
			System.err.println("Empty string to match in folding instr: " + line);
			return null;			
		}
		
		return rv;		
	}
	
	
}
