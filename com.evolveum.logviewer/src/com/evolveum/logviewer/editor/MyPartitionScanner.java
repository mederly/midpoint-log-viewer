package com.evolveum.logviewer.editor;

import org.eclipse.jface.text.rules.*;

public class MyPartitionScanner extends RuleBasedPartitionScanner {

	public final static String PARTITION_OID = "__evolveum_log_oid";
	
	public MyPartitionScanner() {
		IPredicateRule[] rules = new IPredicateRule[1];
		rules[0] = new OidPartitioningRule(new Token(PARTITION_OID));
		setPredicateRules(rules);
	}
}
