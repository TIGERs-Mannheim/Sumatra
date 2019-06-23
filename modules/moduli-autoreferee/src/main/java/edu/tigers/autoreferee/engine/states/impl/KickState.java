/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 15, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.states.impl;

import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * This autoref state compares the current state of the game with the follow up action of the autoreferee. If they do
 * not match the follow up action is reset to match the current state of the game.
 * This should enhance the flow of the game since the autoref does not store outdated information that has been
 * overriden by the human referee.
 * 
 * @author "Lukas Magel"
 */
public class KickState extends AbstractAutoRefState
{
	@Configurable(comment = "If active, the autoref will automatically set the follow up action according to the decisions of the refbox", defValue = "false")
	private static boolean	followUpOverride	= false;
	
	private boolean			firstUpdate			= true;
	
	static
	{
		registerClass(KickState.class);
	}
	
	
	@Override
	protected void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		GameState gamestate = frame.getGameState();
		FollowUpAction originalAction = ctx.getFollowUpAction();
		
		IVector2 curBallPos = frame.getWorldFrame().getBall().getPos();
		
		if (firstUpdate && followUpOverride && !gameStateEqualsFollowUpAction(originalAction, gamestate, curBallPos))
		{
			ctx.setFollowUpAction(getActionForState(gamestate, curBallPos));
		}
		
		firstUpdate = false;
	}
	
	
	private boolean gameStateEqualsFollowUpAction(final FollowUpAction action, final GameState gamestate,
			final IVector2 curBallPos)
	{
		if ((action != null) && gamestateEqualsActionType(action.getActionType(), gamestate)
				&& (action.getTeamInFavor() == gamestate.getForTeam()))
		{
			Optional<IVector2> optActionBallPos = action.getNewBallPosition();
			return optActionBallPos.map(actionBallPos -> ballisPlaced(actionBallPos, curBallPos)).orElse(false);
		}
		return false;
	}
	
	
	private boolean ballisPlaced(final IVector2 a, final IVector2 b)
	{
		return VectorMath.distancePP(a, b) < AutoRefConfig.getBallPlacementAccuracy();
	}
	
	
	private boolean gamestateEqualsActionType(final EActionType type, final GameState gameState)
	{
		EGameState state = gameState.getState();
		
		switch (type)
		{
			case DIRECT_FREE:
				return state == EGameState.DIRECT_FREE;
			case INDIRECT_FREE:
				return state == EGameState.INDIRECT_FREE;
			case KICK_OFF:
				return state == EGameState.KICKOFF;
			case PENALTY:
				return state == EGameState.PENALTY;
			default:
				return false;
		}
	}
	
	
	private FollowUpAction getActionForState(final GameState gameState, final IVector2 ballPos)
	{
		EActionType type;
		EGameState state = gameState.getState();
		
		switch (state)
		{
			case INDIRECT_FREE:
				type = EActionType.INDIRECT_FREE;
				break;
			case DIRECT_FREE:
				type = EActionType.DIRECT_FREE;
				break;
			case KICKOFF:
				type = EActionType.KICK_OFF;
				break;
			case PENALTY:
				type = EActionType.PENALTY;
				break;
			default:
				return null;
		}
		
		return new FollowUpAction(type, gameState.getForTeam(), ballPos);
	}
	
	
	@Override
	public void doReset()
	{
		firstUpdate = true;
	}
	
}
