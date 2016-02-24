package com.evolveum.logviewer.tree;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.MatchResult;
import com.evolveum.logviewer.parsing.Parser;
import com.evolveum.logviewer.parsing.ParsingUtils;

/**
 *
 * Any relevant log file section. See subclasses.
 * 
 * @author mederly
 */

public class OutlineNode<C extends OutlineNodeContent> {

	final EditorConfiguration editorConfiguration;
	final private int level;
	private OutlineNode<?> firstChild, parent;
	private OutlineNode<?> nextSibling, previousSibling;
	
	// position
	IRegion region;
	Integer startLine = -1, endLine = -1;
	
	private String delta, sum;
	
	// other data
	private Date date;
	
	private C content;
	private OutlineLevelDefinition<C> levelDefinition;

	public OutlineNode(EditorConfiguration editorConfiguration, int level) {
		this.editorConfiguration = editorConfiguration;
		this.level = level;
	}
	
	public OutlineNode(OutlineLevelDefinition<C> levelDefinition, C content, IRegion region, int startLineNumber, String line, IDocument document) {
		this(levelDefinition.getEditorConfiguration(), levelDefinition.getLevel());
		this.levelDefinition = levelDefinition;
		if (content != null) {
			this.content = content;
			content.setOwner(this);
		}
		setCoordinates(region, startLineNumber, line, document);
	}
	
	public OutlineLevelDefinition<C> getLevelDefinition() {
		return levelDefinition;
	}
	
	public C getContent() {
		return content;
	}
	
	public IRegion getRegion() {
		return region;
	}

	public Integer getStartLine() {
		return startLine;
	}

	public Integer getEndLine() {
		return endLine;
	}

	public Date getDate() {
		return date;
	}

	public boolean parseLine(int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		
		for (OutlineLevelDefinition<? extends OutlineNodeContent> currentLevelDefinition : editorConfiguration.getOutlineLevelDefinitions(level)) {
			MatchResult<C> result;
			try {
				result = (MatchResult<C>) currentLevelDefinition.matches((OutlineNode) this, lineNumber, line, region, document);
			} catch (RuntimeException e) {
				e.printStackTrace();
				continue;
			}
			if (result != null) {
				result.addNodesIntoChain(this);
				return true;
			}			
		}
		
		OutlineNode<?> lastChild;
		if (firstChild == null) {
			Integer nextLevel = editorConfiguration.getNextOutlineLevel(level);
			if (nextLevel == null) {
				return false;
			}
			firstChild = new OutlineNode(editorConfiguration, nextLevel);
			firstChild.setCoordinates(region, lineNumber, line, document);
			firstChild.parent = this;
			lastChild = firstChild;
		} else {
			lastChild = getLastChild();
		}
		lastChild.parseLine(lineNumber, line, region, document);
		return false;
	}
	
	private OutlineNode<?> getLastChild() {
		return firstChild != null ? firstChild.getLastSibling() : null; 
	}

	public OutlineNode<?> getLastSibling() {
		OutlineNode<?> sibling = this;
		while (sibling.nextSibling != null) {
			sibling = sibling.nextSibling;
		}
		return sibling;
	}

	public OutlineNode<? extends OutlineNodeContent> getFirstChild() {
		return firstChild;
	}
	public OutlineNode<?> getNextSibling() {
		return nextSibling;
	}
	public OutlineNode<?> getPreviousSibling() {
		return previousSibling;
	}
	public void setNextSibling(OutlineNode<?> nextSibling) {
		this.nextSibling = nextSibling;
	}
	public void setPreviousSibling(OutlineNode<?> previousSibling) {
		this.previousSibling = previousSibling;
	}
	public OutlineNode<?> getParent() {
		return parent;
	}
	public void setParent(OutlineNode<?> parent) {
		this.parent = parent;
	}

	public void setCoordinates(IRegion region, int startLineNumber, String line, IDocument document) {
		if (region == null) {
			System.err.println("No region in setCoordinates!");
		}
		this.region = region;
		this.startLine = startLineNumber;
		
		final String dateLine;
		if (ParsingUtils.isLogEntryStart(line)) {
			dateLine = line;
		} else if (startLineNumber > 1) {
			dateLine = DocumentUtils.getLine(document, startLineNumber-1);
		} else {
			dateLine = null;
		}
		date = ParsingUtils.parseDate(dateLine);
	}

	@SuppressWarnings("unchecked")
	public <T extends OutlineNodeContent> List<OutlineNode<T>> getAllChildren(Class<T> contentClass) {
		List<OutlineNode<T>> rv = new ArrayList<>();
		OutlineNode<?> child = firstChild;
		while (child != null) {
			if (child.getContent() != null) {
				if (contentClass.isAssignableFrom(child.getContent().getClass())) {
					rv.add((OutlineNode<T>) child);
				}
			}
			child = child.nextSibling;
		}
		return rv;
	}
	
	//@SuppressWarnings("unchecked")
	public List<OutlineNode<?>> getAllChildren(Class<? extends OutlineNodeContent>... contentClasses) {
		List<OutlineNode<?>> rv = new ArrayList<>();
		OutlineNode<?> child = firstChild;
		while (child != null) {
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
			child = child.nextSibling;
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
		OutlineNode<?> child = firstChild;
		while (child != null) {
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
			child = child.nextSibling;
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

	public void setContent(C content) {
		this.content = content;
		content.setOwner(this);
	}

	public void dumpAll(Parser parser) {
		for (int i = 0; i < level; i++) {
			System.out.print("  ");
		}
		TreeNode treeNode = createTreeNode(parser);
		String label = treeNode != null ? treeNode.getLabel() : "(no tree node)";
		System.out.println("Label: " + label + "; Content: " + content);
		if (firstChild != null) {
			firstChild.dumpAll(parser);
		}
		if (nextSibling != null) {
			nextSibling.dumpAll(parser);
		}
	}
	
	
	
	@Override
	public String toString() {
		return "OutlineNode [level=" + level + ", content=" + content + ", levelDefinition="
				+ levelDefinition + ", startLine=" + startLine + ", date=" + date + "]";
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
	
}
