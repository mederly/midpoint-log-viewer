package com.evolveum.logviewer.tree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.parsing.MatchResult;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class GeneralLevelDefinition extends OutlineLevelDefinition<GeneralNodeContent> {

	public GeneralLevelDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	private String text;
	private String regexp;
	private Pattern pattern;
	private String label;
	
	public static GeneralLevelDefinition parseFromLine(EditorConfiguration editorConfiguration, String line) {
		line = line.trim();
		
		int space0 = line.indexOf(' ');
		if (space0 < 0) {
			System.out.println("Couldn't parse outline instruction: " + line);
			return null;
		}
		
		int space1 = line.indexOf(' ', space0+1);
		if (space1 < 0) {
			System.out.println("Couldn't parse outline instruction: " + line);
			return null;
		}
		int space2 = line.indexOf(' ', space1+1);
		if (space2 < 0) {
			System.out.println("Couldn't parse outline instruction: " + line);
			return null;
		}
		int separator = line.charAt(space2+1);
		int nextSeparator = line.indexOf(separator, space2+2);
		if (nextSeparator < 0) {
			System.out.println("Couldn't parse outline instruction: " + line);
			return null;			
		}
		
		GeneralLevelDefinition rv = new GeneralLevelDefinition(editorConfiguration);
		
		try {
			rv.level = Integer.parseInt(line.substring(space1+1, space2));
		} catch (NumberFormatException e) {
			System.out.println("Couldn't parse outline instruction: " + line + ": " + e);
			return null;
		}

		String string1 = line.substring(space2+2, nextSeparator);
		if (separator == '/') {
			rv.regexp = string1;
			rv.compileRegexp();
		} else {
			rv.text = string1;
		}
		
		int separator2 = line.charAt(nextSeparator+2);
		int nextSeparator2 = line.indexOf(separator2, nextSeparator+3);
		if (nextSeparator2 < 0) {
			System.out.println("Couldn't parse outline instruction: " + line);
			return null;			
		}
		rv.label = line.substring(nextSeparator+3, nextSeparator2);
				
		return rv;		
	}

	private void compileRegexp() {
		try {
			pattern = Pattern.compile(regexp);
		} catch (RuntimeException e) {
			System.err.println("Couldn't compile regexp '" + regexp + "': " + e.getMessage());
			e.printStackTrace();
		}
	}

	public String getText() {
		return text;
	}

	public String getRegexp() {
		return regexp;
	}
	
	public Pattern getPattern() {
		return pattern;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public MatchResult<GeneralNodeContent> matches(OutlineNode<GeneralNodeContent> existingNode, int lineNumber, String line, IRegion region, IDocument document) {
		if (text != null) {
			if (line.contains(text)) {
				return createResult(existingNode, lineNumber, line, region, document, null);
			}
		} else if (pattern != null) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				return createResult(existingNode, lineNumber, line, region, document, matcher);
			}
		}
		return null;
	}
	

	private MatchResult<GeneralNodeContent> createResult(OutlineNode<GeneralNodeContent> existingNode, int lineNumber, 
			String line, IRegion region, IDocument document, Matcher matcher) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < label.length(); i++) {
			if (label.charAt(i) != '%') {
				sb.append(label.charAt(i));
			} else {
				char next;
				if (i == label.length()) {
					next = '%';
				} else {
					next = label.charAt(++i);
				}
				if (next == 'd') {
					sb.append(ParsingUtils.parseDate(line));
				} else if (Character.isDigit(next)) {
					StringBuilder sb1 = new StringBuilder();
					do {
						sb1.append(next);
						if (i == label.length()) {
							next = 0;
							break;
						}
						next = label.charAt(++i); 
					} while (Character.isDigit(next));
					if (next == 'g' && matcher != null) {
						sb.append(matcher.group(Integer.parseInt(sb1.toString())));
					} else {
						sb.append(sb1);
						if (next != 0) {
							sb.append(next);
						}
					}
				} else {
					sb.append(next);
				}
			}
		}
		
		if (sb.length() == 0) {
			sb.append("<empty>");
		}
		GeneralNodeContent content = new GeneralNodeContent(sb.toString());
		
		return createMatchResult(existingNode, content, region, lineNumber, line, document);
	}
	
	@Override
	public boolean isHeaderLast() {
		return false;
	}
	
	public String toString() {
		return super.toString() + "; label: " + label;
	}
	
	public static OutlineLevelDefinition<?> parseFromLineAsStartupDefinition(EditorConfiguration editorConfiguration, String line) {
		GeneralLevelDefinition def = new GeneralLevelDefinition(editorConfiguration);
		def.parseLevelFromLine(line);
		def.text = "Product information : http://wiki.evolveum.com/display/midPoint";
		def.label = "=========> System startup at %d <=========";
		return def;
	}

	public static OutlineLevelDefinition<?> parseFromLineAsTestDefinition(EditorConfiguration editorConfiguration, String line) {
		GeneralLevelDefinition def = new GeneralLevelDefinition(editorConfiguration);
		def.parseLevelFromLine(line);
		def.regexp = ".*=====\\[\\ (\\w+\\.\\w+)\\ \\]======================================.*";
		def.compileRegexp();
		def.label = "TEST: %1g";
		return def;
	}

}
