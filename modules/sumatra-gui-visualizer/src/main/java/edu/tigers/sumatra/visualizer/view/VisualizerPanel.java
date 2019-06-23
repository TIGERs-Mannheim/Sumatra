/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer.view;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.visualizer.view.field.FieldPanel;
import edu.tigers.sumatra.visualizer.view.field.IFieldPanel;
import net.miginfocom.swing.MigLayout;


/**
 * Visualizes the current game situation.
 * It also allows the user to set a robot at a determined position.
 * 
 * @author BernhardP, OliverS
 */
public class VisualizerPanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long				serialVersionUID	= 2686191777355388548L;
	
	private final FieldPanel				fieldPanel;
	private final RobotsPanel				robotsPanel;
	private final VisualizerOptionsMenu	menuBar;
	private final JPanel						panel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Default
	 */
	public VisualizerPanel()
	{
		setLayout(new BorderLayout());
		menuBar = new VisualizerOptionsMenu();
		panel = new JPanel();
		add(menuBar, BorderLayout.PAGE_START);
		add(panel, BorderLayout.CENTER);
		
		// --- set layout ---
		panel.setLayout(new MigLayout("fill, inset 0", "[min!][max][right]", "[top]"));
		
		// --- init panels ---
		robotsPanel = new RobotsPanel();
		fieldPanel = new FieldPanel();
		
		panel.add(robotsPanel);
		panel.add(fieldPanel, "grow, top");
	}
	
	
	/**
	 * Remove the robots panel
	 */
	public void removeRobotsPanel()
	{
		remove(panel);
		add(fieldPanel);
	}
	
	// --------------------------------------------------------------------------
	// --- ISumatraView ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public IFieldPanel getFieldPanel()
	{
		return fieldPanel;
	}
	
	
	/**
	 * @return
	 */
	public RobotsPanel getRobotsPanel()
	{
		return robotsPanel;
	}
	
	
	/**
	 * @return
	 */
	public VisualizerOptionsMenu getOptionsMenu()
	{
		return menuBar;
	}
}
