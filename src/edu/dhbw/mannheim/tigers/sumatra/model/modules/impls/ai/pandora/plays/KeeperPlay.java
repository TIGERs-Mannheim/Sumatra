/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.10.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standard.PenaltyKeeperRoleV2;


/**
 * A OneBot Play to realize a solo keeper behavior.<br>
 * Requires: 1 {@link PenaltyKeeperRoleV2}
 * 
 * @author Malte
 */
public class KeeperPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private ARole			keeper	= null;
	
	private EGameState	gameState;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KeeperPlay()
	{
		super(EPlay.KEEPER);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected ARole onRemoveRole()
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
	protected ARole onAddRole()
	{
		if (!getRoles().isEmpty())
		{
			throw new IllegalStateException("Keeper Play can not handle more than 1 role!");
		}
		if (keeper == null)
		{
			if (gameState == EGameState.PREPARE_KICKOFF_THEY)
			{
				keeper = new PenaltyKeeperRoleV2();
			} else
			{
				keeper = new KeeperRole();
			}
		}
		return (keeper);
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
		ARole oldKeeper = keeper;
		switch (gameState)
		{
			case RUNNING:
				if (oldKeeper != null)
				{
					keeper = new KeeperRole();
					switchRoles(oldKeeper, keeper);
				}
				break;
			case PREPARE_PENALTY_THEY:
				if (oldKeeper != null)
				{
					keeper = new PenaltyKeeperRoleV2();
					switchRoles(oldKeeper, keeper);
				}
				break;
			case STOPPED:
				if (keeper instanceof KeeperRole)
				{
					if (gameState == EGameState.STOPPED)
					{
						((KeeperRole) keeper).setAllowChipkick(false);
					} else
					{
						((KeeperRole) keeper).setAllowChipkick(true);
					}
				}
			default:
				return;
		}
		
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
