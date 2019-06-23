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

import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

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
	private static final long	serialVersionUID	= -3183090653608159807L;
	private ArrayList<JPanel> botPanels = new ArrayList<JPanel>();
	private boolean active = false;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public OverviewPanel()
	{
		setLayout(new MigLayout("fill", "", ""));
		
		setActive(false);
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setActive(boolean active)
	{
		this.active = active;
		
		updatePanels();
	}
	
	public void addBotPanel(JPanel panel)
	{
		botPanels.add(panel);
		
		updatePanels();
	}
	
	public void removeBotPanel(JPanel panel)
	{
		botPanels.remove(panel);
		
		updatePanels();
	}
	
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
			public void run()
			{
				removeAll();

				if(active)
				{
					for(JPanel panel : botPanels)
					{
						add(panel, "wrap, gapbottom 0");
					}
				}
				else
				{
					add(new JLabel("Botcenter unavailable - botmanager stopped"), "wrap");
				}
				
				add(Box.createGlue(), "push");
								
//				Graphics g = getGraphics();
//				if(g != null)
//				{
//					update(g);
//				}
				
				SwingUtilities.updateComponentTreeUI(panel);
			}
		});
	}
}
