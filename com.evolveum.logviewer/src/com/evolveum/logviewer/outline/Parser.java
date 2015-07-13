package com.evolveum.logviewer.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

public class Parser {

	int numberOfLines;
	IDocument document;
	
	List<TreeNode> nodes = new ArrayList<>();

	// ContextDump that was lastly created
	ContextDumpItem lastContextDump = null;
	
	// ContextDump we are currently physically in - the next log line sets this to null
	ContextDumpItem currentContextDump = null;
	
	// scripts and expressions will be aggregated into following mapping
	// there are exceptions, however - like expressions in notifications
	List<TreeNode> scriptsAndExpressions = new ArrayList<>();
	
	// mappings and "going to execution" items are aggregated into the following context dump
	List<DocumentItem> mappingsAndExecutions = new ArrayList<>();
	
	List<Position> foldingRegions = new ArrayList<Position>();
	
	Map<String,OidInfo> discoveredOidInfos = new HashMap<>();
	List<OidInfo> configuredOidInfos = new ArrayList<OidInfo>();
	
	boolean hasConfigSection = false;
	
	public Parser(IDocument document) {
		this.document = document;
		this.numberOfLines = document.getNumberOfLines();
	}

	public void onLogEntryLine(int lineNumber, String line, IRegion region) {
		// This may be a line that closes a context dump.
		if (currentContextDump != null) {
			nodes.add(currentContextDump.createTreeNode(this));
			currentContextDump = null;
		}
	}

	public void onContextDumpStart(int lineNumber, String line, IRegion region) throws BadLocationException {
		currentContextDump = new ContextDumpItem(region, lineNumber);
		
		String line2 = getLine(lineNumber+1);
		currentContextDump.parseWaveInfo(line2);
		currentContextDump.labelCore = line.substring(5);
		currentContextDump.labelSuffix = suffix(document, lineNumber);
	}
	
	private String getLine(int number) throws BadLocationException {
		IRegion region = document.getLineInformation(number);
		String line = document.get(region.getOffset(), region.getLength());
		return line;
	}

	public void onScriptStart(int lineNumber, String line, IRegion region) throws BadLocationException {
		onScriptOrExpression(lineNumber, line, region);
	}

	public void onExpressionStart(int lineNumber, String line, IRegion region) throws BadLocationException {
		onScriptOrExpression(lineNumber, line, region);
	}

	private void onScriptOrExpression(int lineNumber, String line, IRegion region) throws BadLocationException {
		String label = line.substring(5) + suffix(document, lineNumber);
		TreeNode node = new TreeNode(label, region);
		scriptsAndExpressions.add(node);
	}

	public void onMappingStart(int lineNumber, String line, IRegion region) throws BadLocationException {
		String label = line.substring(5) + suffix(document, lineNumber);
		TreeNode node = new TreeNode(label, region);		
		node.addChildren(scriptsAndExpressions);
		scriptsAndExpressions.clear();
		mappingsAndExecutions.add(new MappingItem(region, lineNumber, node));
	}
	
	public void onGoingToExecute(int lineNumber, String line, IRegion region) throws BadLocationException {
		String label = "--> " + line.substring(5) + suffix(document, lineNumber);
		TreeNode node = new TreeNode(label, region);		
		node.addChildren(scriptsAndExpressions);
		scriptsAndExpressions.clear();		
		mappingsAndExecutions.add(new ExecutionItem(region, lineNumber, node));
	}
	
	public void onClockworkSummary(int lineNumber, String line, IRegion region) throws BadLocationException {
		flushMappingsAndScriptsAndExpressions();
		TreeNode node = new TreeNode(line + suffix(document, lineNumber), region);
		nodes.add(node);
		currentContextDump = null;
	}
	
	private void flushMappingsAndScriptsAndExpressions() {
		for (DocumentItem item : mappingsAndExecutions) {
			if (item.treeNode != null) {
				nodes.add(item.treeNode);
			} else {
				System.err.println("Problem - mapping/execution without treeNode: " + item);
			}
		}
		nodes.addAll(scriptsAndExpressions);
		mappingsAndExecutions.clear();
		scriptsAndExpressions.clear();
	}

	private String suffix(IDocument document, int lineNumber) throws BadLocationException {
		String date = "?";
		if (lineNumber > 0) {
			IRegion previousRegion = document.getLineInformation(lineNumber-1);
			String previousLine = document.get(previousRegion.getOffset(), previousRegion.getLength());
			if (previousLine.length() >= 23) {
				date = previousLine.substring(0, 23); 
			}
		}
		return " " + date + " (#" + lineNumber + ")";
	}

	public void onProjectionContextDumpStart(int lineNumber, String line, IRegion region) {
		if (currentContextDump != null) {
			TreeNode node = new TreeNode(line, region.getOffset(), region.getLength());
			currentContextDump.addProjectionContextTreeNode(node);
		}		
	}

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
		if (MyContentOutlinePage.isLogEntryStart(line)) {
			processLogEntryFolding(lineNumber, line);
		} else {
			processIndentBasedFolding(lineNumber, line);
		}
	}
	
	private void processLogEntryFolding(int lineNumber, String line) throws BadLocationException {
		int endLine = lineNumber + 1;
		while (endLine < numberOfLines - 1) {
			String s = getLine(endLine);
			if (MyContentOutlinePage.isLogEntryStart(s)) {
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
		
		// preserve only new entries
		List<OidInfo> reallyNewOidInfoList = new ArrayList<>();
		
		Iterator<Map.Entry<String,OidInfo>> iter = discoveredOidInfos.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, OidInfo> entry = iter.next();
			boolean found = false;
			for (OidInfo cfg : configuredOidInfos) {
				if (cfg.oid.equals(entry.getKey())) {
					found = true;
				}
			}
			if (!found) {
				reallyNewOidInfoList.add(entry.getValue());
			}
		}
		
		if (reallyNewOidInfoList.isEmpty()) {
			return;
		}
		
		Collections.sort(reallyNewOidInfoList, new Comparator<OidInfo>() {

			@Override
			public int compare(OidInfo o1, OidInfo o2) {
				return o1.type.toLowerCase().compareTo(o2.type.toLowerCase());
			}
			
		});
		
		for (OidInfo oidInfo : reallyNewOidInfoList) {
			sb.append('%').append(oidInfo.oid).append(" : ").append(oidInfo.color).append(" : ").append(oidInfo.type).append(" ").append(oidInfo.names);
			sb.append("\n");
		}
		String s = sb.toString();
		document.set(document.get() + "\n" + s);
	}

	public void onConfigLine(int lineNumber, String line, IRegion region) {
		hasConfigSection = true;
		
		if (line.isEmpty()) {
			return;
		}
		if (line.startsWith("%")) {
			OidInfo parsed = OidInfo.parseFromLine(line);
			if (parsed != null) {
				configuredOidInfos.add(parsed);
			}
		}
	}
	

	
}
