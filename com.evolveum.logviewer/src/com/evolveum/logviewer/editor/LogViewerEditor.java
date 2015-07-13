package com.evolveum.logviewer.editor;

import java.util.HashMap;
import java.util.List;

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

import com.evolveum.logviewer.outline.MyContentOutlinePage;

public class LogViewerEditor extends TextEditor {

	private MyContentOutlinePage outlinePage;

	public LogViewerEditor() {
		super();
		ColorManager colorManager = new ColorManager();
		MyConfiguration myconfig = new MyConfiguration(colorManager);
		setSourceViewerConfiguration(myconfig);
		setDocumentProvider(new MyDocumentProvider());
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
	
	private Annotation[] oldAnnotations;
	private ProjectionAnnotationModel annotationModel;
	
	public void updateFoldingStructure(List positions) {
		System.out.println("*** Updating folding structure: " + positions.size() + " positions");
		
		Annotation[] annotations = new Annotation[positions.size()];

		// this will hold the new annotations along
		// with their corresponding positions
		HashMap newAnnotations = new HashMap();

		for (int i = 0; i < positions.size(); i++) {
			ProjectionAnnotation annotation = new ProjectionAnnotation();
			newAnnotations.put(annotation, positions.get(i));
			annotations[i] = annotation;
		}

		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
		oldAnnotations = annotations;
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		
		viewer.invalidateTextPresentation();

		return viewer;
	}

	@Override
	protected void editorSaved() {
		super.editorSaved();
		outlinePage.update();
		((MyConfiguration) getSourceViewerConfiguration()).update(getSourceViewer());
		getSourceViewer().invalidateTextPresentation();
	}

	
	
	
	
}

