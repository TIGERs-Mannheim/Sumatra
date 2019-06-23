/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 19, 2013
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.KickoffShooterRole;


/**
 * The offensive play handles only one role, the OffensiveRole
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class KickoffPlay extends APlay
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public KickoffPlay()
	{
		super(EPlay.KICKOFF);
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
		for (ARole role : getRoles())
		{
			if (role.getType() != ERole.KICKOFF_SHOOTER)
			{
				return role;
			}
		}
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		for (ARole role : getRoles())
		{
			if (role.getType() == ERole.KICKOFF_SHOOTER)
			{
				return new MoveRole(EMoveBehavior.LOOK_AT_BALL);
			}
		}
		return new KickoffShooterRole();
	}
	
	
	@Override
	public void updateBeforeRoles(final AthenaAiFrame frame)
	{
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
		
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		int counter = 0;
		for (ARole role : getRoles())
		{
			if (role instanceof MoveRole)
			{
				if (counter == 0)
				{
					((MoveRole) role).getMoveCon().updateDestination(new Vector2(-300, 2000));
					counter++;
				}
				else if (counter == 1)
				{
					((MoveRole) role).getMoveCon().updateDestination(new Vector2(-300, -2000));
					counter++;
				}
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
