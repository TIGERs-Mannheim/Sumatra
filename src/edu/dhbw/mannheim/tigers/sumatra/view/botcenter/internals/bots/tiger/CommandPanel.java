/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

import javax.swing.JPanel;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.commons.InstanceablePanel;


/**
 * Generate Bot commands
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class CommandPanel extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long			serialVersionUID	= -8818621974705939633L;
	
	private final InstanceablePanel	instPanel			= new InstanceablePanel(ECommand.values());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public CommandPanel()
	{
		add(instPanel);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param obs
	 */
	public void addObserver(IInstanceableObserver obs)
	{
		instPanel.addObserver(obs);
	}
	
	
	/**
	 * @param obs
	 */
	public void removeObserver(IInstanceableObserver obs)
	{
		instPanel.removeObserver(obs);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
