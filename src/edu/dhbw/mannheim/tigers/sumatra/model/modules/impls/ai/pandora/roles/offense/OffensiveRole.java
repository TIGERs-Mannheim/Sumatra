/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.states.OffensiveRoleStopState;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRole extends OffensiveRoleStopState
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public OffensiveRole()
	{
		super();
		
		BallGettingState getter = new BallGettingState();
		KickState kicker = new KickState();
		StopState stop = new StopState();
		
		setInitialState(getter);
		
		addTransition(EStateId.GET, EEvent.BALL_CONTROL_OBTAINED, kicker);
		addTransition(EStateId.KICK, EEvent.LOST_BALL, getter);
		addTransition(EStateId.STOP, EEvent.GAME_STARTED, getter);
		addTransition(EStateId.GET, EEvent.STOP, stop);
		addTransition(EStateId.KICK, EEvent.STOP, stop);
		addTransition(EStateId.STOP, EEvent.NORMALSTART, getter);
	}
	
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	@Override
	public void onGameStateChanged(final EGameState gameState)
	{
		setNormalStart(false);
		switch (gameState)
		{
			case CORNER_KICK_THEY:
			case TIMEOUT_THEY:
			case TIMEOUT_WE:
			case DIRECT_KICK_THEY:
			case GOAL_KICK_THEY:
			case HALTED:
			case PREPARE_PENALTY_THEY:
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
			case PREPARE_PENALTY_WE:
			case STOPPED:
			case THROW_IN_THEY:
				nextState(EEvent.STOP);
				break;
			case CORNER_KICK_WE:
			case DIRECT_KICK_WE:
			case GOAL_KICK_WE:
			case THROW_IN_WE:
				nextState(EEvent.GAME_STARTED);
				break;
			default:
				break;
		}
	}
	
}
