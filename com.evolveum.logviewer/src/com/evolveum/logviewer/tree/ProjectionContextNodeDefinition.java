package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;

public class ProjectionContextNodeDefinition extends OutlineNodeDefinition<ProjectionContextNodeContent> {

	private ProjectionContextNodeDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public ProjectionContextNodeContent recognize(int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("    PROJECTION ShadowType Discr")) {
			return null;
		}
		ProjectionContextNodeContent content = new ProjectionContextNodeContent();
		content.setDefaultLabel(line.substring(4));
		return content;
	}

	@Override
	public ContentSelectionStrategy getContentSelectionStrategy() {
		return ContentSelectionStrategy.HEADER_LAST;
	}
	
	public static OutlineNodeDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new ProjectionContextNodeDefinition(editorConfiguration).parseFromLine(line);
	}
}
