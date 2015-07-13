package com.evolveum.logviewer.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;

public class ContextDumpItem extends DocumentItem {
	
	List<TreeNode> projectionContexts = new ArrayList<>();
	
	String execWave, projWave;
	String labelCore;
	String labelSuffix;
	
	public ContextDumpItem(IRegion region, int startLine) {
		super(region, startLine);
	}

	public TreeNode createTreeNode(Parser parser) {
		List<TreeNode> children = new ArrayList<>();
		
		children.addAll(projectionContexts);

		int mappings = 0, executions = 0;
		for (DocumentItem item : parser.mappingsAndExecutions) {
			if (item instanceof MappingItem) {
				mappings++;
			} else if (item instanceof ExecutionItem) {
				executions++;
			} else {
				System.err.println("Problem - neither mapping nor execution: " + item);
			}
			
			if (item.treeNode != null) {
				children.add(item.treeNode);
			} else {
				System.err.println("Problem - mapping/execution without treeNode: " + item);
			}
		}
		
		children.addAll(parser.scriptsAndExpressions);	// shouldn't be any
		parser.mappingsAndExecutions.clear();
		parser.scriptsAndExpressions.clear();
		
		String label = "Wave: " + execWave + " : " + projWave + 
				" - P:" + projectionContexts.size() + ", M:" + mappings + 
				//(executions > 0 ? ", EXEC: " + executions : "") +
				(executions > 0 ? " # " : " - ") + 
				labelCore + labelSuffix;
		
		treeNode = new TreeNode(label, region);
		treeNode.addChildren(children);
		return treeNode;
	}

	public void addProjectionContextTreeNode(TreeNode node) {
		projectionContexts.add(node);
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
}
