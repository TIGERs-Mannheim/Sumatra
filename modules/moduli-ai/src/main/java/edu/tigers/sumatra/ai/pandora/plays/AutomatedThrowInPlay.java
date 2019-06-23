/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 19, 2013
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.PrimaryPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.SecondaryPlacementRole;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class AutomatedThrowInPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public AutomatedThrowInPlay()
	{
		super(EPlay.AUTOMATED_THROW_IN);
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
		if (getRoles().size() == 1)
		{
			if (getRoles().get(0).getType() == ERole.SECONDARY_AUTOMATED_THROW_IN)
			{
				return new PrimaryPlacementRole();
			} else if (getRoles().get(0).getType() == ERole.PRIMARY_AUTOMATED_THROW_IN)
			{
				return new SecondaryPlacementRole();
			}
		} else if (getRoles().size() == 0)
		{
			return new PrimaryPlacementRole();
		}
		throw new IllegalArgumentException("something went wrong");
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
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
