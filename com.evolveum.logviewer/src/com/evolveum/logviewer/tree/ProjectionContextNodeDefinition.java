package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class ProjectionContextNodeDefinition extends OutlineNodeDefinition<ProjectionContextNodeContent> {

	private ProjectionContextNodeDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public ProjectionContextNodeContent recognize(int lineNumber, String line, String entry, String header, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("    PROJECTION ShadowType Discr")) {
			return null;
		}
		
		// necessary condition - this projection context is part of a context dump!
		int lineNumber1 = lineNumber;
		while (--lineNumber1 >= 0) {
			String line1 = DocumentUtils.getLine(document, lineNumber1);
			if (ContextNodeDefinition.recognizeLine(line1) != null) {
				break;
			}
			if (ParsingUtils.isLogEntryStart(line1)) {
				return null;
			}
		}
		if (lineNumber1 < 0) {
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
