/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.dribbler;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorPidPanel;


/**
 * Dribbler motor control panel.
 * 
 * @author AndreR
 * 
 */
public class DribblerControlPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long						serialVersionUID	= -8265670065896398262L;
	
	private final DribblerConfigurationPanel	configPanel			= new DribblerConfigurationPanel();
	private final MotorPidPanel					pidPanel				= new MotorPidPanel();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public DribblerControlPanel()
	{
		setLayout(new MigLayout("fill", "", ""));
		
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(configPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), pidPanel),
				"grow, push");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public DribblerConfigurationPanel getConfigPanel()
	{
		return configPanel;
	}
	
	
	/**
	 * @return
	 */
	public MotorPidPanel getPidPanel()
	{
		return pidPanel;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
