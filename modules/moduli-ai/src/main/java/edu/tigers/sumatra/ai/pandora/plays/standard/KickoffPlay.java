/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.ArrayList;
import java.util.List;


/**
 * The kick-off play is active during our own kick-off.
 */
public class KickoffPlay extends APlay
{
	public KickoffPlay()
	{
		super(EPlay.KICKOFF);
	}


	@Override
	protected ARole onAddRole()
	{
		return new MoveRole();
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		List<IVector2> movePos = new ArrayList<>(
				getAiFrame().getTacticalField().getKickoffStrategy().getBestMovementPositions());
		for (MoveRole role : findRoles(MoveRole.class))
		{
			if (!movePos.isEmpty())
			{
				role.updateDestination(movePos.remove(0));
				role.updateLookAtTarget(getBall());
			}
		}
	}
}
