package com.evolveum.logviewer.editor;

import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.evolveum.logviewer.outline.MyContentOutlinePage;

public class LogViewerEditor extends TextEditor {

	private MyContentOutlinePage outlinePage;

	public LogViewerEditor() {
		super();
	}
	
	public void dispose() {
		super.dispose();
	}
	
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (outlinePage == null) {
				outlinePage = new MyContentOutlinePage(getDocumentProvider(), this);
				if (getEditorInput() != null) {
					outlinePage.setInput(getEditorInput());
				}
			}
			return outlinePage;
		}
		return super.getAdapter(required);
	}
}
