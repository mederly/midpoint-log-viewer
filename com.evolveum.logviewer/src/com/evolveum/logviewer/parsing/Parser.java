package com.evolveum.logviewer.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

import com.evolveum.logviewer.config.ConfigurationParser;
import com.evolveum.logviewer.config.ConfigurationTemplateHelp;
import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.config.ErrorMarkingInstruction;
import com.evolveum.logviewer.config.OidInfo;
import com.evolveum.logviewer.config.ThreadInfo;
import com.evolveum.logviewer.outline.MyContentOutlinePage;
import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.tree.OutlineLevelDefinition;
import com.evolveum.logviewer.tree.OutlineNode;
import com.evolveum.logviewer.tree.OutlineNodeContent;

public class Parser {

	private final int numberOfLines;
	private final IDocument document;
	private final IResource resource;
	
	private final EditorConfiguration configuration;
	
	private final OutlineNode<? extends OutlineNodeContent> firstOutlineNode;
	private OutlineNode<? extends OutlineNodeContent> currentOutlineNode;

//	// ContextDump that was lastly created
//	private ContextDumpItem lastContextDump = null;
//	
//	// ContextDump we are currently physically in - the next log line sets this to null
//	private ContextDumpItem currentContextDump = null;
//	
//	// scripts and expressions will be aggregated into following mapping
//	// there are exceptions, however - like expressions in notifications
//	private List<DocumentItem> scriptsAndExpressions = new ArrayList<>();
//	
//	// mappings and "going to execution" items are aggregated into the following context dump
//	private List<DocumentItem> mappingsAndExecutions = new ArrayList<>();
	
	
	
	
	public List<Position> foldingRegions = new ArrayList<Position>();
	
	Map<String,OidInfo> discoveredOidInfos = new HashMap<>();
	List<OidInfo> configuredOidInfos = new ArrayList<OidInfo>();
	
	public boolean hasConfigSection = false;
	
	Map<String,ThreadInfo> discoveredThreads = new HashMap<>();
	List<String> configuredThreads = new ArrayList<String>();
	
	Boolean componentNames;				// whether expect component names (e.g. [PROVISIONING]) in log lines
	
	public Parser(IDocument document, IResource resource) {
		this.document = document;
		this.resource = resource;
		this.numberOfLines = document.getNumberOfLines();
		this.configuration = ConfigurationParser.getConfiguration(document);
		this.componentNames = configuration.componentNames;
		OutlineLevelDefinition<? extends OutlineNodeContent> rootOutlineInstruction = configuration.getRootOutlineInstruction();
		if (rootOutlineInstruction != null) {
			this.firstOutlineNode = new OutlineNode<>(rootOutlineInstruction);
			this.currentOutlineNode = this.firstOutlineNode;
		} else {
			this.firstOutlineNode = null;
		}
	}
	
	public void parse() {
		for (int lineNumber = 0; lineNumber < numberOfLines; lineNumber++) {
			try {
				IRegion region = document.getLineInformation(lineNumber);
				String line = getLine(document, region);

				if (line.equals(MyContentOutlinePage.CONFIG_MARKER) || hasConfigSection) {
					onConfigLine(lineNumber, line, region);
					continue;
				}

				onAnyLine(lineNumber, line, region);
				if (ParsingUtils.isLogEntryStart(line)) {
					onLogEntryLine(lineNumber, line, region);
				}
				
				if (currentOutlineNode != null) {
					currentOutlineNode.parseLine(lineNumber, line, region, document);
					if (currentOutlineNode.getNextSibling() != null) {
						currentOutlineNode = currentOutlineNode.getNextSibling();
					}
				}

//				if (line.contains("---[ SYNCHRONIZATION")) {
//					line = line.substring(line.indexOf("---["));
//					onContextDumpStart(lineNumber, line, region, false); 
//				} else if () {
//					parser.onContextDumpStart(lineNumber, line, region, true);
//				if (line.startsWith("---[ SCRIPT")) {
//					onScriptStart(lineNumber, line, region);
//				} else if (line.startsWith("---[ EXPRESSION")) {
//					onExpressionStart(lineNumber, line, region);					
//				} else if (line.startsWith("---[ MAPPING")) {
//					onMappingStart(lineNumber, line, region);					
//				} else if (line.startsWith("    PROJECTION ShadowType Discr")) {
//					onProjectionContextDumpStart(lineNumber, line, region);
//				} else if (line.startsWith("---[ Going to EXECUTE")) {
//					onGoingToExecute(lineNumber, line, region);		
//				} else if () {
//					onClockworkSummary(lineNumber, line, region);
//				} else if (line.startsWith("---[")) {
//					onMappingStart(lineNumber, line, region);		// temporary solution					
//				} 
			} catch (BadLocationException e) {
				System.err.println("Couldn't parse line #" + lineNumber + ": " + e);
			}
		}
		try {
			dumpInfo();
		} catch (BadLocationException e) {
			System.err.println("Couldn't dump info: " + e);
		}
		
		if (firstOutlineNode != null) {
			firstOutlineNode.dumpAll(this);
		}
	}
	
	private Long lastTimestamp = null;
	
	public void onLogEntryLine(int lineNumber, String line, IRegion region) {
		// This may be a line that closes a context dump.
//		if (currentContextDump != null) {
//			nodes.add(currentContextDump.createTreeNode(this));
//			currentContextDump = null;
//		}
		
		if (!configuration.skipThreadProcessing) {
			registerThread(line);
		}
		
		if (line.contains("] ERROR (")) {
			onErrorLine(lineNumber, line, region);
		}
		
		Date date = ParsingUtils.parseDate(line);
		if (date != null) {
			long currentTimestamp = date.getTime();
			if (lastTimestamp != null) {
				long delta = currentTimestamp - lastTimestamp;
				if (configuration.getErrorIfDelay() != null && delta >= configuration.getErrorIfDelay()) {
					addMarker(lineNumber, "Delay (" + delta + " msec) greater than or equal configured threshold of " + configuration.getErrorIfDelay() + " msec", IMarker.SEVERITY_ERROR);
				} else if (configuration.getWarningIfDelay() != null && delta >= configuration.getWarningIfDelay()) {
					addMarker(lineNumber, "Delay (" + delta + " msec) greater than or equal configured threshold of " + configuration.getWarningIfDelay() + " msec", IMarker.SEVERITY_WARNING);
				} else if (configuration.getInfoIfDelay() != null && delta >= configuration.getInfoIfDelay()) {
					addMarker(lineNumber, "Delay (" + delta + " msec) greater than or equal configured threshold of " + configuration.getInfoIfDelay() + " msec", IMarker.SEVERITY_INFO);
				}
			}
			lastTimestamp = currentTimestamp;
		}
	}

	private void registerThread(String line) {
		int firstLeftBracket = line.indexOf('[');
		if (firstLeftBracket < 0) {
			return;
		}
		
		if (componentNames == null) {
			componentNames = line.contains("] [");
		}
		
		int threadLeftBracket;
		if (!componentNames) {
			threadLeftBracket = firstLeftBracket;
		} else {
			threadLeftBracket = line.indexOf('[', firstLeftBracket+1);
			if (threadLeftBracket < 0) {
				return;
			}
		}
		
		int threadRightBraket = line.indexOf(']', threadLeftBracket+1);
		if (threadRightBraket < 0) {
			return;
		}
		String threadName = line.substring(threadLeftBracket+1, threadRightBraket);
		ThreadInfo info = discoveredThreads.get(threadName);
		if (info == null) {
			info = new ThreadInfo(threadName);
			discoveredThreads.put(threadName, info);
		}
		info.records++;
	}

	private String getLine(int number) throws BadLocationException {
		IRegion region = document.getLineInformation(number);
		String line = document.get(region.getOffset(), region.getLength());
		return line;
	}

//	public void onScriptStart(int lineNumber, String line, IRegion region) throws BadLocationException {
//		onScriptOrExpression(lineNumber, line, region);
//	}
//
//	public void onExpressionStart(int lineNumber, String line, IRegion region) throws BadLocationException {
//		onScriptOrExpression(lineNumber, line, region);
//	}
//
//	private void onScriptOrExpression(int lineNumber, String line, IRegion region) throws BadLocationException {
//		String label = line.substring(5) + suffix(document, lineNumber, true);
//		TreeNode node = new TreeNode(label, region);
//		scriptsAndExpressions.add(node);
//	}
//
//	public void onMappingStart(int lineNumber, String line, IRegion region) throws BadLocationException {
//		String label = line.substring(5) + suffix(document, lineNumber, true);
//		mappingsAndExecutions.add(new MappingItem(region, lineNumber, document, getPreviousMapping(), label, scriptsAndExpressions));
//		scriptsAndExpressions.clear();
//	}
//	
//	private MappingItem getPreviousMapping() {
//		int i = mappingsAndExecutions.size()-1;
//		while (i >= 0) {
//			if (mappingsAndExecutions.get(i) instanceof MappingItem) {
//				return (MappingItem) mappingsAndExecutions.get(i);
//			}
//			i--;
//		}
//		return null;
//	}
//	
//	public void onGoingToExecute(int lineNumber, String line, IRegion region) throws BadLocationException {
//		String label = "--> " + line.substring(5) + suffix(document, lineNumber, true);
//		TreeNode node = new TreeNode(label, region);		
//		node.addChildren(scriptsAndExpressions);
//		scriptsAndExpressions.clear();		
//		mappingsAndExecutions.add(new ExecutionItem(region, lineNumber, node));
//	}
//	
//	public void onClockworkSummary(int lineNumber, String line, IRegion region) throws BadLocationException {
//		flushMappingsAndScriptsAndExpressions();
//		TreeNode node = new TreeNode(line + suffix(document, lineNumber, true), region);
//		nodes.add(node);
//		currentContextDump = null;
//	}
//	
//	private void flushMappingsAndScriptsAndExpressions() {
//		for (DocumentItem item : mappingsAndExecutions) {
//			if (item.treeNode != null) {
//				nodes.add(item.treeNode);
//			} else {
//				System.err.println("Problem - mapping/execution without treeNode: " + item);
//			}
//		}
//		nodes.addAll(scriptsAndExpressions);
//		mappingsAndExecutions.clear();
//		scriptsAndExpressions.clear();
//	}
//
//	
//
//	public void onProjectionContextDumpStart(int lineNumber, String line, IRegion region) {
//		if (currentContextDump != null) {
//			TreeNode node = new TreeNode(line, region.getOffset(), region.getLength());
//			currentContextDump.addProjectionContextTreeNode(node);
//		}		
//	}

	public void onAnyLine(int lineNumber, String line, IRegion region) throws BadLocationException {
		extractOidInfo(lineNumber, line);
		processFolding(lineNumber, line);
	}

	// various possibilities, e.g.
	//   "shadow:f4eccb7b-e61c-436b-a09a-cad6addb1904(VC)"
	
	private Pattern oidPattern = Pattern.compile(
			".*\\b(\\w+):([A-F0-9]{8}(?:-[A-F0-9]{4}){3}-[A-F0-9]{12})\\((.*)\\).*",
			Pattern.CASE_INSENSITIVE);
	private void extractOidInfo(int lineNumber, String line) {
		Matcher matcher = oidPattern.matcher(line);
		if (matcher.matches()) {
			String type = matcher.group(1);
			String oid = matcher.group(2);
			String name = matcher.group(3);
			if (type.equals("FocusType")) {
				// like T=FocusType:296f822d-a93e-4a63-b3e8-5e603138d2d1({http://midpoint.evolveum.com/xml/ns/public/common/common-3}org)
				// i.e. no info
				return;
			}
			registerOid(oid, type, name);
			//System.out.println("registered oid " + oid);
		}
	}

	private void registerOid(String oid, String type, String name) {
		if (discoveredOidInfos.containsKey(oid)) {
			OidInfo oidInfo = discoveredOidInfos.get(oid);
			oidInfo.update(oid, type, name);
		} else {
			OidInfo oidInfo = new OidInfo(oid, type, name);
			discoveredOidInfos.put(oid, oidInfo);
		}
	}

	private void processFolding(int lineNumber, String line) throws BadLocationException {
		if (ParsingUtils.isLogEntryStart(line)) {
			processLogEntryFolding(lineNumber, line);
			processEntryExitFolding(lineNumber, line);
		} else {
			processIndentBasedFolding(lineNumber, line);
		}
	}
	
	private Map<Integer,Integer> openEntryPoints = new HashMap<>();
	
	private void processEntryExitFolding(int lineNumber, String line) throws BadLocationException {
		final String ENTRY_TEXT = "(PROFILING): #### Entry: ";
		final String EXIT_TEXT = "(PROFILING): ##### Exit: ";
		Integer entryNumber = getNumber(line, ENTRY_TEXT);
		if (entryNumber != null) {
			openEntryPoints.put(entryNumber, lineNumber);
			return;
		}
		Integer exitNumber = getNumber(line, EXIT_TEXT);
		if (exitNumber != null) {
			Integer startLine = openEntryPoints.get(exitNumber);
			if (startLine == null) {
				System.err.println("Warning: exit without entry: " + line);
			} else {
				//System.out.println("Adding folding region for entry/exit " + exitNumber + ": " + startLine + "->" + lineNumber);
				addFoldingRegion(startLine, lineNumber);
				openEntryPoints.remove(exitNumber);
			}
		}
	}

	private Integer getNumber(String line, String text) {
		int i = line.indexOf(text);
		if (i < 0) {
			return null;
		}
		i += text.length();
		int j = line.indexOf(' ', i);
		if (j < 0) {
			System.err.println("Warning: strange entry/exit line: " + line);
			return null;
		}
		return Integer.valueOf(line.substring(i, j));
	}

	private void processLogEntryFolding(int lineNumber, String line) throws BadLocationException {
		int endLine = lineNumber + 1;
		while (endLine < numberOfLines - 1) {
			String s = getLine(endLine);
			if (ParsingUtils.isLogEntryStart(s)) {
				break;
			}
			endLine++;
		}
		if (endLine > lineNumber + 1) {
			addFoldingRegion(lineNumber, endLine);
		}
	}

	private void addFoldingRegion(int lineNumber, int endLine) throws BadLocationException {
		int startOffset = document.getLineOffset(lineNumber);
		int endOffset = document.getLineOffset(endLine);
		foldingRegions.add(new Position(startOffset, endOffset-startOffset));
	}

	private void processIndentBasedFolding(int lineNumber, String line) throws BadLocationException {
		int indent = getIndent(line);
		int nextIndent = getIndent(lineNumber+1);
		if (nextIndent > indent) {
			int endLine = lineNumber+2;
			for (;;) {
				String currentLine = getLine(endLine);
				if (!currentLine.trim().isEmpty() && getIndent(currentLine) <= indent) {
					break;
				}
				endLine++;
			}
			addFoldingRegion(lineNumber, endLine);
		}
	}

	private int getIndent(int lineNumber) throws BadLocationException {
		if (lineNumber < numberOfLines) {
			return getIndent(getLine(lineNumber));
		} else {
			return 0;
		}
	}

	private int getIndent(String line) {
		int indent = 0;
		while (indent < line.length() && line.charAt(indent)==' ') {
			indent++;
		}
		return indent;
	}

	public void dumpInfo() throws BadLocationException {
		StringBuilder sb = new StringBuilder();
		
		if (!hasConfigSection) {
			sb.append(MyContentOutlinePage.CONFIG_MARKER).append("\n\n");
		}
		
		boolean change = appendOidInfos(sb);
		if (!configuration.skipThreadProcessing) {
			if (appendThreads(sb)) {
				change = true;
			}
		}
		
		if (!hasConfigSection) {
			ConfigurationTemplateHelp.writeTo(sb);
		}

		if (change) {
			String s = sb.toString();
			document.set(document.get() + "\n" + s);
		}
		
	}
	
	private boolean appendThreads(StringBuilder sb) {
		boolean anyNewThreads = false;
		for (ThreadInfo threadInfo : discoveredThreads.values()) {
			if (!configuredThreads.contains(threadInfo.name)) {
				if (!anyNewThreads) {
					sb.append("\n");
				}
				sb.append("%thread ");
				sb.append(String.format("%-50s# %7d records\n", threadInfo.name, threadInfo.records));
				anyNewThreads = true;
			}
		}
		return anyNewThreads;
	}

	private boolean appendOidInfos(StringBuilder sb) {
		// preserve only new entries
		List<OidInfo> reallyNewOidInfoList = new ArrayList<>();
		
		Iterator<Map.Entry<String,OidInfo>> iter = discoveredOidInfos.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, OidInfo> entry = iter.next();
			boolean found = false;
			for (OidInfo cfg : configuredOidInfos) {
				if (cfg.getOid().equals(entry.getKey())) {
					found = true;
				}
			}
			if (!found) {
				reallyNewOidInfoList.add(entry.getValue());
			}
		}
		
		if (reallyNewOidInfoList.isEmpty()) {
			return false;
		}
		
		Collections.sort(reallyNewOidInfoList, new Comparator<OidInfo>() {

			@Override
			public int compare(OidInfo o1, OidInfo o2) {
				return o1.getType().toLowerCase().compareTo(o2.getType().toLowerCase());
			}
			
		});
		
		for (OidInfo oidInfo : reallyNewOidInfoList) {
			sb.append("%oid ").append(oidInfo.getOid()).append(" : ").append(oidInfo.getColor()).append(" : ").append(oidInfo.getType()).append(" ").append(oidInfo.getNames());
			sb.append("\n");
		}
		return true;
	}

	public void onConfigLine(int lineNumber, String line, IRegion region) {
		hasConfigSection = true;
		
		if (line.isEmpty()) {
			return;
		}
		if (line.startsWith("%oid ")) {
			OidInfo parsed = OidInfo.parseFromLine(line);
			if (parsed != null) {
				configuredOidInfos.add(parsed);
			}
		}
		if (line.startsWith("%thread ")) {
			String body = line.substring(8);
			int lastHash = body.lastIndexOf('#');
			if (lastHash > 0) {
				body = body.substring(0, lastHash);
			}
			configuredThreads.add(body.trim());
		}
	}

	private void onErrorLine(int lineNumber, String line, IRegion region) {
		if (resource == null) {
			return;		// no resource, no markers
		}
		boolean enabled = true;
		for (ErrorMarkingInstruction errorInstruction : configuration.getErrorInstructions()) {
			if (errorInstruction.matches(line)) {
				enabled = errorInstruction.isEnable();
				break;
			}
		}
		if (enabled) {
			addMarker(lineNumber, line, IMarker.SEVERITY_ERROR);
		}
	}

	private void addMarker(int lineNumber, String text, int severity) {
		try {
			IMarker m = resource.createMarker(IMarker.PROBLEM);
			m.setAttribute(IMarker.LINE_NUMBER, lineNumber+1);
			m.setAttribute(IMarker.MESSAGE, text);
			m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			m.setAttribute(IMarker.SEVERITY, severity);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private String getLine(IDocument document, IRegion region) throws BadLocationException {
		return document.get(region.getOffset(), region.getLength());
	}

	public List<Position> getFoldingRegions() {
		return foldingRegions;
	}

	public TreeNode[] getTreeNodesAsArray() {
		List<TreeNode> treeNodes = new ArrayList<>();
		
		OutlineNode<? extends OutlineNodeContent> outlineNode = firstOutlineNode; 
		while (outlineNode != null)	{
			TreeNode tn = outlineNode.createTreeNode(this);
			if (tn != null) {
				treeNodes.add(tn);
			}
			outlineNode = outlineNode.getNextSibling();
		}

		return treeNodes.toArray(new TreeNode[0]);
	}
	
}
