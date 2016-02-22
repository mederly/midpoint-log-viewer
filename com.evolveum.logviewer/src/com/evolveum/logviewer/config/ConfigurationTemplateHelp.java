package com.evolveum.logviewer.config;

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
		sb.append("# %error-if-delay <n> - if delay between two log lines is at least <n> seconds, marks the line as error\n");
		sb.append("# %warning-if-delay <n> - the same, but mark as warning\n");		
		sb.append("# %info-if-delay <n> - the same, but mark as info\n");		
		sb.append("# %error-marking on/off [containing/not-containing <text>] - turns on/off marking logged ERROR messages (all or\n");
		sb.append("#                those that contain/don't contain given text); see autogenerated samples\n");
		sb.append("# %outline <level> <regexp> [<title>]- lines matching <regexp> are displayed in outline at given level; see samples below\n");		
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
