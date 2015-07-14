package com.evolveum.logviewer.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.evolveum.logviewer.editor.LogViewerEditor;

public class MyContentOutlinePage extends ContentOutlinePage {
	
	public static final String CONFIG_MARKER = "%%% CONFIGURATION %%%"; 
	
	private LogViewerEditor editor;

	public MyContentOutlinePage(IDocumentProvider documentProvider, LogViewerEditor editor) {
		this.editor = editor;
	}
	
	private IEditorInput editorInput;

	public void setInput(IEditorInput editorInput) {
		this.editorInput = editorInput;
		update();
	}
	
	public void update() {
	    TreeViewer viewer = getTreeViewer();
	    if (viewer != null) {
	        Control control = viewer.getControl();
	        if (control != null && !control.isDisposed()) {
	            control.setRedraw(false);
	            TreeNode[] outlineInput = parseEditorInput();
	            viewer.setInput(outlineInput);
	            //viewer.();
	            control.setRedraw(true);
	        }
	    }
	}

	private TreeNode[] parseEditorInput() {
		if (editorInput == null) {
			return new TreeNode[0];
		}
		IDocument document = editor.getDocumentProvider().getDocument(editorInput);
		
		int lines = document.getNumberOfLines();
		System.out.println("Lines: " + lines);
		long start = System.currentTimeMillis();

		Parser parser = new Parser(document);
		
		boolean inConfigSection = false;
		
		for (int lineNumber = 0; lineNumber < lines; lineNumber++) {
			try {
				IRegion region = document.getLineInformation(lineNumber);
				String line = getLine(document, region);
				if (line.equals(CONFIG_MARKER) || inConfigSection) {
					inConfigSection = true;
					parser.onConfigLine(lineNumber, line, region);
					continue;
				}
				
				parser.onAnyLine(lineNumber, line, region);
				if (ParsingUtils.isLogEntryStart(line)) {
					parser.onLogEntryLine(lineNumber, line, region);
				} else if (line.startsWith("---[ PROJECTOR") || line.startsWith("---[ CLOCKWORK")) {
					parser.onContextDumpStart(lineNumber, line, region);
				} else if (line.startsWith("---[ SCRIPT")) {
					parser.onScriptStart(lineNumber, line, region);
				} else if (line.startsWith("---[ EXPRESSION")) {
					parser.onExpressionStart(lineNumber, line, region);					
				} else if (line.startsWith("---[ MAPPING")) {
					parser.onMappingStart(lineNumber, line, region);					
				} else if (line.startsWith("    PROJECTION ShadowType Discr")) {
					parser.onProjectionContextDumpStart(lineNumber, line, region);
				} else if (line.startsWith("---[ Going to EXECUTE")) {
					parser.onGoingToExecute(lineNumber, line, region);		
				} else if (line.startsWith("###[ CLOCKWORK SUMMARY")) {
					parser.onClockworkSummary(lineNumber, line, region);
				} else if (line.startsWith("---[")) {
					parser.onMappingStart(lineNumber, line, region);		// temporary solution					
				} 
			} catch (BadLocationException e) {
				System.err.println("Couldn't parse line #" + lineNumber + ": " + e);
			}
		}
		System.out.println("### FOLDING REGIONS: " + parser.foldingRegions.size());
		editor.updateFoldingStructure(parser.foldingRegions);
		System.out.println("Parsed in " + (System.currentTimeMillis()-start) + " ms");
		try {
			parser.dumpInfo();
		} catch (BadLocationException e) {
			System.err.println("Couldn't dump info: " + e);
		}
		return parser.nodes.toArray(new TreeNode[0]);
	}

	private String getLine(IDocument document, IRegion region) throws BadLocationException {
		return document.get(region.getOffset(), region.getLength());
	}

	@Override
	public void createControl(Composite parent) {

		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();
		viewer.setLabelProvider(new TreeLabelProvider());
		viewer.setContentProvider(new TreeContentProvider());
		
		viewer.addSelectionChangedListener(this);
		update();
	}
	
	@Override
	protected int getTreeStyle() {
		return SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
	}

	public void selectionChanged(SelectionChangedEvent event) {

		super.selectionChanged(event);

		ISelection selection = event.getSelection();
		
		TreeNode node = null;
		
		if (selection.isEmpty()) {
			editor.resetHighlightRange();
		} else if (!(selection instanceof TreeSelection)) {
			System.err.println("Not a TreeSelection: " + selection);
		} else {
			TreeSelection ts = (TreeSelection) selection;
			if (ts.getPaths().length > 0) {
				TreePath path = ts.getPaths()[0];		// should be only one
				Object lastSegment = path.getLastSegment();
				if (lastSegment instanceof TreeNode) {
					node = (TreeNode) lastSegment;
				} 
			}
		}
		
		if (node != null) {
			try {
				editor.setHighlightRange(node.getOffset(), node.getLength(), true);
			} catch (IllegalArgumentException x) {
				editor.resetHighlightRange();
			}
		} else {
			editor.resetHighlightRange();			
		}
	}

//	private String dumpPaths(TreePath[] paths) {
//		StringBuilder sb = new StringBuilder();
//		for (TreePath path : paths) {
//			sb.append("[path with last = ").append(path.getLastSegment()).append("] ");
//		}
//		return sb.toString();
//	}
}
