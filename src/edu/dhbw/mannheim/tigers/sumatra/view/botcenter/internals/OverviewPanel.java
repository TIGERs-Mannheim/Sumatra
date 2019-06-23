/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Overview of all bots.
 * 
 * @author AndreR
 * 
 */
public class OverviewPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long			serialVersionUID	= -3183090653608159807L;
	private final Map<BotID, JPanel>	botPanels			= new TreeMap<BotID, JPanel>();
	private boolean						active				= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
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
	public void setActive(boolean active)
	{
		this.active = active;
		
		updatePanels();
	}
	
	
	/**
	 * @param botID
	 * @param panel
	 */
	public void addBotPanel(BotID botID, JPanel panel)
	{
		botPanels.put(botID, panel);
		
		updatePanels();
	}
	
	
	/**
	 * @param botID
	 */
	public void removeBotPanel(BotID botID)
	{
		botPanels.remove(botID);
		
		updatePanels();
	}
	
	
	/**
	 */
	public void removeAllBotPanels()
	{
		botPanels.clear();
		
		updatePanels();
	}
	
	
	private void updatePanels()
	{
		final JPanel panel = this;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				removeAll();
				
				if (active)
				{
					for (final JPanel panel : botPanels.values())
					{
						add(panel, "wrap, gapbottom 0");
					}
				} else
				{
					add(new JLabel("Botcenter unavailable - botmanager stopped"), "wrap");
				}
				
				add(Box.createGlue(), "push");
				
				SwingUtilities.updateComponentTreeUI(panel);
			}
		});
	}
}
