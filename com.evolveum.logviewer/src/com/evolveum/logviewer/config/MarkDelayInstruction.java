package com.evolveum.logviewer.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;

public class MarkDelayInstruction implements Instruction {
	
	private static final Pattern PATTERN = Pattern.compile("\\%mark\\-delay\\s+(\\d+)\\s+(error|warn|info)");
	
	private final int milliseconds;
	private final int severity;
	
	public MarkDelayInstruction(int milliseconds, int severity) {
		this.milliseconds = milliseconds;
		this.severity = severity;
	}
	
	public int getMilliseconds() {
		return milliseconds;
	}

	public int getSeverity() {
		return severity;
	}

	public static MarkDelayInstruction parseFromLine(EditorConfiguration config, String line) {
		Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}
		final int ms = Integer.parseInt(matcher.group(1));
		final String severityString = matcher.group(2);
		final int severity;
		if ("error".equalsIgnoreCase(severityString)) {
			severity = IMarker.SEVERITY_ERROR;
		} else if ("warn".equalsIgnoreCase(severityString)) {
			severity = IMarker.SEVERITY_WARNING;
		} else if ("info".equalsIgnoreCase(severityString)) {
			severity = IMarker.SEVERITY_INFO;
		} else {
			System.err.println("Unknown severity name: " + severityString);
			severity = IMarker.SEVERITY_INFO;
		}
		return new MarkDelayInstruction(ms, severity);
	}

}
