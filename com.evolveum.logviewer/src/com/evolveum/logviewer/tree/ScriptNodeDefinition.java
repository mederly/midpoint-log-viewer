package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;

public class ScriptNodeDefinition extends OutlineNodeDefinition<ScriptNodeContent> {

	private ScriptNodeDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public ScriptNodeContent recognize(int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("---[ SCRIPT")) {
			return null;
		}
		ScriptNodeContent content = new ScriptNodeContent();
		content.setDefaultLabel(line.substring(5), "]---");
		return content;
	}

	@Override
	public ContentSelectionStrategy getContentSelectionStrategy() {
		return ContentSelectionStrategy.HEADER_LAST;
	}
	
	public static OutlineNodeDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new ScriptNodeDefinition(editorConfiguration).parseFromLine(line);
	}
}
