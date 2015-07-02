package com.evolveum.logviewer.outline;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider implements ITreeContentProvider {

	@Override
	public boolean hasChildren(Object element) {
		if (!(element instanceof TreeNode)) {
			System.err.println("Unexpected element: " + element);
		}
		TreeNode node = (TreeNode) element;
		return !node.getChildren().isEmpty();
	}

	@Override
	public Object[] getChildren(Object element) {
		if (!(element instanceof TreeNode)) {
			System.err.println("Unexpected element: " + element);
		}
		TreeNode node = (TreeNode) element;
		return node.getChildren().toArray();
	}

	@Override
	public Object[] getElements(Object input) {
		if (input instanceof Object[]) {
			return (Object[]) input;
		} else {
			return new Object[0];
		}
	}
	
	@Override
	public Object getParent(Object element) {
		if (!(element instanceof TreeNode)) {
			System.err.println("Unexpected element: " + element);
		}
		TreeNode node = (TreeNode) element;
		return node.getParent();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		System.out.println("newInput = " + newInput);
	}

}
