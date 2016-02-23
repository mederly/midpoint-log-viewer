package com.evolveum.logviewer.tree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.parsing.MatchResult;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class GeneralLevelDefinition extends OutlineLevelDefinition<GeneralNodeContent> {

	private String text;
	private String regexp;
	private Pattern pattern;
	private String label;
	
	public static GeneralLevelDefinition parseFromLine(String line) {
		line = line.trim();
		
		int space1 = line.indexOf(' ');
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
		
		GeneralLevelDefinition rv = new GeneralLevelDefinition();
		
		try {
			rv.level = Integer.parseInt(line.substring(space1+1, space2));
		} catch (NumberFormatException e) {
			System.out.println("Couldn't parse outline instruction: " + line + ": " + e);
			return null;
		}

		String string1 = line.substring(space2+2, nextSeparator);
		if (separator == '/') {
			rv.regexp = string1;
			try {
				rv.pattern = Pattern.compile(rv.regexp);
			} catch (RuntimeException e) {
				System.err.println("Couldn't compile regexp '" + rv.regexp + "': " + e.getMessage());
				e.printStackTrace();
			}
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
	public MatchResult<GeneralNodeContent> matches(OutlineNode<GeneralNodeContent> documentItem, int lineNumber, String line, IRegion region, IDocument document) {
		if (text != null) {
			if (line.contains(text)) {
				return createResult(lineNumber, line, region, document, null);
			}
		} else if (pattern != null) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				return createResult(lineNumber, line, region, document, matcher);
			}
		}
		return null;
	}
	

	private MatchResult<GeneralNodeContent> createResult(int lineNumber, String line, IRegion region, IDocument document, Matcher matcher) {
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
		
		GeneralNodeContent content = new GeneralNodeContent(sb.toString());
		
		OutlineNode<GeneralNodeContent> node = new OutlineNode<>(this, content);
		node.setCoordinates(region, lineNumber, line, document);
		return new MatchResult<>(node);
	}
	
	@Override
	public boolean isHeaderLast() {
		return false;
	}

}
