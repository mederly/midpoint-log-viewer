package com.evolveum.logviewer.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;

import com.evolveum.logviewer.config.MarkProblemInstruction.Kind;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class KillInstruction implements Instruction {

	// like %kill containing "(com.evolveum.midpoint.provisioning.impl.ResourceManager)"

	private static final Pattern PATTERN = Pattern.compile("\\%kill-(?<kind>line|entry|duplicate-entry)\\s*" + "(?<condition>" + Condition.REGEXP_COMPLETE + ")" + "\\s*(#.*)?");
	
	public static enum Kind { LINE, ENTRY, DUPLICATE_ENTRY };
	
	private final Kind kind;
	private final Condition condition;

	public KillInstruction(Kind kind, Condition condition) {
		super();
		this.kind = kind;
		this.condition = condition;
	}

	public Kind getKind() {
		return kind;
	}

	public Condition getCondition() {
		return condition;
	}
	
	public static KillInstruction parseFromLine(EditorConfiguration editorConfiguration, String line) {
		Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}
		
		final String kindString = matcher.group("kind");
		final Kind kind;
		if ("line".equalsIgnoreCase(kindString)) {
			kind = Kind.LINE;
		} else if ("entry".equalsIgnoreCase(kindString)) {
			kind = Kind.ENTRY;
		} else if ("duplicate-entry".equalsIgnoreCase(kindString)) {
			kind = Kind.DUPLICATE_ENTRY;
		} else {
			throw new IllegalStateException("Unknown kind: " + kindString);
		}
		
		final Condition condition = Condition.parse(matcher.group("condition"));
		return new KillInstruction(kind, condition);
	}


	public boolean matches(String line, String entry, String header) {
		if (kind == Kind.LINE) {
			return condition.matches(line, entry, header, Scope.LINE);			
		} else {
			if (!ParsingUtils.isLogEntryStart(line)) {
				return false;
			}
			return condition.matches(line, entry, header, Scope.ENTRY);
		}
		
	}	
	
}
