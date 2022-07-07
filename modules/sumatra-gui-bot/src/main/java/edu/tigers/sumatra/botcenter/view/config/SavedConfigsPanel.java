/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botcenter.view.config;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;


public class SavedConfigsPanel extends JPanel
{
	private final JTabbedPane tabs;


	public SavedConfigsPanel()
	{
		setLayout(new BorderLayout());
		tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);
	}


	public void addTab(final String title, final JPanel panel)
	{
		tabs.add(title, panel);
	}


	public void clearTabs()
	{
		tabs.removeAll();
	}
}
