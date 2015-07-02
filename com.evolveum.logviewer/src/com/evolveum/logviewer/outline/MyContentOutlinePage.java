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
		List<TreeNode> nodes = new ArrayList<>();
		int lines = document.getNumberOfLines();
		System.out.println("Lines: " + lines);
		long start = System.currentTimeMillis();

		List<TreeNode> mappings = new ArrayList<TreeNode>();
		List<TreeNode> scriptsAndExpressions = new ArrayList<TreeNode>();
		
		for (int lineNumber = 0; lineNumber < lines; lineNumber++) {
			try {
				IRegion region = document.getLineInformation(lineNumber);
				String line = document.get(region.getOffset(), region.getLength());
				if (line.startsWith("---[ PROJECTOR") || line.startsWith("---[ CLOCKWORK")) {
					IRegion region2 = document.getLineInformation(lineNumber+1);
					String line2 = document.get(region2.getOffset(), region2.getLength());
					TreeNode node = new TreeNode(getWaveInfo(line2) + " " + line.substring(5), region.getOffset(), region.getLength());
					nodes.add(node);
					node.addChildren(mappings);
					mappings.clear();
					node.addChildren(scriptsAndExpressions);	// shouldn't be any
					scriptsAndExpressions.clear();
				} else if (line.startsWith("---[ SCRIPT") || line.startsWith("---[ EXPRESSION")) {
					TreeNode node = new TreeNode(line.substring(5), region.getOffset(), region.getLength());
					scriptsAndExpressions.add(node);
				} else if (line.startsWith("---[ MAPPING")) {
					TreeNode node = new TreeNode(line.substring(5), region.getOffset(), region.getLength());
					mappings.add(node);
					node.addChildren(scriptsAndExpressions);
					scriptsAndExpressions.clear();
				} else if (line.startsWith("---[")) {
					// add to mappings (temporarily)
					TreeNode node = new TreeNode(line.substring(5), region.getOffset(), region.getLength());
					mappings.add(node);
					node.addChildren(scriptsAndExpressions);
					scriptsAndExpressions.clear();
				}
			} catch (BadLocationException e) {
				System.err.println("Couldn't parse line #" + lineNumber + ": " + e);
			}
		}
		System.out.println("Parsed in " + (System.currentTimeMillis()-start) + " ms");
		return nodes.toArray(new TreeNode[0]);
	}

	// input like this: LensContext: state=SECONDARY, Wave(e=1,p=1,max=0), focus, 2 projections, 2 changes, fresh=true
	private String getWaveInfo(String line) {
		int i = line.indexOf("Wave(");
		if (i < 0) {
			return "?";
		}
		int j = line.indexOf(')', i);
		return line.substring(i, j+1);
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

	private String dumpPaths(TreePath[] paths) {
		StringBuilder sb = new StringBuilder();
		for (TreePath path : paths) {
			sb.append("[path with last = ").append(path.getLastSegment()).append("] ");
		}
		return sb.toString();
	}
}
