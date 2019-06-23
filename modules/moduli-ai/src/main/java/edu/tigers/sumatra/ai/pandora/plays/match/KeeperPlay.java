/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.match;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.PenaltyKeeperRole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperOneOnOneRole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;


/**
 * A OneBot Play to realize a solo keeper behavior.
 */
public class KeeperPlay extends APlay
{
	/**
	 * Constructor
	 */
	public KeeperPlay()
	{
		super(EPlay.KEEPER);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		if (!getRoles().isEmpty())
		{
			throw new IllegalStateException("Keeper Play can not handle more than 1 role!");
		}
		return new KeeperRole();
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		if (getRoles().isEmpty())
		{
			return;
		}
		ARole keeper = getLastRole();
		if (frame.getRefereeMsg().getStage() == Referee.SSL_Referee.Stage.PENALTY_SHOOTOUT)
		{
			if (keeper.getType() != ERole.ONE_ON_ONE_KEEPER)
			{
				switchRoles(keeper, new KeeperOneOnOneRole());
			}
		} else if (frame.getTacticalField().getGameState().isPenaltyOrPreparePenaltyForThem())
		{
			if (keeper.getType() != ERole.PENALTY_KEEPER)
			{
				switchRoles(keeper, new PenaltyKeeperRole());
			}
		} else if (keeper.getType() != ERole.KEEPER)
		{
			switchRoles(keeper, new KeeperRole());
		}
	}
}
