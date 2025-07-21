/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.visualizer.view;

import lombok.Getter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;


public class RobotScrollPanel extends JPanel
{
	private static final int SCROLL_INCREMENT = 5;
	@Getter
	private RobotsPanel robotsPanel;


	public RobotScrollPanel()
	{
		robotsPanel = new RobotsPanel();
		setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(robotsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
		scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
		add(scrollPane, BorderLayout.CENTER);
	}
}
