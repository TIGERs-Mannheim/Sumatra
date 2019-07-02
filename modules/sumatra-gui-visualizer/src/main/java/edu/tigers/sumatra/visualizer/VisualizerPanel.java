/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * Visualizes the current game situation.
 * It also allows the user to set a robot at a determined position.
 * 
 * @author BernhardP, OliverS
 */
public class VisualizerPanel extends JPanel implements ISumatraView
{
	private static final long serialVersionUID = 2686191777355388548L;
	
	private final FieldPanel fieldPanel;
	private final RobotsPanel robotsPanel;
	private final VisualizerOptionsMenu menuBar;
	private final JPanel panel;
	
	
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
		panel.setLayout(new MigLayout("fill, inset 0, gap 0", "[min!][max][right]", "[top]"));
		
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
