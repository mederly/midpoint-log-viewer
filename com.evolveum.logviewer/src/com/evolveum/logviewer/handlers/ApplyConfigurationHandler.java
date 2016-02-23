package com.evolveum.logviewer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.evolveum.logviewer.editor.LogViewerEditor;

public class ApplyConfigurationHandler extends AbstractHandler {

	private static final String DEFAULT_DIALOG_TITLE = "Evolveum Log Viewer";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof LogViewerEditor)) {
			MessageDialog.openError(null, DEFAULT_DIALOG_TITLE, "This action is available only in the Evolveum Log Viewer.");
		}
		LogViewerEditor logViewerEditor = (LogViewerEditor) editor;
		logViewerEditor.applyConfigurationAndActions();
		return null;
		
	}

}
