/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots;

import javax.swing.JPanel;

import com.github.g3force.instanceables.IInstanceableObserver;
import com.github.g3force.instanceables.InstanceablePanel;

import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Generate Bot commands
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CommandPanel extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long			serialVersionUID	= -8818621974705939633L;
	
	private final InstanceablePanel	instPanel			= new InstanceablePanel(ECommand.values(), SumatraModel
																				.getInstance().getUserSettings());
	
	
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
	public void addObserver(final IInstanceableObserver obs)
	{
		instPanel.addObserver(obs);
	}
	
	
	/**
	 * @param obs
	 */
	public void removeObserver(final IInstanceableObserver obs)
	{
		instPanel.removeObserver(obs);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
