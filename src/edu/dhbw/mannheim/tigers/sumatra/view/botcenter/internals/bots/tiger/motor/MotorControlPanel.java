/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.10.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;

/**
 * Single motor control panel.
 * 
 * @author AndreR
 * 
 */
public class MotorControlPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -8265670065896398262L;
	
	private MotorConfigurationPanel configPanel = new MotorConfigurationPanel();
	private MotorPidPanel pidPanel = new MotorPidPanel();

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public MotorControlPanel()
	{
		setLayout(new MigLayout("fill", "", ""));
		
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
				new JScrollPane(configPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), pidPanel),
				"grow, push");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public MotorConfigurationPanel getConfigPanel()
	{
		return configPanel;
	}
	
	public MotorPidPanel getPidPanel()
	{
		return pidPanel;
	}	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
