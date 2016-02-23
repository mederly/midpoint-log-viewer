package com.evolveum.logviewer.tree;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

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

	final private OutlineLevelDefinition<C> levelDefinition;
	
	private OutlineNode<?> firstChild, parent;
	private OutlineNode<?> nextSibling, previousSibling;
	
	// position
	IRegion region;
	Integer startLine = -1, endLine = -1;
	
	// other data
	private Date date;
	private C content;
	
	public OutlineNode(OutlineLevelDefinition<C> levelDefinition) {
		this.levelDefinition = levelDefinition;
	}
	
	public OutlineNode(OutlineLevelDefinition<C> outlineInstruction, C content) {
		this(outlineInstruction);
		this.content = content;
		content.setOwner(this);
	}
	
	public OutlineNode(OutlineLevelDefinition<C> outlineInstruction, IRegion region, int startLine) {
		this(outlineInstruction);
		this.region = region;
		this.startLine = startLine;
	}
	
	public OutlineNode(OutlineLevelDefinition<C> outlineInstruction, IRegion region, int startLine, IDocument document) {
		this(outlineInstruction, region, startLine);
		if (startLine > 1) {
			String previousLine = DocumentUtils.getLine(document, startLine-1);
			date = ParsingUtils.parseDate(previousLine);
		}
	}

	public OutlineLevelDefinition<C> getLevelDefinition() {
		return levelDefinition;
	}
	
//	public String getLabel() {
//		if (content != null) {
//			return content.getLabel();
//		} else {
//			return "<no label>";
//		}
//	}

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

	public void parseLine(int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		MatchResult<C> result;
		try {
			result = levelDefinition.matches(this, lineNumber, line, region, document);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return;
		}
		if (result != null) {
			nextSibling = result.getNewDocumentItem();
			nextSibling.previousSibling = this;
			nextSibling.parent = this.parent;
		} else {
			OutlineNode<?> lastChild;
			if (firstChild == null) {
				OutlineLevelDefinition<? extends OutlineNodeContent> nextInstruction = levelDefinition.getNextLevelDefinition();
				if (nextInstruction == null) {
					return;
				}
				firstChild = new OutlineNode<>(nextInstruction);
				firstChild.parent = this;
				lastChild = firstChild;
			} else {
				lastChild = getLastChild();
			}
			lastChild.parseLine(lineNumber, line, region, document);
		}
	}
	
	private OutlineNode<?> getLastChild() {
		return firstChild != null ? firstChild.getLastSibling() : null; 
	}

	private OutlineNode<?> getLastSibling() {
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
	
	public void setCoordinates(IRegion region, int startLineNumber, String line, IDocument document) {
		this.region = region;
		this.startLine = startLineNumber;
		if (startLineNumber > 1) {
			String previousLine = DocumentUtils.getLine(document, startLineNumber-1);
			date = ParsingUtils.parseDate(previousLine);
		}
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
			if (child.getContent() != null) {
				if (contentClasses.length == 0) {
					rv.add((OutlineNode<?>) child);					
				} else {
					for (Class<? extends OutlineNodeContent> contentClass : contentClasses) {
						if (contentClass.isAssignableFrom(child.getContent().getClass())) {
							rv.add((OutlineNode<?>) child);
							break;
						}
					}
				}
			}
			child = child.nextSibling;
		}
		return rv;
	}

	private TreeNode cachedTreeNode;			// TODO remove this
	
	public TreeNode createTreeNode(Parser parser) {
		if (cachedTreeNode != null) {
			return cachedTreeNode;
		}
		if (content != null) {
			cachedTreeNode = content.createTreeNode(parser);
		} else {
			TreeNode treeNode = new TreeNode("<none>", 0, 1);		// ????
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
		for (int i = 0; i < levelDefinition.getLevel(); i++) {
			System.out.print("  ");
		}
		TreeNode treeNode = createTreeNode(parser);
		String label = treeNode != null ? treeNode.getLabel() : "(no tree node)";
		System.out.println("L:" + label + "; C:" + content);
		if (firstChild != null) {
			firstChild.dumpAll(parser);
		}
		if (nextSibling != null) {
			nextSibling.dumpAll(parser);
		}
	}
	
	
}
