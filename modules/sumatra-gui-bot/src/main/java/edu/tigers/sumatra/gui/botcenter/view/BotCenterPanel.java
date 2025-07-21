/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.botcenter.view;

import edu.tigers.sumatra.gui.botcenter.view.basestation.BaseStationPanel;
import edu.tigers.sumatra.gui.botcenter.view.config.BotConfigOverviewPanel;
import edu.tigers.sumatra.gui.botcenter.view.config.SavedConfigsPanel;
import lombok.Getter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.Serial;


/**
 * New bot center.
 */
public class BotCenterPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -7749317503520671162L;

	@Getter
	private final BotCenterOverviewPanel overviewPanel = new BotCenterOverviewPanel();
	@Getter
	private final BotConfigOverviewPanel botOverviewPanel = new BotConfigOverviewPanel();
	@Getter
	private final SavedConfigsPanel savedConfigsPanel = new SavedConfigsPanel();
	private final JTabbedPane tabbedPane = new JTabbedPane();


	public BotCenterPanel()
	{
		setLayout(new BorderLayout());
	}


	/**
	 * @param panel
	 */
	public void addBaseStationTab(final BaseStationPanel panel)
	{
		tabbedPane.addTab(panel.getName(), setupScrollPane(panel));
	}


	/**
	 * Do create panel.
	 */
	public void createPanel()
	{
		tabbedPane.addTab("Bot Overview", setupScrollPane(overviewPanel));
		tabbedPane.addTab("Bot Details", botOverviewPanel);
		tabbedPane.addTab("Saved Bot Configs", savedConfigsPanel);

		add(tabbedPane, BorderLayout.CENTER);
	}


	/**
	 * Clear panel.
	 */
	public void clearPanel()
	{
		remove(tabbedPane);
		tabbedPane.removeAll();
	}


	private Component setupScrollPane(final Component comp)
	{
		JScrollPane scrollPane = new JScrollPane(comp);
		scrollPane.setPreferredSize(new Dimension(0, 0));
		return scrollPane;
	}
}
