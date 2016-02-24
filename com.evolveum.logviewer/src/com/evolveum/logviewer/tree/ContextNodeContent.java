package com.evolveum.logviewer.tree;

import java.util.ArrayList;
import java.util.List;

import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.Parser;

public class ContextNodeContent extends OutlineNodeContent {
	
	private String execWave, projWave;
	private String labelCore;
	private String labelSuffix;
	
	public String getLabelCore() {
		return labelCore;
	}

	public void setLabelCore(String labelCore) {
		this.labelCore = labelCore;
	}

	public String getLabelSuffix() {
		return labelSuffix;
	}

	public void setLabelSuffix(String labelSuffix) {
		this.labelSuffix = labelSuffix;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TreeNode createTreeNode(Parser parser) {
		List<TreeNode> children = new ArrayList<>();
		
		OutlineNode<ContextNodeContent> owner = (OutlineNode<ContextNodeContent>) getOwner();
		
		List<OutlineNode<ProjectionContextNodeContent>> projectionContexts = owner.getAllChildren(ProjectionContextNodeContent.class);
		for (OutlineNode<ProjectionContextNodeContent> projectionContext : projectionContexts) {
			children.add(projectionContext.createTreeNode(parser));
		}

		int mappings = 0, executions = 0;
		for (OutlineNode<?> item : owner.getAllChildren()) {
			if (item.getContent() == null) {
				continue; // shouldn't occur
			}
			if (item.getContent() instanceof ProjectionContextNodeContent) {
				continue; // already processed
			}
			
			if (item.getContent() instanceof MappingNodeContent) {
				mappings++;
			} else if (item.getContent() instanceof ExecutionNodeContent) {
				executions++;
			} else {
				System.err.println("Problem - neither mapping nor execution: " + item);
			}

			children.add(item.getContent().createTreeNode(parser));
		}
		
		//children.addAll(parser.scriptsAndExpressions);	// shouldn't be any
		
		String label = "Wave: " + execWave + " : " + projWave + 
				" - P:" + projectionContexts.size() + ", M:" + mappings + 
				//(executions > 0 ? ", EXEC: " + executions : "") +
				(executions > 0 ? " # " : " - ") + 
				labelCore + labelSuffix;

		OutlineNode<ContextNodeContent> first = getFirstDump();
		OutlineNode<ContextNodeContent> previous = getPreviousDump();
		if (owner.getDate() != null && (previous != null || first != null)) {
			owner.setDelta(getDelta(previous));
			owner.setSum(getDelta(first));
		}
		
		TreeNode treeNode = new TreeNode(owner, label, owner.getRegion());
		treeNode.addChildren(children);
		return treeNode;
	}

	private String getDelta(OutlineNode<ContextNodeContent> reference) {
		if (reference == null || reference.getDate() == null) {
			return "-";
		}
		return String.valueOf(owner.getDate().getTime() - reference.getDate().getTime());
	}
	
	@SuppressWarnings("unchecked")
	private OutlineNode<ContextNodeContent> getPreviousDump() {
		if (owner.getPreviousSibling() != null && owner.getPreviousSibling().getContent() instanceof ContextNodeContent) {
			return (OutlineNode<ContextNodeContent>) owner.getPreviousSibling();
		} else {
			return null;
		}
	}

	private OutlineNode<ContextNodeContent> getFirstDump() {
		OutlineNode<ContextNodeContent> prev = getPreviousDump();		
		if (prev == null) {
			return null;
		}
		OutlineNode<ContextNodeContent> last = prev;
		while (last.getContent().getPreviousDump() != null) {
			last = last.getContent().getPreviousDump();
		}
		return last;
	}

	// input like this: LensContext: state=SECONDARY, Wave(e=1,p=1,max=0), focus, 2 projections, 2 changes, fresh=true
	public void parseWaveInfo(String line) {
		projWave = "?";
		execWave = "?";
		
		int i = line.indexOf("Wave(e=");
		if (i < 0) {
			return;
		}
		
		int j = line.indexOf(",p=", i);
		if (j < 0) {
			return;
		}
		execWave = line.substring(i+7, j);
		
		int k = line.indexOf(",max", j);
		if (k < 0) {
			return;
		}
		projWave = line.substring(j+3, k);
	}

	// from: ---[ PROJECTOR (INITIAL) context after load ]--------------------------------
	// to: PROJECTOR (INITIAL) context after load 
	public void parseLabelCore(String line) {
		int i = line.indexOf(" ]---");
		if (i > 0) {
			line = line.substring(5, i);
		} else {
			line = line.substring(5);
		}
		setLabelCore(line);
	}
}
