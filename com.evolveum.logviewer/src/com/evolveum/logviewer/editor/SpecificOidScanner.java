package com.evolveum.logviewer.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class SpecificOidScanner extends RuleBasedScanner {
	
	public SpecificOidScanner(MyColorManager manager, IDocument document) {
		
		Display display = Display.getCurrent();
		Color c = display.getSystemColor(SWT.COLOR_BLACK);
		
		IToken defaultToken = new Token(new TextAttribute(c, null, SWT.BOLD));
		
		IRule[] rules = new IRule[1];
		rules[0] = new SpecificOidRecognitionRule(defaultToken, document);
		setRules(rules);
		
		setDefaultReturnToken(defaultToken);
	}
}
