/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): BernhardP
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.FieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.OptionsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.RobotsPanel;


/**
 * Visualizes the current game situation.
 * It also allows the user to set a robot at a determinated position.
 * 
 * @author BernhardP
 * 
 */
public class VisualizerPanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 2686191777355388548L;
	
	private FieldPanel			fieldPanel			= null;
	

	private JTextField			teamColor			= null;
	private RobotsPanel			robotsPanel			= null;
	private OptionsPanel			optionsPanel		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public VisualizerPanel()
	{
		// --- set layout ---
		setLayout(new MigLayout("fill, inset 0", "[70!][455!][150!]", "[100]"));
		
		// --- left panel ---
		JPanel leftPanel = new JPanel(new MigLayout("fill, inset 0", "[70!]", "[40!]30[]"));
		
		
		// team color panel
		teamColor = new JTextField();
		teamColor.setEditable(false);
		teamColor.setBackground(Color.WHITE);
		JPanel teamColorPanel = new JPanel(new MigLayout("fill", "[fill]", "[fill]"));
		
		TitledBorder border = BorderFactory.createTitledBorder("Team:");
		border.setTitleJustification(TitledBorder.CENTER);
		teamColorPanel.setBorder(border);
		teamColorPanel.add(teamColor);
		
		
		// --- init panels ---
		robotsPanel = new RobotsPanel();
		fieldPanel = new FieldPanel();
		optionsPanel = new OptionsPanel();
		
		// --- set panels ---
		leftPanel.add(teamColorPanel, "grow, top, wrap");
		leftPanel.add(robotsPanel, "grow, top");
		add(leftPanel, "growY");
		add(fieldPanel, "grow, top");
		add(optionsPanel, "growY, top");
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public int getID()
	{
		return 4;
	}
	

	@Override
	public String getTitle()
	{
		return "Visualizer";
	}
	

	@Override
	public Component getViewComponent()
	{
		return this;
	}
	

	@Override
	public List<JMenu> getCustomMenus()
	{
		/*
		 * List<JMenu> menus = new ArrayList<JMenu>();
		 * JMenu editBots = new JMenu("Bots bearbeiten");
		 * JMenuItem addBot = new JMenuItem("hinzuf�gen");
		 * JMenuItem removeBot = new JMenuItem("entfernen");
		 * editBots.add(addBot);
		 * editBots.add(removeBot);
		 * JMenu botConfig = new JMenu("Botkonfiguration");
		 * JMenuItem saveConfig = new JMenuItem("speichern");
		 * JMenuItem loadConfig = new JMenuItem("laden");
		 * botConfig.add(saveConfig);
		 * botConfig.add(loadConfig);
		 * 
		 * menus.add(editBots);
		 * menus.add(botConfig);
		 * 
		 * return menus;
		 */
		return null;
	}
	

	@Override
	public void onShown()
	{
	}
	

	@Override
	public void onHidden()
	{
	}
	

	@Override
	public void onFocused()
	{
	}
	

	@Override
	public void onFocusLost()
	{
	}
	

	public FieldPanel getFieldPanel()
	{
		return fieldPanel;
	}
	

	public RobotsPanel getRobotsPanel()
	{
		return robotsPanel;
	}
	

	public OptionsPanel getOptionsPanel()
	{
		return optionsPanel;
	}
	

	public void setTigersAreYellow(boolean yellow)
	{
		if (yellow)
		{
			teamColor.setText("YELLOW");
			teamColor.setBackground(Color.YELLOW);
		} else
		{
			teamColor.setText("BLUE");
			teamColor.setBackground(Color.BLUE);
		}
		
		// Sub-panels
		getRobotsPanel().setTigersAreYellow(yellow);
		getFieldPanel().setTigersAreYellow(yellow);
	}
}
