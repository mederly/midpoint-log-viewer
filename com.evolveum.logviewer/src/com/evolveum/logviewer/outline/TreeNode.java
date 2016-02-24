package com.evolveum.logviewer.outline;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.tree.OutlineNode;
import com.evolveum.logviewer.tree.OutlineNodeContent;

public class TreeNode {
	
	private List<TreeNode> children = new ArrayList<>();
	private TreeNode parent = null;
	private OutlineNode<? extends OutlineNodeContent> outlineNode;
	
	private String label;
	
	private int offset, length;

	public TreeNode(OutlineNode<? extends OutlineNodeContent> outlineNode, String label, int offset, int length) {
		this.outlineNode = outlineNode;
		this.label = label;
		this.offset = offset;
		this.length = length;
	}

	public TreeNode(OutlineNode<? extends OutlineNodeContent> outlineNode, String label, IRegion region) {
		this(outlineNode, label, region.getOffset(), region.getLength());
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
	
	public Integer getStartLine() {
		if (outlineNode != null) {
			return outlineNode.getStartLine();
		} else {
			return null;
		}
	}
	
	public Date getDate() {
		if (outlineNode != null) {
			return outlineNode.getDate();
		} else {
			return null;
		}
	}
	
	public String getDelta() {
		if (outlineNode != null) {
			return outlineNode.getDelta();
		} else {
			return "";
		}
	}
	
	public String getSum() {
		if (outlineNode != null) {
			return outlineNode.getSum();
		} else {
			return "";
		}
	}

	public void addChild(TreeNode n1) {
		if (n1 == null) {
			return;			// only in errors
		}
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

	public boolean isEmpty() {
		if (!hasEmptyRoot()) {
			return false;
		}
		for (TreeNode child : children) {
			if (!child.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private boolean hasEmptyRoot() {
		return outlineNode == null || outlineNode.getContent() == null;
	}

	public void removeEmptyChildren() {
		Iterator<TreeNode> iterator = children.iterator();
		while (iterator.hasNext()) {
			TreeNode child = iterator.next();
			if (child.isEmpty()) {
				iterator.remove();
			} else {
				child.removeEmptyChildren();
			}
		}
	}

	public static void removeEmptyRoots(List<TreeNode> nodes, TreeNode parent) {
		for (int i = 0; i < nodes.size(); i++) {
			TreeNode tn = nodes.get(i);
			if (tn.hasEmptyRoot()) {
				nodes.remove(i);
				int restartAt = i;
				for (TreeNode tn1 : tn.children) {
					tn1.setParent(parent);
					nodes.add(i++, tn1);
				}
				i = restartAt;
				tn = nodes.get(i);
			}
			removeEmptyRoots(tn.children, tn);
		}
	}
	
}
