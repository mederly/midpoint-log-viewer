package com.evolveum.logviewer.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Condition {

	public static final String REGEXP_COMPLETE = "(" + AtomicCondition.REGEXP_ATOMIC_AND + ")*";

	public static final Pattern patternComplete = Pattern.compile(REGEXP_COMPLETE);

	private final List<AtomicCondition> atomicConditions = new ArrayList<>();
	
	public static Condition parse(String text) {
		System.out.println("Condition.parse called on " + text);
		
		Condition condition = new Condition();
		
		for (;;) {
			Matcher matcher = patternComplete.matcher(text);
			if (!matcher.matches()) {
				throw new IllegalStateException(text + " doesn't match REGEXP_COMPLETE i.e. " + REGEXP_COMPLETE);
			}
			String atomicAnd = matcher.group(1);
			System.out.println(" - atomicAnd = " + atomicAnd);
			if (atomicAnd == null || atomicAnd.length() == 0) {
				return condition;
			}
			condition.atomicConditions.add(AtomicCondition.parse(atomicAnd));
			text = text.substring(0, text.length() - atomicAnd.length()); 
		}
	}
	
	public boolean matches(String line, String entry, String header, Scope defaultScope) {
		for (AtomicCondition atomicCondition : atomicConditions) {
			if (!atomicCondition.matches(line, entry, header, defaultScope)) {
				return false;
			}
		}
		return true;
	}
	
	public Matcher matchingMatcher(String entry, String header, String line, Scope defaultScope) {
		Matcher matcher = null;
		for (AtomicCondition atomicCondition : atomicConditions) {
			matcher = atomicCondition.matchingMatcher(entry, header, line, defaultScope);
			if (matcher == null) {
				break;
			}
		}
		return matcher;
	}
}
