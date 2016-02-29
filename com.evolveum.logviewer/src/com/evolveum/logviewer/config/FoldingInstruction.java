package com.evolveum.logviewer.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FoldingInstruction implements Instruction {

	// like %collapse-entry containing "(com.evolveum.midpoint.provisioning.impl.ResourceManager)"

	private static final Pattern PATTERN = Pattern.compile("\\%(?<type>collapse|expand)\\-(?<kind>line|entry)\\s+" + "(?<condition>" + Condition.REGEXP_COMPLETE + ")" + "\\s*(#.*)?");
	
	public enum Type { COLLAPSE, EXPAND };
	public enum Kind { LINE, ENTRY };

	private final Type type;
	private final Kind kind;
	private final Condition condition;
	
	public FoldingInstruction(Type type, Kind kind, Condition condition) {
		this.type = type;
		this.kind = kind;
		this.condition = condition;
	}
	
	public Type getType() {
		return type;
	}

	public Kind getKind() {
		return kind;
	}

	public Condition getCondition() {
		return condition;
	}

	public static FoldingInstruction parseFromLine(EditorConfiguration editorConfiguration, String line) {
		Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}
		
		final String typeString = matcher.group("type");
		final Type type;
		if ("collapse".equalsIgnoreCase(typeString)) {
			type = Type.COLLAPSE;
		} else if ("expand".equalsIgnoreCase(typeString)) {
			type = Type.EXPAND;
		} else {
			throw new IllegalStateException("Unknown type: " + typeString);
		}

		final String kindString = matcher.group("kind");
		final Kind kind;
		if ("line".equalsIgnoreCase(kindString)) {
			kind = Kind.LINE;
		} else if ("entry".equalsIgnoreCase(kindString)) {
			kind = Kind.ENTRY;
		} else {
			throw new IllegalStateException("Unknown kind: " + kindString);
		}
		
		final Condition condition = Condition.parse(matcher.group("condition"));
		return new FoldingInstruction(type, kind, condition);
	}
	
	
}
