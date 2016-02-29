package com.evolveum.logviewer.tree;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class ContextNodeDefinition extends OutlineNodeDefinition<ContextNodeContent> {

	private static final ContentSelectionStrategy CONTENT_SELECTION_STRATEGY = new ContextDumpContentSelectionStrategy();

	private ContextNodeDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}
	
	private static class ContextDumpContentSelectionStrategy implements ContentSelectionStrategy {

		@Override
		public Result computeContent(OutlineNode<?> outlineNode, TreeMap<Integer, OutlineNode<?>> availableNodes) {

			NavigableMap<Integer, OutlineNode<?>> content = ContentSelectionStrategy.HEADER_LAST.computeContent(outlineNode, availableNodes).getContent();
			int startLine = !content.isEmpty() ? content.firstKey() : outlineNode.getStartLine();
			
			// find next log line
			
			IDocument document = outlineNode.getDocument();
			Integer nextLogLine = ParsingUtils.findNextLogLine(document, outlineNode.getStartLine()+1);
			if (nextLogLine == null) {
				nextLogLine = document.getNumberOfLines();
			}
			
			TreeMap<Integer, OutlineNode<?>> subNodes = new TreeMap<>(availableNodes.subMap(startLine, true, nextLogLine, false));
			subNodes.remove(outlineNode.getStartLine());		// context dump node should not be in the result!
			
//			System.out.print("# ContextDumpCSS for line " + outlineNode.getStartLine() + " returned: ");
//			if (subNodes.isEmpty()) {
//				System.out.print("(empty map)");
//			} else {
//				System.out.print(subNodes.firstKey() + ".." + subNodes.lastKey());
//			}
//			System.out.println(", continuing after " + nextLogLine);
			
			return new Result(subNodes, nextLogLine);
		}
		
	}

	public static String recognizeLine(String line) {
		if (line.contains("---[ SYNCHRONIZATION")) {
			return line.substring(line.indexOf("---["));
		} else if (line.startsWith("---[ PROJECTOR") || 
				line.startsWith("---[ CLOCKWORK") ||
				line.startsWith("---[ preview")) {
			return line;
		} else {
			return null;
		}
	}

	@Override
	public ContextNodeContent recognize(int lineNumber, String line, String entry, String header, IRegion region, IDocument document) throws BadLocationException {

		String lineContent = recognizeLine(line);
		if (lineContent == null) {
			return null;
		}
		
		ContextNodeContent content = new ContextNodeContent();
		String line2 = DocumentUtils.getLine(document, lineNumber+1);
		content.parseWaveInfo(line2);
		content.parseLabelCore(lineContent);
		content.setLabelSuffix("");
		return content;
	}

	@Override
	public ContentSelectionStrategy getContentSelectionStrategy() {
		return CONTENT_SELECTION_STRATEGY;
	}
	
	public static OutlineNodeDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new ContextNodeDefinition(editorConfiguration).parseFromLine(line);
	}

}
