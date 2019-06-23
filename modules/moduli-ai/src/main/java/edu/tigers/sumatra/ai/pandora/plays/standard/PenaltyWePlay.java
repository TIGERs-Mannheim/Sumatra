/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.EpicPenaltyShooterRole;


/**
 * Handle some roles, if we have a penalty against the others
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PenaltyWePlay extends APenaltyPlay
{
	/**
	 * Default
	 */
	public PenaltyWePlay()
	{
		super(EPlay.PENALTY_WE);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		for (ARole role : getRoles())
		{
			if (role.getType() != ERole.EPIC_PENALTY_SHOOTER)
			{
				return role;
			}
		}
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		for (ARole role : getRoles())
		{
			if (role.getType() == ERole.EPIC_PENALTY_SHOOTER)
			{
				return new MoveRole();
			}
		}
		return new EpicPenaltyShooterRole();
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		if (!getRoles().isEmpty())
		{
			updateMoveRoles(frame, 1, 0);
		}
	}
}
