package com.evolveum.logviewer.tree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.AtomicCondition;
import com.evolveum.logviewer.config.Condition;
import com.evolveum.logviewer.config.ConfigurationParser;
import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.config.Scope;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class GenericNodeDefinition extends OutlineNodeDefinition<GenericNodeContent> {

	private final Condition condition;
	private final String label;
	
	private static final Pattern PATTERN = Pattern.compile("\\%outline\\s+custom\\s+" + "(?<condition>" + Condition.REGEXP_COMPLETE + ")" + "(?<level>\\d+)\\s+(?<title>" + AtomicCondition.REGEXP_TEXT + ")\\s*(#.*)?");
	
	public GenericNodeDefinition(EditorConfiguration editorConfiguration, Condition condition, int level, String label) {
		super(editorConfiguration);
		this.physicalLevel = level;
		this.condition = condition;
		this.label = label;
	}

	public Condition getCondition() {
		return condition;
	}

	public static GenericNodeDefinition parseFromLine(EditorConfiguration editorConfiguration, String line) {
		Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}
		
		final Condition condition = Condition.parse(matcher.group("condition"));
		final int level = Integer.parseInt(matcher.group("level"));
		final String label = ConfigurationParser.unwrapText(matcher.group("label"));
		return new GenericNodeDefinition(editorConfiguration, condition, level, label);
	}

	public String getLabel() {
		return label;
	}

	@Override
	public GenericNodeContent recognize(int lineNumber, String line, String entry, String header, IRegion region, IDocument document) {
		final boolean matches;
		final Matcher matcher;
		if (!label.contains("${group")) {
			matches = condition.matches(line, entry, header, Scope.LINE);
			matcher = null;
		} else {
			matcher = condition.matchingMatcher(line, entry, header, Scope.LINE);
			matches = matcher != null;
		}
		if (matches) {
			return createResult(lineNumber, line, region, document, matcher);
		} else {
			return null;
		}
	}

	private GenericNodeContent createResult(int lineNumber, String line, IRegion region, IDocument document, Matcher matcher) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < label.length(); i++) {
			if (label.charAt(i) != '$') {
				sb.append(label.charAt(i));
				continue;
			} 
			if (i == label.length() || label.charAt(i+1) != '{') {
				sb.append('$');
				continue;
			}
			int ending = label.indexOf('}', i);
			if (ending < 0) {
				sb.append('$');
				continue;
			}
			
			String expression = label.substring(i+2, ending);
			if ("date".equals(expression)) {
				sb.append(ParsingUtils.parseDate(line));
				i = ending;
			} else if (expression.startsWith("group:")) {
				sb.append(matcher.group(Integer.parseInt(expression.substring(6))));
				i = ending;
			} else {
				sb.append('$');
			}
		}
		
		if (sb.length() == 0) {
			sb.append("<empty>");
		}
		GenericNodeContent content = new GenericNodeContent(sb.toString());
		
		return content;
	}

	@Override
	public ContentSelectionStrategy getContentSelectionStrategy() {
		return ContentSelectionStrategy.HEADER_FIRST;
	}
	
	public String toString() {
		return super.toString() + "; label: " + label;
	}

	// %outline startup <level>
	public static OutlineNodeDefinition<?> parseFromLineAsStartupDefinition(EditorConfiguration editorConfiguration, String line) {
		Condition condition = Condition.parse("line containing 'Product information : http://wiki.evolveum.com/display/midPoint'");
		String label = "=========> System startup at ${date} <=========";
		int level = OutlineNodeDefinition.getLevel(line);
		return new GenericNodeDefinition(editorConfiguration, condition, level, label);
	}

	public static OutlineNodeDefinition<?> parseFromLineAsTestDefinition(EditorConfiguration editorConfiguration, String line) {
		Condition condition = Condition.parse("line matching '.*TestUtil\\): =====\\[\\ (\\w+\\.\\w+)\\ \\]======================================.*'");
		String label = "TEST: ${group:1}";
		int level = OutlineNodeDefinition.getLevel(line);
		return new GenericNodeDefinition(editorConfiguration, condition, level, label);
	}
	
	// like TestUtil): -----  WHEN test204AutzJackSelfRole --------------------------------------

	public static OutlineNodeDefinition<?> parseFromLineAsTestPartDefinition(EditorConfiguration editorConfiguration, String line) {
		Condition condition = Condition.parse("line matching '.*TestUtil\\): -----  (WHEN|THEN) (\\w+) --------------------------------------.*'");
		String label = "${group:1}: ${group:2}";
		int level = OutlineNodeDefinition.getLevel(line);
		return new GenericNodeDefinition(editorConfiguration, condition, level, label);
	}
}
