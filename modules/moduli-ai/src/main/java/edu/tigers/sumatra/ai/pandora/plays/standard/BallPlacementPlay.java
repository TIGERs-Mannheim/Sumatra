/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.standard;

import java.util.List;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.ABallPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.PrimaryBallPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.SecondaryBallPlacementRole;


/**
 * Play that handles automated ball placement
 */
public class BallPlacementPlay extends APlay
{
	public BallPlacementPlay()
	{
		super(EPlay.BALL_PLACEMENT);
	}


	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}


	@Override
	protected ARole onAddRole()
	{
		return new PrimaryBallPlacementRole();
	}


	@Override
	public void updateBeforeRoles(final AthenaAiFrame frame)
	{
		super.updateBeforeRoles(frame);
		if (getRoles().size() > 1 && getRoles().stream().noneMatch(r -> r instanceof SecondaryBallPlacementRole))
		{
			List<ARole> roles = getRoles();
			final int secondaryRoleIndex;
			if (frame.getTacticalField().isInsaneKeeper()
					&& roles.stream().anyMatch(r -> r.getBotID() == getAiFrame().getKeeperId()))
			{
				if (roles.get(1).getBotID() == getAiFrame().getKeeperId())
				{
					secondaryRoleIndex = 0;
				} else
				{
					secondaryRoleIndex = 1;
				}
			} else
			{
				boolean firstRoleCloserToBall = getWorldFrame().getBot(roles.get(0).getBotID()).getPos()
						.distanceTo(getAiFrame().getGamestate().getBallPlacementPositionForUs()) < getWorldFrame()
								.getBot(roles.get(1).getBotID()).getPos()
								.distanceTo(getAiFrame().getGamestate().getBallPlacementPositionForUs());
				secondaryRoleIndex = firstRoleCloserToBall ? 1 : 0;
			}
			switchRoles(roles.get(secondaryRoleIndex), new SecondaryBallPlacementRole());
		}
		getRoles().stream().map(r -> (ABallPlacementRole) r).forEach(r -> r.setHasCompanion(getRoles().size() > 1));
	}
}
