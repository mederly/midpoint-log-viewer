package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;

public class ExecutionNodeDefinition extends OutlineNodeDefinition<ExecutionNodeContent> {

	private ExecutionNodeDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public ExecutionNodeContent recognize(int lineNumber, String line, String entry, String header, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("---[ Going to EXECUTE")) {
			return null;
		}
		ExecutionNodeContent content = new ExecutionNodeContent();
		content.setDefaultLabel(line.substring(5), "]---");
		return content;
	}

	@Override
	public ContentSelectionStrategy getContentSelectionStrategy() {
		return ContentSelectionStrategy.HEADER_LAST;
	}
	
	public static OutlineNodeDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new ExecutionNodeDefinition(editorConfiguration).parseFromLine(line);
	}
}
