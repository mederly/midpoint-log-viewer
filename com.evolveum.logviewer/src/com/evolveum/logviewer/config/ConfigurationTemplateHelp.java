package com.evolveum.logviewer.config;

import com.evolveum.logviewer.tree.ContextNodeDefinition;
import com.evolveum.logviewer.tree.ExecutionNodeDefinition;
import com.evolveum.logviewer.tree.ExpressionNodeDefinition;
import com.evolveum.logviewer.tree.GenericNodeDefinition;
import com.evolveum.logviewer.tree.MappingNodeDefinition;
import com.evolveum.logviewer.tree.ProjectionContextNodeDefinition;
import com.evolveum.logviewer.tree.ScriptNodeDefinition;
import com.evolveum.logviewer.tree.SummaryNodeDefinition;

public class ConfigurationTemplateHelp {

	public static void writeTo(StringBuilder sb) {
		sb.append("\n\n");
		sb.append("# Content of the configuration/action section:\n");
		sb.append("# ********************************************\n");
		sb.append("#\n");
		sb.append("# Editor configuration:\n");
		sb.append("# =====================\n");
		sb.append("# %skip-thread-processing - turns off parsing thread names\n");
		sb.append("# %no-component-names/%component-names - tells not to expect/to expect component names (default: autodetect)\n");
		sb.append("# %mark-delay <n> <severity> - if delay between two log lines is at least <n> milliseconds, marks the line as <level> (error, warning, info)\n");
		sb.append("# %mark-error [containing/not-containing/regexp <text>] <severity> - mark ERROR messages (all or containing/not-containing given text) as <severity> (error, warning, info, none)\n");
		sb.append("# %mark-warn [containing/not-containing/regexp <text>] <severity> - mark WARN messages (all or containing/not-containing given text) as <severity> (error, warning, info, none)\n");
		sb.append("# %mark-info [containing/not-containing/regexp <text>] <severity> - mark WARN messages (all or containing/not-containing given text) as <severity> (error, warning, info, none)\n");
		sb.append("# %mark-line containing/not-containing/regexp <text> <severity> - mark lines (containing/not-containing given text) as <severity> (error, warning, info, none)\n");
		sb.append("# %show-in-outline containing/not-containing/log-line-containing/log-line-not-containing/regexp <text> on/off\n");
		sb.append("# %outline <type> <level> - standard 'type' lines outlined at <level>; type = (startup, test, operation-summary, operation-context, projection-context, execution\n"
				+ "                            mapping, expression, script (note: recommended approximately in this order; projection-context, execution and mapping should be\n"
				+ "                            one level below operation-context)\n");
		sb.append("# %outline custom <level> <text> <title> - lines containing <text> are displayed in outline at given level\n");		
		sb.append("# %outline custom <level> regexp <regexp> <title> - lines matching <regexp> are displayed in outline at given level\n");
		sb.append("#\n");
		sb.append("#\n");
		sb.append("# Line erasing (killing) instructions - used to permanently remove lines you don't need:\n");
		sb.append("# ======================================================================================\n");		
		sb.append("#\n");
		sb.append("# %kill <when> <what>, where:\n");
		sb.append("#       <when> = { containing, not-containing, log-line-containing, log-line-not-containing },\n");
		sb.append("#       <what> is text to be matched enclosed in a pair of \" or \' (or any other characters)\n");
		sb.append("#\n");
		sb.append("# e.g. %kill containing \"(com.evolveum.midpoint.provisioning.impl.ResourceManager)\"\n");
		sb.append("#      %kill log-line-not-containing \'[main]\' - erases information from all threads other than [main]\n");
		sb.append("#\n");
		sb.append("#\n");
		sb.append("# Line folding instructions - used to hide (fold) less important lines:\n");
		sb.append("# =====================================================================\n");
		sb.append("#\n");
		sb.append("# <action> <when> <what>, where:\n");
		sb.append("#          <action> = { %collapse, %expand },\n");
		sb.append("#          <when> = { containing, not-containing },\n");
		sb.append("#          <what> is text to be matched enclosed in a pair of \" or \' (or any other characters, like * in example below)\n");
		sb.append("#\n");
		sb.append("# e.g. %collapse containing *(com.evolveum.midpoint.provisioning.impl.ResourceManager)*\n");
		sb.append("#\n");
		sb.append("#\n");
		sb.append("#\n");
		sb.append("%error-marking off containing 'ConnectorFactoryIcfImpl): Provided Icf connector path '\n");		
		sb.append("%error-marking off containing 'No system configuration found, skipping application of initial system settings'\n");
		sb.append("%error-marking off containing 'HHH000315: Exception executing batch [could not execute batch]'\n");
		sb.append("\n");		
		sb.append("%outline 1 'Product information : http://wiki.evolveum.com/display/midPoint' 'STARTUP AT %D'\n");
		sb.append("%outline 2 '=====[ 'TestUtil\\): =====\\[ (\\w+\\.\\w+) \\]======================================' 'TEST: %1G'\n");		
		sb.append("\n");		
		sb.append("# %D - current date/time from log line\n");		
		sb.append("# %nG - content of group n from the regexp\n");		
	}

}
