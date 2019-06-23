/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 19, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.EpicPenaltyShooterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.OffensiveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.states.OffensiveRoleStopState;


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
	
	private OffensiveRole				offensiveRole	= null;
	private EpicPenaltyShooterRole	penaltyRole		= null;
	private boolean						penaltyActive	= false;
	
	
	/**
	 */
	public OffensivePlay()
	{
		super(EPlay.OFFENSIVE);
		offensiveRole = new OffensiveRole();
		penaltyRole = new EpicPenaltyShooterRole();
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected ARole onRemoveRole()
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		offensiveRole = new OffensiveRole();
		return offensiveRole;
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
		switch (gameState)
		{
			case PREPARE_PENALTY_WE:
				if (!penaltyActive)
				{
					penaltyRole = new EpicPenaltyShooterRole();
					penaltyActive = true;
					switchRoles(offensiveRole, penaltyRole);
				}
				break;
			default:
				if (penaltyActive)
				{
					penaltyActive = false;
					switchRoles(penaltyRole, offensiveRole);
				}
				break;
		}
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		if (frame.getNewRefereeMsg() != null)
		{
			if (frame.getNewRefereeMsg().getCommand().equals(Command.NORMAL_START))
			{
				OffensiveRoleStopState.normalStartCalled();
			}
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
