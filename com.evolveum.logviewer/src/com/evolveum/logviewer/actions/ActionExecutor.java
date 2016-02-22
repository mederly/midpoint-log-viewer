package com.evolveum.logviewer.actions;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import com.evolveum.logviewer.config.ConfigurationParser;
import com.evolveum.logviewer.config.FoldingInstruction;

public class ActionExecutor {

	public static void executeActions(ISourceViewer sourceViewer) {
		
		LineKiller.applyKillInstructions(sourceViewer.getDocument());
		
		if (sourceViewer instanceof ProjectionViewer) {		// always should be
			ProjectionViewer projectionViewer = (ProjectionViewer) sourceViewer;
			ProjectionAnnotationModel pam = projectionViewer.getProjectionAnnotationModel();
			
			IDocument document = sourceViewer.getDocument();
			List<FoldingInstruction> instructions = ConfigurationParser.getAllFoldingInstructions(document);
			FoldingExecutor.applyFoldingInstructions(instructions, document, pam);
		} else {
			System.out.println("Unknown sourceViewer: " + sourceViewer);
		}
	}


}
