package com.evolveum.logviewer.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;

import com.evolveum.logviewer.parsing.ParsingUtils;

public class MarkProblemInstruction implements Instruction {
	
	private static final Pattern PATTERN = Pattern.compile("\\%mark\\-(error|warn|info|line)\\s+((containing|not-containing|regexp)\\s+('.*'|\".*\"|\\*.*\\*)\\s+)?(error|warning|info|none)\\s*(#.*)?");
	private static final int G_KIND = 1; // group 1 = error|warn|...
										 // group 2 = aggregation
	private static final int G_WHEN = 3; // group 3 = containing|not-containing|...
	private static final int G_TEXT = 4; // group 4 = text
	private static final int G_SEVERITY = 5; // group 5 = error|warning|...
	
	public static enum Kind { ERROR, WARN, INFO, LINE };
	
	private final Kind kind;
	private final When when;
	private final String text;
	private final Pattern pattern;
	private final int severity;

	public MarkProblemInstruction(Kind kind, When when, String text, Pattern pattern, int severity) {
		this.kind = kind;
		this.when = when;
		this.text = text;
		this.pattern = pattern;
		this.severity = severity;
	}

	public Kind getKind() {
		return kind;
	}

	public When getWhen() {
		return when;
	}

	public String getText() {
		return text;
	}

	public int getSeverity() {
		return severity;
	}

	public static MarkProblemInstruction parseFromLine(String line) {
		Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}
		
		final String kindString = matcher.group(G_KIND);
		final Kind kind;
		if ("error".equalsIgnoreCase(kindString)) {
			kind = Kind.ERROR;
		} else if ("warn".equalsIgnoreCase(kindString)) {
			kind = Kind.WARN;
		} else if ("info".equalsIgnoreCase(kindString)) {
			kind = Kind.INFO;
		} else if ("line".equalsIgnoreCase(kindString)) {
			kind = Kind.LINE;
		} else {
			throw new IllegalStateException("Unknown kind: " + kindString);
		}
		
		final When when = When.fromString(matcher.group(G_WHEN));
		final String text = ConfigurationParser.unwrapText(matcher.group(G_TEXT));
		Pattern pattern = null;
		if (when == When.REGEXP) {
			try {
				pattern = Pattern.compile(text);
			} catch (RuntimeException e) {
				System.err.println("Couldn't compile regex: '"+text+"'");
				e.printStackTrace();
			}
		}
		
		final String severityString = matcher.group(G_SEVERITY);
		final int severity;
		if ("error".equalsIgnoreCase(severityString)) {
			severity = IMarker.SEVERITY_ERROR;
		} else if ("warning".equalsIgnoreCase(severityString)) {
			severity = IMarker.SEVERITY_WARNING;
		} else if ("info".equalsIgnoreCase(severityString)) {
			severity = IMarker.SEVERITY_INFO;
		} else if ("none".equalsIgnoreCase(severityString)) {
			severity = -1;
		} else {
			System.err.println("Unknown severity name: " + severityString);
			severity = IMarker.SEVERITY_INFO;
		}
		return new MarkProblemInstruction(kind, when, text, pattern, severity);
	}

	public boolean matches(String line) {
		if (kind != Kind.LINE) {
			if (!ParsingUtils.isLogEntryStart(line)) {
				return false;
			}
			switch (kind) {
			case ERROR: if (!(line.contains("] ERROR ("))) return false; break;
			case WARN:  if (!(line.contains("] WARN  (")) && !(line.contains("] WARN ("))) return false; break;
			case INFO:  if (!(line.contains("] INFO  (")) && !(line.contains("] INFO ("))) return false; break;
			}
		}
		if (when == null) {
			return true;
		}
		switch (when) {
		case CONTAINING: return line.contains(text);
		case NOT_CONTAINING: return !line.contains(text);
		case REGEXP: return pattern.matcher(line).matches();
		default: return false;	//not supported
		}
	}

}
