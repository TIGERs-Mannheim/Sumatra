/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;


/**
 * Overview of all bots.
 * 
 * @author AndreR
 */
public class OverviewPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long			serialVersionUID	= -3183090653608159807L;
	private final Map<BotID, JPanel>	botPanels			= new TreeMap<>(BotID.getComparator());
	private boolean						active				= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constructor.
	 */
	public OverviewPanel()
	{
		setLayout(new MigLayout("fill", "", ""));
		
		setActive(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param active
	 */
	public void setActive(final boolean active)
	{
		this.active = active;
		
		updatePanels();
	}
	
	
	/**
	 * @param botID
	 * @param panel
	 */
	public void addBotPanel(final BotID botID, final JPanel panel)
	{
		synchronized (botPanels)
		{
			botPanels.put(botID, panel);
		}
		updatePanels();
	}
	
	
	/**
	 * @param botID
	 */
	public void removeBotPanel(final BotID botID)
	{
		JPanel panel = botPanels.get(botID);
		if (panel != null)
		{
			panel.setEnabled(false);
		}
		
		updatePanels();
	}
	
	
	/**
	 * Remove all bot panels.
	 */
	public void removeAllBotPanels()
	{
		synchronized (botPanels)
		{
			botPanels.clear();
		}
		updatePanels();
	}
	
	
	/**
	 * @param botId
	 * @return
	 */
	public JPanel getBotPanel(final BotID botId)
	{
		return botPanels.get(botId);
	}
	
	
	private void updatePanels()
	{
		final JPanel thisPanel = this;
		
		SwingUtilities.invokeLater(() -> {
			removeAll();
			
			synchronized (botPanels)
			{
				if (active && !botPanels.isEmpty())
				{
					botPanels.values().stream()
							.filter(JPanel::isEnabled)
							.forEach(p -> add(p, "wrap, gapbottom 0"));
				} else
				{
					add(new JLabel("No bots connected."), "wrap");
				}
			}
			
			add(Box.createGlue(), "push");
			SwingUtilities.updateComponentTreeUI(thisPanel);
		});
	}
}
