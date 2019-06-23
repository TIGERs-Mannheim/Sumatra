/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.PenaltyKeeperRole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperOneOnOneRole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * A OneBot Play to realize a solo keeper behavior.<br>
 * Requires: 1 {@link PenaltyKeeperRole}
 *
 * @author Malte
 */
public class KeeperPlay extends APlay
{
	
	private ARole keeper = null;
	
	
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
		ARole role = keeper;
		keeper = null;
		return role;
	}
	
	
	@Override
	protected void onRoleRemoved(final ARole role)
	{
		keeper = null;
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		if (!getRoles().isEmpty())
		{
			throw new IllegalStateException("Keeper Play can not handle more than 1 role!");
		}
		if (frame.getRefereeMsg().getStage() == Referee.SSL_Referee.Stage.PENALTY_SHOOTOUT)
		{
			keeper = new KeeperOneOnOneRole();
		} else
		{
			if (frame.getTacticalField().getGameState().isPrepareKickoffForThem())
			{
				keeper = new PenaltyKeeperRole();
			} else
			{
				keeper = new KeeperRole();
			}
		}
		return keeper;
	}
	
	
	@Override
	protected void onGameStateChanged(final GameState gameState)
	{
		if (getRoles().isEmpty())
		{
			return;
		}
		if (gameState.isPenaltyOrPreparePenaltyForThem())
		{
			keeper = new PenaltyKeeperRole();
			switchRoles(getRoles().get(0), keeper);
		} else
		{
			if (keeper != null && keeper.getType() != ERole.KEEPER)
			{
				keeper = new KeeperRole();
				switchRoles(getRoles().get(0), keeper);
			}
		}
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		// Only one bot -> play is not needed
	}
}
