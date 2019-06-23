/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.botoverview;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;


/**
 * This panel gives an full overview of all bots.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class BotFullOverviewPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long							serialVersionUID	= 4990265633846449163L;
	
	private static final String						TITLE					= "Full bot overview";
	

	private final Map<Integer, BotOverviewPanel>	botOverviews		= new HashMap<Integer, BotOverviewPanel>();
	
	private JLabel											statuslabel			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public BotFullOverviewPanel()
	{
		setLayout(new MigLayout("fill"));
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE), TITLE));
		
		statuslabel = new JLabel("AICenter unavailable - start sumatra");
		
		add(statuslabel);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public BotOverviewPanel getBotPanel(int botId)
	{
		synchronized (botOverviews)
		{
			return botOverviews.get(botId);
		}
	}
	

	public void clearBotViews()
	{
		synchronized (botOverviews)
		{
			for (Integer botId : botOverviews.keySet())
			{
				getBotPanel(botId).clearView();
			}
		}
	}
	

	public void addBotPanel(int botId, String name)
	{
		remove(statuslabel);
		
		BotOverviewPanel botPanel = new BotOverviewPanel(botId, name);
		botPanel.setBotId(botId);
		add(botPanel);
		
		synchronized (botOverviews)
		{
			botOverviews.put(botId, botPanel);
		}
		
		revalidate();
	}
	

	public void removeBotPanel(int botId)
	{
		remove(botOverviews.get(botId));
		
		synchronized (botOverviews)
		{
			botOverviews.remove(botId);
		}
		
		revalidate();
	}
	

	public void removeAllBotPanels()
	{
		removeAll();
		
		synchronized (botOverviews)
		{
			botOverviews.clear();
		}
		
		add(statuslabel);
		
		revalidate();
	}
}
