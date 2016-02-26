package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;

public class ExpressionNodeDefinition extends OutlineNodeDefinition<ExpressionNodeContent> {

	private ExpressionNodeDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public ExpressionNodeContent recognize(int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("---[ EXPRESSION")) {
			return null;
		}
		ExpressionNodeContent content = new ExpressionNodeContent();
		content.setDefaultLabel(line.substring(5), "]---");
		return content;
	}

	@Override
	public ContentSelectionStrategy getContentSelectionStrategy() {
		return ContentSelectionStrategy.HEADER_LAST;
	}
	
	public static OutlineNodeDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new ExpressionNodeDefinition(editorConfiguration).parseFromLine(line);
	}
}
