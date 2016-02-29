package com.evolveum.logviewer.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowInOutlineInstruction implements Instruction {
	
	private static final Pattern PATTERN = Pattern.compile("\\%show\\-in\\-outline\\s+" + "(?<condition>" + Condition.REGEXP_COMPLETE + ")" + "(?<onOff>on|off)\\s*(#.*)?");
	
	private final Condition condition;
	private final boolean on;

	public ShowInOutlineInstruction(Condition condition, boolean on) {
		super();
		this.condition = condition;
		this.on = on;
	}

	public Condition getCondition() {
		return condition;
	}
	
	public boolean isOn() {
		return on;
	}

	public static ShowInOutlineInstruction parseFromLine(EditorConfiguration config, String line) {
		Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}
		
		final String onOffString = matcher.group("onOff");
		final Condition condition = Condition.parse(matcher.group("condition"));
		return new ShowInOutlineInstruction(condition, "on".equals(onOffString));
	}

	public boolean matches(String line, String entry, String header) {
		return condition.matches(line, entry, header, Scope.LINE);
	}

}
