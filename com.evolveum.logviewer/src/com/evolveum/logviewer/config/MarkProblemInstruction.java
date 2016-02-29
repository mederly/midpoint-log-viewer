package com.evolveum.logviewer.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;

import com.evolveum.logviewer.parsing.ParsingUtils;

public class MarkProblemInstruction implements Instruction {
	
	private static final Pattern PATTERN = Pattern.compile("\\%mark\\-(?<kind>error|warn|info|line)\\s+" + "(?<condition>" + Condition.REGEXP_COMPLETE + ")" + "(?<severity>error|warn|info|none)\\s*(#.*)?");
	
	public static enum Kind { ERROR, WARN, INFO, LINE };
	
	private final Kind kind;
	private final Condition condition;
	private final int severity;

	public MarkProblemInstruction(Kind kind, Condition condition, int severity) {
		this.kind = kind;
		this.condition = condition;
		this.severity = severity;
	}

	public Kind getKind() {
		return kind;
	}
	
	public Condition getCondition() {
		return condition;
	}

	public int getSeverity() {
		return severity;
	}

	public static MarkProblemInstruction parseFromLine(EditorConfiguration editorConfiguration, String line) {
		Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}
		
		final String kindString = matcher.group("kind");
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
		
		final String severityString = matcher.group("severity");
		final int severity;
		if ("error".equalsIgnoreCase(severityString)) {
			severity = IMarker.SEVERITY_ERROR;
		} else if ("warn".equalsIgnoreCase(severityString)) {
			severity = IMarker.SEVERITY_WARNING;
		} else if ("info".equalsIgnoreCase(severityString)) {
			severity = IMarker.SEVERITY_INFO;
		} else if ("none".equalsIgnoreCase(severityString)) {
			severity = -1;
		} else {
			System.err.println("Unknown severity name: " + severityString);
			severity = IMarker.SEVERITY_INFO;
		}
		
		final Condition condition = Condition.parse(matcher.group("condition"));
		return new MarkProblemInstruction(kind, condition, severity);
	}

	public boolean matches(String line, String entry, String header) {
		if (kind != Kind.LINE) {
			if (!ParsingUtils.isLogEntryStart(line)) {
				return false;
			}
			// so header == line after this point
			switch (kind) {
			case ERROR: if (!(line.contains("] ERROR ("))) return false; break;
			case WARN:  if (!(line.contains("] WARN  (")) && !(line.contains("] WARN ("))) return false; break;
			case INFO:  if (!(line.contains("] INFO  (")) && !(line.contains("] INFO ("))) return false; break;
			default: throw new IllegalStateException();
			}
		}
		return condition.matches(line, entry, header, Scope.LINE);
	}

}
