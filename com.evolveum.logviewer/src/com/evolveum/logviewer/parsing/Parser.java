package com.evolveum.logviewer.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import com.evolveum.logviewer.config.MarkProblemInstruction;
import com.evolveum.logviewer.config.OidInfo;
import com.evolveum.logviewer.config.Scope;
import com.evolveum.logviewer.config.ShowInOutlineInstruction;
import com.evolveum.logviewer.config.ThreadInfo;
import com.evolveum.logviewer.outline.MyContentOutlinePage;
import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.tree.OutlineNode;
import com.evolveum.logviewer.tree.OutlineNodeContent;
import com.evolveum.logviewer.tree.OutlineNodeDefinition;

public class Parser {

	private final int numberOfLines;
	private final IDocument document;
	private final IResource resource;
	
	private final EditorConfiguration configuration;
	
	private final TreeMap<Integer,OutlineNode<?>> outlineNodesMap = new TreeMap<>();
	
	public List<Position> foldingRegions = new ArrayList<Position>();
	
	Map<String,OidInfo> discoveredOidInfos = new HashMap<>();
	List<OidInfo> configuredOidInfos = new ArrayList<OidInfo>();
	
	public boolean hasConfigSection = false;
	private boolean createdConfigSection = false;
	
	Map<String,ThreadInfo> discoveredThreads = new HashMap<>();
	List<String> configuredThreads = new ArrayList<String>();
	
	Boolean componentNames;				// whether expect component names (e.g. [PROVISIONING]) in log lines
	
	public Parser(IDocument document, IResource resource) {
		this.document = document;
		this.resource = resource;
		this.numberOfLines = document.getNumberOfLines();
		this.configuration = ConfigurationParser.getConfiguration(document);
		this.componentNames = configuration.componentNames;
	}
	
	public boolean isCreatedConfigSection() {
		return createdConfigSection;
	}

	public void parse() throws BadLocationException {
		
		final List<OutlineNodeDefinition<?>> nodeDefinitions = configuration.getAllOutlineLevelDefinitions();
		final List<ShowInOutlineInstruction> showInOutlineInstructions = configuration.getInstructions(ShowInOutlineInstruction.class);
		
		String entry = null, header = null; 
		
		for (int lineNumber = 0; lineNumber < numberOfLines; lineNumber++) {
			
			IRegion region = document.getLineInformation(lineNumber);
			String line = getLine(document, region);

			if (line.equals(MyContentOutlinePage.CONFIG_MARKER) || hasConfigSection) {
				onConfigLine(lineNumber, line, region);
				continue;
			}
			
			if (ParsingUtils.isLogEntryStart(line)) {
				header = line;
				entry = ParsingUtils.getLogEntry(document, lineNumber);
			}

			onAnyLine(lineNumber, line, entry, header, region);
			if (ParsingUtils.isLogEntryStart(line)) {
				onLogEntryLine(lineNumber, line, entry, region);
			}
			
			if (showInOutline(showInOutlineInstructions, line, entry, header)) {
				for (OutlineNodeDefinition<?> nodeDefinition : nodeDefinitions) {
					OutlineNodeContent content = nodeDefinition.recognize(lineNumber, line, entry, header, region, document);
					if (content != null) {
						@SuppressWarnings({ "unchecked", "rawtypes" })
						OutlineNode<?> node = new OutlineNode(nodeDefinition, content, region, lineNumber, line, document);
						outlineNodesMap.put(lineNumber, node);
					}
				}	
			}
		}

		sortOutlineNodes();
		dumpInfoToConfigSection();
	}

	private boolean showInOutline(List<ShowInOutlineInstruction> showInOutlineInstructions, String line, String entry,
			String header) {
		for (ShowInOutlineInstruction instruction : showInOutlineInstructions) {
			if (instruction.getCondition().matches(line, entry, header, Scope.LINE)) {
				return instruction.isOn();
			}
		}
		return true;
	}

	private void dumpOutlineNodesMap() {
		dumpOutlineNodesMap(outlineNodesMap, 0);
	}

	private void dumpOutlineNodesMap(Map<Integer, OutlineNode<?>> map, int level) {
		if (map == null) {
			return;
		}
		for (Map.Entry<Integer, OutlineNode<?>> entry : map.entrySet()) {
			indent("  ", level);
			System.out.println(entry.getKey() + " -> " + entry.getValue());
			dumpOutlineNodesMap(entry.getValue().getContentMap(), level+1);
		}
	}

	private void sortOutlineNodes() {
		for (int level = 1; level <= configuration.getNumberOfLevels(); level++) {
			sortOutlineNodes(outlineNodesMap, level);
//			System.out.println("--- Outline nodes map after sorting at level " + level + " ---");
//			dumpOutlineNodesMap();
		}
		OutlineNode.createTreePositions(null, outlineNodesMap);
	}

	private void sortOutlineNodes(TreeMap<Integer,OutlineNode<?>> nodesToProcess, int level) {
		if (nodesToProcess.isEmpty()) {
			return;
		}

		int currentLine = -1;		
		for (;;) {
			
			final Map.Entry<Integer, OutlineNode<?>> currentEntry = nodesToProcess.higherEntry(currentLine);
			if (currentEntry == null) {
				break;
			}
			final int currentNodeLevel = currentEntry.getValue().getLevel();
			if (currentNodeLevel > level) {
				currentLine = currentEntry.getKey();
			} else if (currentNodeLevel < level) {
				// was already processed; process its children
				sortOutlineNodes(currentEntry.getValue().getContentMap(), level);
				currentLine = currentEntry.getKey();
			} else {
				final OutlineNode<?> currentNode = currentEntry.getValue();
				int continueAfter = currentNode.createContentMap(nodesToProcess);
				nodesToProcess.keySet().removeAll(currentNode.getContentMap().keySet());
				currentLine = continueAfter;
			}
		}
	}
	

	private Long lastTimestamp = null;
	
	public void onLogEntryLine(int lineNumber, String line, String entry, IRegion region) {
		if (!configuration.skipThreadProcessing) {
			registerThread(line);
		}
		
		Date date = ParsingUtils.parseDate(line);
		if (date != null) {
			long currentTimestamp = date.getTime();
			if (lastTimestamp != null) {
				long delta = currentTimestamp - lastTimestamp;
				if (configuration.getErrorIfDelay() != null && delta >= configuration.getErrorIfDelay()) {
					addMarker(lineNumber, "Delay (" + delta + " msec) reached configured threshold of " + configuration.getErrorIfDelay() + " msec", IMarker.SEVERITY_ERROR);
				} else if (configuration.getWarningIfDelay() != null && delta >= configuration.getWarningIfDelay()) {
					addMarker(lineNumber, "Delay (" + delta + " msec) reached configured threshold of " + configuration.getWarningIfDelay() + " msec", IMarker.SEVERITY_WARNING);
				} else if (configuration.getInfoIfDelay() != null && delta >= configuration.getInfoIfDelay()) {
					addMarker(lineNumber, "Delay (" + delta + " msec) reached configured threshold of " + configuration.getInfoIfDelay() + " msec", IMarker.SEVERITY_INFO);
				}
			}
			lastTimestamp = currentTimestamp;
		}
	}

	private void registerThread(String line) {
		String threadName = ParsingUtils.parseThread(line, componentNames);
		if (threadName == null) {
			return;
		}
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

	public void onAnyLine(int lineNumber, String line, String entry, String header, IRegion region) throws BadLocationException {
		extractOidInfo(lineNumber, line);
		processFolding(lineNumber, line);
		markLineIfNeeded(lineNumber, line, entry, header, region);
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
			j = line.indexOf('.', i);
		}
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

	public void dumpInfoToConfigSection() throws BadLocationException {
		StringBuilder sb = new StringBuilder();
		
		if (!hasConfigSection) {
			sb.append(MyContentOutlinePage.CONFIG_MARKER).append("\n\n");
			createdConfigSection = true;
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

	private void markLineIfNeeded(int lineNumber, String line, String entry, String header, IRegion region) {
		if (resource == null) {
			return;		// no resource, no markers
		}
		int severity = -1;
		for (MarkProblemInstruction markInstruction : configuration.getInstructions(MarkProblemInstruction.class)) {
			if (markInstruction.matches(line, entry, header)) {
				severity = markInstruction.getSeverity();
				break;
			}
		}
		if (severity >= 0) {
			addMarker(lineNumber, line, severity);
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
		if (outlineNodesMap.isEmpty()) {
			return new TreeNode[0];
		}
		
		final List<TreeNode> treeNodes = new ArrayList<>();
		for (OutlineNode<? extends OutlineNodeContent> outlineNode: outlineNodesMap.values()) { 
			TreeNode tn = outlineNode.createTreeNode(this);
			if (tn != null && !tn.isEmpty()) {
				tn.removeEmptyChildren();
				treeNodes.add(tn);
			}
		}
		
		TreeNode.removeEmptyRoots(treeNodes, null);
		
		//System.out.println("Tree nodes:");
		//dumpTreeNodes(treeNodes, 0);

		return treeNodes.toArray(new TreeNode[0]);
	}

	private void dumpTreeNodes(List<TreeNode> treeNodes, int level) {
		for (TreeNode treeNode : treeNodes) {
			indent("..", level);
			System.out.println(treeNode != null ? treeNode.getLabel() : "!!! NULL TreeNode !!!");
			dumpTreeNodes(treeNode.getChildren(), level+1);
		}
	}

	private void indent(String text, int level) {
		for (int i = 0; i <= level; i++) {
			System.out.print(text);
		}
	}
	
}
