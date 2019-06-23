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

import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


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
	private static final long								serialVersionUID	= 4990265633846449163L;
	
	private static final String							TITLE					= "Full bot overview";
	
	
	private final Map<BotID, BotOverviewPanel>	botOverviews		= new TreeMap<BotID, BotOverviewPanel>();
	
	private JLabel												statuslabel			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BotFullOverviewPanel()
	{
		setLayout(new MigLayout("fill"));
		setBorder(BorderFactory.createTitledBorder(TITLE));
		
		statuslabel = new JLabel("AICenter unavailable - start sumatra");
		
		add(statuslabel);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param botId
	 * @return
	 */
	public BotOverviewPanel getBotPanel(BotID botId)
	{
		synchronized (botOverviews)
		{
			return botOverviews.get(botId);
		}
	}
	
	
	/** */
	public void clearBotViews()
	{
		synchronized (botOverviews)
		{
			for (final BotOverviewPanel panel : botOverviews.values())
			{
				panel.clearView();
			}
		}
	}
	
	
	/**
	 * @param botId
	 * @param name
	 */
	public void addBotPanel(BotID botId, String name)
	{
		
		final BotOverviewPanel botPanel = new BotOverviewPanel(botId, name);
		
		synchronized (botOverviews)
		{
			botOverviews.put(botId, botPanel);
		}
		
		updatePanels();
	}
	
	
	/**
	 * @param botId
	 */
	public void removeBotPanel(BotID botId)
	{
		synchronized (botOverviews)
		{
			botOverviews.remove(botId);
		}
		
		updatePanels();
	}
	
	
	/** */
	public void removeAllBotPanels()
	{
		removeAll();
		
		synchronized (botOverviews)
		{
			botOverviews.clear();
		}
		
		add(statuslabel);
	}
	
	
	/**
	 * Reorder all panels
	 */
	public void updatePanels()
	{
		removeAll();
		if (botOverviews.isEmpty())
		{
			add(statuslabel);
		} else
		{
			remove(statuslabel);
		}
		synchronized (botOverviews)
		{
			for (JPanel panel : botOverviews.values())
			{
				add(panel);
			}
		}
		revalidate();
	}
}
