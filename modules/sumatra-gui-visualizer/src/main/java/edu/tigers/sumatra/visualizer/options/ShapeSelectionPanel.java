/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.options;


import com.jidesoft.swing.CheckBoxTree;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.JPanel;
import java.awt.BorderLayout;


@Log4j2
public class ShapeSelectionPanel extends JPanel
{
	@Getter
	private final CheckBoxTree tree = new CheckBoxTree();


	public ShapeSelectionPanel()
	{
		setLayout(new BorderLayout());
		add(tree, BorderLayout.CENTER);
	}
}
