package com.evolveum.logviewer.outline;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
	
	private List<TreeNode> children = new ArrayList<>();
	private TreeNode parent = null;
	
	private String label;
	
	private int offset, length;

	public TreeNode(String label, int offset, int length) {
		this.label = label;
		this.offset = offset;
		this.length = length;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public TreeNode getParent() {
		return parent;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void addChild(TreeNode n1) {
		if (n1.getParent() != null) {
			throw new IllegalStateException("Adding child with a parent: " + n1);
		}
		children.add(n1);
		n1.setParent(this);
	}

	private void setParent(TreeNode treeNode) {
		parent = treeNode;
	}

	@Override
	public String toString() {
		return "TreeNode [label=" + label + ", offset=" + offset + ", length=" + length + "]";
	}

	public void addChildren(List<TreeNode> nodes) {
		for (TreeNode node : nodes) {
			addChild(node);
		}
	}
	
}
