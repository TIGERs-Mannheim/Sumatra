/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.ai.pandora.roles.offense.EpicPenaltyShooterRole;


/**
 * Handle some roles, if we have a penalty against the others
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PenaltyWePlay extends APenaltyPlay
{
	/**
	 * 
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
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		for (ARole role : getRoles())
		{
			if (role.getType() == ERole.EPIC_PENALTY_SHOOTER)
			{
				return new MoveRole(EMoveBehavior.LOOK_AT_BALL);
			}
		}
		return new EpicPenaltyShooterRole();
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		if (!getRoles().isEmpty())
		{
			List<ARole> moveRoles = new ArrayList<ARole>(getRoles().size());
			for (ARole role : getRoles())
			{
				if (role.getType() != ERole.EPIC_PENALTY_SHOOTER)
				{
					moveRoles.add(role);
				}
			}
			updateMoveRoles(frame, moveRoles, 1, 0);
		}
	}
}
