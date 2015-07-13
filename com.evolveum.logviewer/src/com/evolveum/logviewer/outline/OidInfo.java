package com.evolveum.logviewer.outline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OidInfo {

	String oid;
	String type;
	List<String> names = new ArrayList<>();
	String comment;
	String color = "default";
	
	public OidInfo(String oid, String type, String name) {
		this.oid = oid;
		this.type = type.trim();
		names.add(name);
	}
	
	public OidInfo(String oid, String type, String name, String color) {
		this(oid, type, name);
		this.color = color;
	}
	
	public void update(String oid, String type, String name) {
		type = type.trim();
		if (!this.type.equals(type)) {
			System.err.println("Warning: overwriting type for " + oid + ": " + this.type + " -> " + type);
			this.type = type;
		}
		if (!names.contains(name)) {
			Iterator<String> iter = names.iterator();
			boolean found = false;
			String nameLower = name.toLowerCase();
			while (iter.hasNext()) {
				String existingLower = iter.next().toLowerCase();
				if (nameLower.startsWith(existingLower)) {
					found = true;	// no need to update
					break;	 
				}
				if (existingLower.startsWith(nameLower)) {
					iter.remove();		// 'name' is cleaner name than existing one 
				}
			}
			if (!found) {
				names.add(name);
			}
		}
	}

	// %cdbc3398-4913-4691-9d07-547b30930070 : green : user [msadovskis]
	public static OidInfo parseFromLine(String line) {
		if (!line.startsWith("%") || line.length() < 37) {
			return null;
		}
		
		String oid = line.substring(1, 37);
		
		int colon1 = line.indexOf(':');
		if (colon1 < 0) {
			return null;
		}
		int colon2 = line.indexOf(':', colon1+1);
		if (colon2 < 0) {
			return null;
		}
		int bracket = line.indexOf('[', colon2+2);
		if (bracket < 0) {
			return null;
		}
		int closing = line.indexOf(']', bracket+1);
		if (closing < 0) {
			return null;
		}
		
		String color = line.substring(colon1+1, colon2).trim();
		String type = line.substring(colon2+1, bracket).trim();
		String name = line.substring(bracket+1, closing);
		return new OidInfo(oid, type, name, color);
	}
	
	

	public String getOid() {
		return oid;
	}

	public String getType() {
		return type;
	}

	public List<String> getNames() {
		return names;
	}

	public String getComment() {
		return comment;
	}

	public String getColor() {
		return color;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(oid).append(": ");
		sb.append(type).append(": ");
		sb.append(names);
		if (comment != null) {
			sb.append(" [").append(comment).append("]");
		}
		return sb.toString();
	}
}
