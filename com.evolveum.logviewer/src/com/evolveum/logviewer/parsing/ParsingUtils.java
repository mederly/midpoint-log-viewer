package com.evolveum.logviewer.parsing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.outline.MyContentOutlinePage;

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
	
	public static String parseThread(String line, Boolean componentNames) {
		int firstLeftBracket = line.indexOf('[');
		if (firstLeftBracket < 0) {
			return null;
		}
		
		if (componentNames == null) {
			componentNames = line.contains("] [");
		}
		
		int threadLeftBracket;
		if (!componentNames) {
			threadLeftBracket = firstLeftBracket;
		} else {
			threadLeftBracket = line.indexOf('[', firstLeftBracket+1);
			if (threadLeftBracket < 0) {
				return null;
			}
		}
		
		int threadRightBracket = line.indexOf(']', threadLeftBracket+1);
		if (threadRightBracket < 0) {
			return null;
		}
		String threadName = line.substring(threadLeftBracket+1, threadRightBracket);
		return threadName;
	}

	public static String suffix(IDocument document, int lineNumber, boolean newLine) throws BadLocationException {
		String date = "?";
		int lineWithDate = newLine ? lineNumber-1 : lineNumber;
		if (lineWithDate >= 0) {
			IRegion dateRegion = document.getLineInformation(lineWithDate);
			String dateLine = document.get(dateRegion.getOffset(), dateRegion.getLength());
			if (dateLine.length() >= 23) {
				date = dateLine.substring(0, 23); 
			}
		}
		return " " + date + " (#" + lineNumber + ")";
	}

	public static Integer findNextLogLine(IDocument document, int lineNumber) {
		int total = document.getNumberOfLines();
		for (;;) {
			if (lineNumber >= total) {
				return null;
			}
			String line = DocumentUtils.getLine(document, lineNumber);
			if (line.equals(MyContentOutlinePage.CONFIG_MARKER)) {
				return null;
			} else if (isLogEntryStart(line)) {
				return lineNumber;
			}
			lineNumber++;
		}
	}
	
	public static int findLastLogEntryLine(IDocument document, int lineNumber) {
		int total = document.getNumberOfLines();
		for (;;) {
			lineNumber++;
			if (lineNumber >= total) {
				return lineNumber-1;
			}
			String line = DocumentUtils.getLine(document, lineNumber);
			if (line.equals(MyContentOutlinePage.CONFIG_MARKER)) {
				return lineNumber-1;
			} else if (isLogEntryStart(line)) {
				return lineNumber-1;
			}
		}
	}
	
	public static String getLogEntry(IDocument document, int lineNumber) {
		StringBuilder sb = new StringBuilder();
		String line = DocumentUtils.getLine(document, lineNumber);
		int total = document.getNumberOfLines();
		for (;;) {
			if (lineNumber >= total) {
				break;
			}
			sb.append(line).append("\n");
			lineNumber++;
			line = DocumentUtils.getLine(document, lineNumber);
			if (line.equals(MyContentOutlinePage.CONFIG_MARKER)) {
				break;
			} else if (isLogEntryStart(line)) {
				break;
			}
		}
		return sb.toString();
	}

}
