package com.evolveum.logviewer.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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

import com.evolveum.logviewer.editor.FoldingInstruction.Type;
import com.evolveum.logviewer.editor.FoldingInstruction.When;
import com.evolveum.logviewer.outline.MyContentOutlinePage;
import com.evolveum.logviewer.outline.ParsingUtils;

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
		ProjectionViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		
		viewer.invalidateTextPresentation();
		
		return viewer;
	}

	@Override
	protected void editorSaved() {
		super.editorSaved();
		if (outlinePage != null) {
			outlinePage.update();
		}
		applyConfiguration();
	}

	public void applyConfiguration() {
		ISourceViewer sourceViewer = getSourceViewer();
		
		((MyConfiguration) getSourceViewerConfiguration()).update(sourceViewer);
		sourceViewer.invalidateTextPresentation();
		
		applyKillInstructions(sourceViewer.getDocument());
		
		if (sourceViewer instanceof ProjectionViewer) {		// always should be
			ProjectionViewer projectionViewer = (ProjectionViewer) sourceViewer;
			ProjectionAnnotationModel pam = projectionViewer.getProjectionAnnotationModel();
			
			IDocument document = sourceViewer.getDocument();
			List<FoldingInstruction> instructions = OidUtils.getAllFoldingInstructions(document);
			applyFoldingInstructions(instructions, document, pam);
		} else {
			System.out.println("Unknown sourceViewer: " + sourceViewer);
		}
		
	}

	private void applyKillInstructions(IDocument document) {
		List<KillInstruction> instructions = OidUtils.getAllKillInstructions(document);
		System.out.println("Applying " + instructions.size() + " killing instructions.");
		if (instructions.isEmpty()) {
			return;
		}
		int killed = 0;
		try {
			int lineNumber = 0;
			for (;;) {
				
				int lines = document.getNumberOfLines();
				if (lineNumber >= lines) {
					break;
				}
				
				String line;
				for (;;) {
					line = DocumentUtils.getLine(document, lineNumber);
					if (ParsingUtils.isLogEntryStart(line)) {
						break;
					}
					lineNumber++;
					if (lineNumber >= lines) {
						return;						// next log entry was not found
					}
				}
				
				int logStartLineNumber = lineNumber;
				String logStartLine = line;
				
				StringBuilder logEntry = new StringBuilder();
				for (;;) {
					logEntry.append(line).append("\n");
					lineNumber++;
					if (lineNumber >= lines) {
						break;
					}
					line = DocumentUtils.getLine(document, lineNumber);
					if (ParsingUtils.isLogEntryStart(line)) {
						break;
					}
				}
				
				// lineNumber is at next log entry
				// logEntry contains whole entry
				
				for (KillInstruction instr : instructions) {
					if (instr.appliesTo(logStartLine, logEntry.toString())) {
						int start = document.getLineOffset(logStartLineNumber);
						int end;
						if (lineNumber >= lines) {
							end = document.getLength();
						} else {
							end = document.getLineOffset(lineNumber);
						}
						System.out.println("Killing lines #" + (logStartLineNumber+1) + " to #" + (lineNumber+1));
						document.replace(start, end-start, "");
						killed++;
						
						lineNumber = logStartLineNumber;			// restart at deleted line
						break;										// skipping other instructions
					}
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		System.out.println("Killed " + killed + " line(s)");
		
	}

	private void applyFoldingInstructions(List<FoldingInstruction> instructions, IDocument document, ProjectionAnnotationModel pam) {
		try {
			for (FoldingInstruction instr : instructions) {
				int lines = document.getNumberOfLines();
				for (int lineNumber = 0; lineNumber < lines; lineNumber++) {
					IRegion region = document.getLineInformation(lineNumber);
					String line = document.get(region.getOffset(), region.getLength());
					if (line.contains(MyContentOutlinePage.CONFIG_MARKER)) {
						break;
					}
					if (instr.when == When.CONTAINING && line.contains(instr.string)
							|| (instr.when == When.NOT_CONTAINING && !line.contains(instr.string))) {
						Iterator iter = pam.getAnnotationIterator(region.getOffset(), region.getLength(), false, true);
						if (!iter.hasNext()) {
//							System.out.println("Warn: no annotation for line " + (lineNumber+1)
//									+ " was found, skipping folding instruction");
							continue;
						}
						Object o = iter.next();
						if (o instanceof Annotation) {
							if (instr.type == Type.COLLAPSE) {
								pam.collapse((Annotation) o);
							} else {
								pam.expand((Annotation) o);
							}
						} else {
							System.out.println("Warn: Unknown annotation for line " + lineNumber + ": " + o);
						}
					}
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	
	
}

