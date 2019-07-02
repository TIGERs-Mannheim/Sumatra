/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botcenter.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import edu.tigers.sumatra.botcenter.view.basestation.BaseStationPanel;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * New v2015 botcenter
 */
public class BotCenterPanel extends JPanel implements ISumatraView
{
	private static final long serialVersionUID = -7749317503520671162L;

	private final BotCenterOverviewPanel botOverview;
	private final BotConfigOverviewPanel botOverviewPanel;
	private final JTabbedPane tabbedPane;


	public BotCenterPanel()
	{
		setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane();
		botOverview = new BotCenterOverviewPanel();
		botOverviewPanel = new BotConfigOverviewPanel();
	}


	/**
	 * @param panel
	 */
	public void addBaseStationTab(final BaseStationPanel panel)
	{
		tabbedPane.addTab(panel.getName(), setupScrollPane(panel));
		repaint();
	}


	/** Do create panel. */
	public void createPanel()
	{
		tabbedPane.addTab("Bot Overview", setupScrollPane(botOverview));
		tabbedPane.addTab("Bot Details", botOverviewPanel);

		add(tabbedPane, BorderLayout.CENTER);
		repaint();
	}


	/** Clear panel. */
	public void clearPanel()
	{
		remove(tabbedPane);
		tabbedPane.removeAll();
		repaint();
	}


	private Component setupScrollPane(final Component comp)
	{
		JScrollPane scrollPane = new JScrollPane(comp);
		scrollPane.setPreferredSize(new Dimension(0, 0));
		return scrollPane;
	}


	@Override
	public List<JMenu> getCustomMenus()
	{
		return Collections.emptyList();
	}


	/**
	 * @return the botOverviewPanel
	 */
	public BotConfigOverviewPanel getBotOverviewPanel()
	{
		return botOverviewPanel;
	}


	/**
	 * @return the botSummary
	 */
	public final BotCenterOverviewPanel getOverviewPanel()
	{
		return botOverview;
	}
}
