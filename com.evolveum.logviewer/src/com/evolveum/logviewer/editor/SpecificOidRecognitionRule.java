package com.evolveum.logviewer.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.evolveum.logviewer.outline.OidInfo;

public class SpecificOidRecognitionRule implements IPredicateRule {

	private Map<String,Integer> systemColors = new HashMap<>();
	{
		systemColors.put("white", SWT.COLOR_WHITE);
		systemColors.put("black", SWT.COLOR_BLACK);
		systemColors.put("red", SWT.COLOR_RED);
		systemColors.put("green", SWT.COLOR_GREEN);
		systemColors.put("dark-green", SWT.COLOR_DARK_GREEN);
		systemColors.put("yellow", SWT.COLOR_YELLOW);
		systemColors.put("dark-yellow", SWT.COLOR_DARK_YELLOW);
		systemColors.put("blue", SWT.COLOR_BLUE);
		systemColors.put("dark-blue", SWT.COLOR_DARK_BLUE);
		systemColors.put("magenta", SWT.COLOR_MAGENTA);
		systemColors.put("dark-magenta", SWT.COLOR_DARK_MAGENTA);
		systemColors.put("cyan", SWT.COLOR_CYAN);
		systemColors.put("dark-cyan", SWT.COLOR_DARK_CYAN);
		systemColors.put("gray", SWT.COLOR_GRAY);
		systemColors.put("dark-gray", SWT.COLOR_DARK_GRAY);
	}
	private Map<String,Color> oids = new HashMap<>();
	
	IToken defaultReturnToken;
	
	public SpecificOidRecognitionRule(IToken defaultReturnToken, IDocument document) {
		this.defaultReturnToken = defaultReturnToken;
		
		Display display = Display.getCurrent();
		
		List<OidInfo> oidInfoList = OidUtils.getAllOidInfos(document);
		for (OidInfo oidInfo : oidInfoList) {
			Integer systemColor = systemColors.get(oidInfo.getColor());
			if (systemColor != null) {
				oids.put(oidInfo.getOid(), display.getSystemColor(systemColor));
			}
		}
	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		for (Map.Entry<String, Color> oidEntry : oids.entrySet()) {
			IToken t = matches(scanner, oidEntry.getKey());
			if (t.isEOF()) {
				return t;
			} else if (t.isOther()) {
				return new Token(new TextAttribute(oidEntry.getValue(), null, SWT.BOLD));
			}
		}
		
		return Token.UNDEFINED;
	}
	
	private IToken matches(ICharacterScanner scanner, String oid) {
		for (int index = 0; index < oid.length(); index++) {
			int in = scanner.read();
			if (in == ICharacterScanner.EOF) {
				return Token.EOF;
			}
			if (in != oid.charAt(index)) {
				while (index >= 0) {
					scanner.unread();
					index--;
				}
				return Token.UNDEFINED;
			}
		}
		return Token.OTHER;
	}

	@Override
	public IToken getSuccessToken() {
		return defaultReturnToken;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		return evaluate(scanner);
	}
}
