/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.basestation.BaseStationPanel;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * New v2015 botcenter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotCenterPanel extends JPanel implements ISumatraView
{
	/**  */
	private static final long					serialVersionUID	= -7749317503520671162L;
	
	private final OverviewPanel				botOverview;
	private final BotConfigOverviewPanel	botOverviewPanel;
	private final List<BaseStationPanel>	baseStationPanels	= new ArrayList<>();
	private final BcFeaturesPanel				featurePanel;
	private final JTabbedPane					tabbedPane;
	
	
	/**
	 * Constructor.
	 */
	public BotCenterPanel()
	{
		setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane();
		botOverview = new OverviewPanel();
		botOverviewPanel = new BotConfigOverviewPanel();
		featurePanel = new BcFeaturesPanel();
	}
	
	
	/**
	 * @param panel
	 */
	public void addBaseStationTab(final BaseStationPanel panel)
	{
		baseStationPanels.add(panel);
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
	 * @return the baseStationPanel
	 */
	public List<BaseStationPanel> getBaseStationPanels()
	{
		return baseStationPanels;
	}
	
	
	/**
	 * @return the botSummary
	 */
	public final OverviewPanel getOverviewPanel()
	{
		return botOverview;
	}
	
	
	/**
	 * @return the featurePanel
	 */
	public final BcFeaturesPanel getFeaturePanel()
	{
		return featurePanel;
	}
}
