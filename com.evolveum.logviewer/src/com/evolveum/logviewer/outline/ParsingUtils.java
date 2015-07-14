package com.evolveum.logviewer.outline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParsingUtils {

	public static boolean isLogEntryStart(String line) {
		// TEMPORARY
		return line != null &&
				line.length() >= 23 && 
				line.startsWith("201") && 
				line.charAt(4) == '-' &&
				line.charAt(7) == '-' &&
				line.charAt(10) == ' ' &&
				line.charAt(13) == ':' &&
				line.charAt(16) == ':' &&
				line.charAt(19) == ',';
	}

	// we need to parse dates like "2015-07-14 00:13:24,595"
	public static Date parseDate(String line) {
		
		if (!isLogEntryStart(line)) {
			return null;
		}
		
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		try {
			return parser.parse(line.substring(0, 23));
		} catch (ParseException e) {
			return null;
		}
	}

}
