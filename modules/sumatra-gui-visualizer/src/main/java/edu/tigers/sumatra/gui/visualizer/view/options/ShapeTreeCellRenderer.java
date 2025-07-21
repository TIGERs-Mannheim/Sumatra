/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.view.options;

import com.jidesoft.swing.CheckBoxTree;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public class ShapeTreeCellRenderer extends DefaultTreeCellRenderer
{
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (tree instanceof CheckBoxTree checkBoxTree && value instanceof DefaultMutableTreeNode node)
		{
			ESelectionLevel selectionLevel = determineSelectionLevel(checkBoxTree, node);
			switch (selectionLevel)
			{
				case UNSELECTED -> setFont(getFont().deriveFont(Map.of(
						TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR,
						TextAttribute.UNDERLINE, -1
				)));

				case ONLY_CHILD_SELECTED -> setFont(getFont().deriveFont(Map.of(
						TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR,
						TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON
				)));

				case SELF_SELECTED -> setFont(getFont().deriveFont(Map.of(
						TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD,
						TextAttribute.UNDERLINE, -1
				)));
			}
		}
		return this;
	}


	private ESelectionLevel determineSelectionLevel(CheckBoxTree tree, DefaultMutableTreeNode node)
	{
		var myPath = node.getPath();
		var allSelectedPaths = tree.getCheckBoxTreeSelectionModel().getSelectionPaths();
		var selectedLeafs = Arrays.stream(allSelectedPaths)
				.map(TreePath::getPath)
				.filter(p -> p.length > 0)
				.map(p -> p[p.length - 1])
				.collect(Collectors.toUnmodifiableSet());


		if (Arrays.stream(myPath).anyMatch(selectedLeafs::contains))
		{
			return ESelectionLevel.SELF_SELECTED;
		}

		if (Arrays.stream(allSelectedPaths)
				.anyMatch(path -> path != null && Arrays.stream(path.getPath()).anyMatch(obj -> obj == node)))
		{
			return ESelectionLevel.ONLY_CHILD_SELECTED;
		}
		return ESelectionLevel.UNSELECTED;
	}


	private enum ESelectionLevel
	{
		UNSELECTED,
		ONLY_CHILD_SELECTED,
		SELF_SELECTED
	}
}
