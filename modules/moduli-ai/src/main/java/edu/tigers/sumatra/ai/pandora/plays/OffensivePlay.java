/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 19, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;


/**
 * The offensive play handles only one role, the OffensiveRole
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>,
 *         Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensivePlay extends APlay
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public OffensivePlay()
	{
		super(EPlay.OFFENSIVE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		OffensiveRole newRole;
		newRole = new OffensiveRole();
		return newRole;
	}
	
	
	@Override
	public void updateBeforeRoles(final AthenaAiFrame frame)
	{
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
		
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		frame.getPrevFrame().getAICom().setSpecialMoveCounter(0);
		frame.getAICom().setSpecialMoveCounter(0);
		frame.getAICom().setUnassignedStateCounter(0);
		frame.getPrevFrame().getAICom().setUnassignedStateCounter(0);
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
