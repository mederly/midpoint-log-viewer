package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;

public class MappingNodeDefinition extends OutlineNodeDefinition<MappingNodeContent> {

	private MappingNodeDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public MappingNodeContent recognize(int lineNumber, String line, String entry, String header, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("---[ MAPPING")) {
			return null;
		}
		MappingNodeContent content = new MappingNodeContent();
		content.setDefaultLabel(line.substring(5), "]---");
		return content;
	}

	@Override
	public ContentSelectionStrategy getContentSelectionStrategy() {
		return ContentSelectionStrategy.HEADER_LAST;
	}
	
	public static OutlineNodeDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new MappingNodeDefinition(editorConfiguration).parseFromLine(line);
	}
}
