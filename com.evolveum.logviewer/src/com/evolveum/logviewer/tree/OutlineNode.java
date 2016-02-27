package com.evolveum.logviewer.tree;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.Parser;
import com.evolveum.logviewer.parsing.ParsingUtils;
import com.evolveum.logviewer.tree.ContentSelectionStrategy.Result;

/**
 *
 * Any relevant log file section. See subclasses.
 * 
 * @author mederly
 */

public class OutlineNode<C extends OutlineNodeContent> {

	/*
	 * Tree-position-independent information, gathered during first pass through the logfile. 
	 */
	final EditorConfiguration editorConfiguration;
	final private int level;
	final private Integer line;
	final private IRegion region;
	final private Date date;
	final private String thread;
	final private IDocument document; 
	
	final private C content;
	final private OutlineNodeDefinition<C> nodeDefinition;
	
	/*
	 * Outline tree position information.
	 */
	private TreeMap<Integer,OutlineNode<?>> contentMap;			// intermediary data structure - contains all subnodes, iteratively being sorted out to outline subtree
	private TreePosition treePosition;
	
	/*
	 * Other info.
	 */
	
	private String delta, sum;
	
	
	public OutlineNode(OutlineNodeDefinition<C> levelDefinition, C content, IRegion region, int startLineNumber, String line, IDocument document) {
		this.editorConfiguration = levelDefinition.getEditorConfiguration();
		this.level = levelDefinition.getLevel();
		this.nodeDefinition = levelDefinition;
		this.content = content;
		content.setOwner(this);
		this.region = region;
		this.line = startLineNumber;
		
		final String logLine;
		if (ParsingUtils.isLogEntryStart(line)) {
			logLine = line;
		} else if (startLineNumber > 1) {
			logLine = DocumentUtils.getLine(document, startLineNumber-1);
		} else {
			logLine = null;
		}
		date = ParsingUtils.parseDate(logLine);
		thread = ParsingUtils.parseThread(logLine, editorConfiguration.componentNames);
		this.document = document;
	}
	
	public OutlineNodeDefinition<C> getLevelDefinition() {
		return nodeDefinition;
	}
	
	public int getLevel() {
		return level;
	}
	
	public String getThread() {
		return thread;
	}

	public C getContent() {
		return content;
	}
	
	public IRegion getRegion() {
		return region;
	}

	public Integer getStartLine() {
		return line;
	}

	public Date getDate() {
		return date;
	}
	
	public IDocument getDocument() {
		return document;
	}

	@SuppressWarnings("unchecked")
	public <T extends OutlineNodeContent> List<OutlineNode<T>> getAllChildren(Class<T> contentClass) {
		checkTreePositionInitialized();
		
		List<OutlineNode<T>> rv = new ArrayList<>();
		for (OutlineNode<?> child : treePosition.children) {
			if (child.getContent() != null) {
				if (contentClass.isAssignableFrom(child.getContent().getClass())) {
					rv.add((OutlineNode<T>) child);
				}
			}
		}
		return rv;
	}
	
	//@SuppressWarnings("unchecked")
	public List<OutlineNode<?>> getAllChildren(Class<? extends OutlineNodeContent>... contentClasses) {
		checkTreePositionInitialized();
		List<OutlineNode<?>> rv = new ArrayList<>();
		for (OutlineNode<?> child : treePosition.children) {
			if (contentClasses.length == 0) {
				rv.add((OutlineNode<?>) child);					
			} else if (child.getContent() != null) {
				for (Class<? extends OutlineNodeContent> contentClass : contentClasses) {
					if (contentClass.isAssignableFrom(child.getContent().getClass())) {
						rv.add((OutlineNode<?>) child);
						break;
					}
				}
			}
		}
		return rv;
	}
	
	//@SuppressWarnings("unchecked")
	public List<OutlineNode<?>> getAllChildrenRecursive(Class<? extends OutlineNodeContent>... contentClasses) {
		List<OutlineNode<?>> rv = new ArrayList<>();
		addAllChildrenRecursive(rv, contentClasses);
		return rv;
	}
	
	public void addAllChildrenRecursive(List<OutlineNode<?>> list, Class<? extends OutlineNodeContent>... contentClasses) {
		checkTreePositionInitialized();
		for (OutlineNode<?> child : treePosition.children) {
			if (contentClasses.length == 0) {
				list.add((OutlineNode<?>) child);
				child.addAllChildrenRecursive(list, contentClasses);
			} else if (child.getContent() != null) {
				for (Class<? extends OutlineNodeContent> contentClass : contentClasses) {
					if (contentClass.isAssignableFrom(child.getContent().getClass())) {
						list.add((OutlineNode<?>) child);
						child.addAllChildrenRecursive(list, contentClasses);
						break;
					}
				}
			}
		}
	}

	private void checkTreePositionInitialized() {
		if (treePosition == null) {
			throw new IllegalStateException("Tree position is not initialized in " + this);
		}
	}


	private TreeNode cachedTreeNode;
	
	public TreeNode createTreeNode(Parser parser) {
		if (cachedTreeNode != null) {
			return cachedTreeNode;
		}
		if (content != null) {
			cachedTreeNode = content.createTreeNode(parser);
		} else {
			TreeNode treeNode = new TreeNode(this, "<none>", 0, 1);		// ????
			for (OutlineNode<? extends OutlineNodeContent> node : getAllChildren()) {
				treeNode.addChild(node.createTreeNode(parser));
			}
			cachedTreeNode = treeNode;
		}
		return cachedTreeNode;
	}


//	public void dumpAll(Parser parser) {
//		for (int i = 0; i < level; i++) {
//			System.out.print("  ");
//		}
//		TreeNode treeNode = createTreeNode(parser);
//		String label = treeNode != null ? treeNode.getLabel() : "(no tree node)";
//		System.out.println("Label: " + label + "; Content: " + content);
//		if (firstChild != null) {
//			firstChild.dumpAll(parser);
//		}
//		if (nextSibling != null) {
//			nextSibling.dumpAll(parser);
//		}
//	}
	
	
	
	@Override
	public String toString() {
		return "OutlineNode [level=" + level + 
				", CMap" + (contentMap != null ? "+" : "-") + 
				" TP" + (treePosition != null ? "+" : "-") + 
				" content=" + content + ", levelDefinition="
				+ nodeDefinition + ", startLine=" + line + ", date=" + date + "]";
	}

	public String getDelta() {
		return delta;
	}

	public void setDelta(String delta) {
		this.delta = delta;
	}

	public String getSum() {
		return sum;
	}

	public void setSum(String sum) {
		this.sum = sum;
	}
	
	public class TreePosition {
		
		private final List<OutlineNode<?>> children = new ArrayList<>();
		private final OutlineNode<?> parent;
		private OutlineNode<?> previousSibling;
		
		TreePosition(OutlineNode<?> parent) {
			this.parent = parent;
		}

		public List<OutlineNode<?>> getChildren() {
			return children;
		}

		public OutlineNode<?> getParent() {
			return parent;
		}

		public OutlineNode<?> getPreviousSibling() {
			return previousSibling;
		}
		
	}

	public OutlineNode<?> getPreviousSibling() {
		checkTreePositionInitialized();
		return treePosition.getPreviousSibling();
	}

	// return the line from which the parsing should continue (not including it)
	public int createContentMap(TreeMap<Integer, OutlineNode<?>> availableNodes) {
		ContentSelectionStrategy strategy = nodeDefinition.getContentSelectionStrategy();
		Result result = strategy.computeContent(this, availableNodes);
		contentMap = new TreeMap<>(result.getContent());
		checkContentMap();
		return result.getContinueParsingAfter();
		
	}

	private void checkContentMap() {
		Iterator<OutlineNode<?>> iterator = contentMap.values().iterator();
		while (iterator.hasNext()) {
			OutlineNode<?> child = iterator.next();
			if (child.getLevel() <= this.getLevel()) {
				System.err.println("Miscalculated child entry; it's level (" + child.getLevel() + ") is not greater than the level of this node (" + this.getLevel() + "). This node = " + this);
				iterator.remove(); 							// return to parent level
				if (child.getContentMap() == null) {		// very probably so
					child.contentMap = new TreeMap<>();
				}
			}
		}
	}

	public TreeMap<Integer, OutlineNode<?>> getContentMap() {
		return contentMap;
	}
	
	public static void createTreePositions(OutlineNode<?> parent, TreeMap<Integer,OutlineNode<?>> nodesToProcess) {
		OutlineNode<?> previous = null;
		for (OutlineNode<?> node : nodesToProcess.values()) {
			node.createTreePosition(parent, previous);
			previous = node;
		}
	}

	public void createTreePosition(OutlineNode<?> parent, OutlineNode<?> previousSibling) {
		if (treePosition != null) {
			throw new IllegalStateException("Tree position was already set for " + this);
		}
		treePosition = new TreePosition(parent);
		treePosition.children.addAll(contentMap.values());
		treePosition.previousSibling = previousSibling;
		
		createTreePositions(this, contentMap);
	}
	
}
