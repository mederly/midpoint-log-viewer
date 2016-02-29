package com.evolveum.logviewer.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.evolveum.logviewer.actions.ActionExecutor;
import com.evolveum.logviewer.outline.MyContentOutlinePage;

public class LogViewerEditor extends TextEditor {

	private MyContentOutlinePage outlinePage;

	public LogViewerEditor() {
		super();
		MyColorManager colorManager = new MyColorManager();
		setSourceViewerConfiguration(new MySourceViewerConfiguration(colorManager));
		setDocumentProvider(new MyDocumentProvider());
	}
	
	public void dispose() {
		super.dispose();
	}
	
	@SuppressWarnings("rawtypes")
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
	
	@Override
	public void createPartControl(Composite parent) {
	    super.createPartControl(parent);
	    ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();

	    ProjectionSupport projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
	    projectionSupport.install();

	    //turn projection mode on
	    viewer.doOperation(ProjectionViewer.TOGGLE);

	    annotationModel = viewer.getProjectionAnnotationModel();
	}
	
	private Annotation[] currentAnnotations;
	private ProjectionAnnotationModel annotationModel;
	
	public void updateFoldingStructure(List<Position> positions) {
		System.out.println("*** Updating folding structure: " + positions.size() + " positions");
		
		Annotation[] newAnnotationsArray = new Annotation[positions.size()];

		// this will hold the new annotations along
		// with their corresponding positions
		Map<Annotation,Position> newAnnotationsMap = new HashMap<>();

		for (int i = 0; i < positions.size(); i++) {
			ProjectionAnnotation annotation = new ProjectionAnnotation();
			newAnnotationsMap.put(annotation, positions.get(i));
			newAnnotationsArray[i] = annotation;
		}

		annotationModel.modifyAnnotations(currentAnnotations, newAnnotationsMap, null);
		currentAnnotations = newAnnotationsArray;
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ProjectionViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		getSourceViewerDecorationSupport(viewer);				// ensure decoration support has been created and configured.
		viewer.invalidateTextPresentation();
		return viewer;
	}

	@Override
	protected void editorSaved() {
		super.editorSaved();
		applyConfigurationAndActions();
		if (outlinePage != null) {
			outlinePage.update();
		}
	}

	public void applyConfigurationAndActions() {
		System.out.println("==> Starting application of configuration and actions <==");
		long started = System.currentTimeMillis();

		ISourceViewer sourceViewer = getSourceViewer();
		((MySourceViewerConfiguration) getSourceViewerConfiguration()).update(sourceViewer);
		sourceViewer.invalidateTextPresentation();
		
		ActionExecutor.executeActions(sourceViewer);
		
		System.out.println("==> Configuration and actions applied in " + (System.currentTimeMillis()-started) + " msec <==");
	}
	
}

