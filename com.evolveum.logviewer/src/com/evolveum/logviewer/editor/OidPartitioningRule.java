package com.evolveum.logviewer.editor;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class OidPartitioningRule implements IPredicateRule {

	private static final String OID_MASK = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX";
	
	IToken defaultReturnToken;
	
	public OidPartitioningRule(IToken defaultReturnToken) {
		this.defaultReturnToken = defaultReturnToken;
	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		for (int index = 0; index < OID_MASK.length(); index++) {
			int in = scanner.read();
			if (in == ICharacterScanner.EOF) {
				return Token.EOF;
			}
			int mask = OID_MASK.charAt(index);
			if ((mask == 'X' && !Character.isLetterOrDigit(in)) ||
					(mask != 'X' && in != mask)) {
				while (index >= 0) {
					scanner.unread();
					index--;
				}
				return Token.UNDEFINED;
			}
		}
		return getSuccessToken();
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
