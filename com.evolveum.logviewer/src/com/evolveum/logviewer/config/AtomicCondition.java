package com.evolveum.logviewer.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtomicCondition {

	public static final String REGEXP_TEXT = "'.*'|\".*\"|\\[.*\\]";
	public static final String REGEXP_ATOMIC = "(line|entry|header)?\\s*(containing|not-containing|matching|not-matching)\\s+(" + REGEXP_TEXT + ")\\s*";
	public static final String REGEXP_ATOMIC_AND = REGEXP_ATOMIC + "(and\\s+)?";
	public static final Pattern patternAtomicAnd = Pattern.compile(REGEXP_ATOMIC_AND);
	
	private static int G_SCOPE = 1;
	private static int G_OPERATOR = 2;
	private static int G_TEXT = 3;
	
	private final Scope scope;
	private final Operator operator;
	private final String text;
	private final Pattern pattern;
	
	public AtomicCondition(Scope scope, Operator operator, String text) {
		super();
		this.scope = scope;
		this.operator = operator;
		this.text = text;
		if (operator.isRegex()) {
			this.pattern = Pattern.compile(text);
		} else {
			this.pattern = null;
		}
	}
	
	public Scope getScope() {
		return scope;
	}

	public Scope getScope(Scope defaultScope) {
		return scope != null ? scope : defaultScope;
	}

	public Operator getOperator() {
		return operator;
	}

	public String getText() {
		return text;
	}
	
	public Pattern getPattern() {
		return pattern;
	}

	public static AtomicCondition parse(String text) {
		System.out.println(" - AtomicCondition.parse called on " + text);
		Matcher matcher = patternAtomicAnd.matcher(text);
		if (!matcher.matches()) {
			throw new IllegalStateException(text + " doesn't match REGEXP_ATOMIC_AND i.e. " + REGEXP_ATOMIC_AND);
		}
		
		return new AtomicCondition(
				Scope.parse(matcher.group(G_SCOPE)), 
				Operator.parse(matcher.group(G_OPERATOR)),
				ConfigurationParser.unwrapText(matcher.group(G_TEXT)));
	}

	public boolean matches(String line, String entry, String header, Scope defaultScope) {
		Scope actualScope = getScope(defaultScope);
		switch (actualScope) {
		case ENTRY: return operator.matches(entry, text, pattern);
		case HEADER: return operator.matches(header, text, pattern);
		default: return operator.matches(line, text, pattern);
		}
	}
	
	public Matcher matchingMatcher(String line, String entry, String header, Scope defaultScope) {
		Scope actualScope = getScope(defaultScope);
		switch (actualScope) {
		case ENTRY: return operator.matchingMatcher(entry, text, pattern);
		case HEADER: return operator.matchingMatcher(header, text, pattern);
		default: return operator.matchingMatcher(line, text, pattern);
		}
	}
}
