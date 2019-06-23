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
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


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
	@Configurable(comment = "If active, the autoref will automatically set the follow up action according to the decisions of the refbox")
	private static boolean	followUpOverride	= false;
	
	private boolean			firstUpdate			= true;
	
	static
	{
		registerClass(KickState.class);
	}
	
	
	@Override
	protected void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		EGameStateNeutral gamestate = frame.getGameState();
		FollowUpAction originalAction = ctx.getFollowUpAction();
		
		IVector2 curBallPos = frame.getWorldFrame().getBall().getPos();
		
		if (firstUpdate && followUpOverride && !gameStateEqualsFollowUpAction(originalAction, gamestate, curBallPos))
		{
			ctx.setFollowUpAction(getActionForState(gamestate, curBallPos));
		}
		
		firstUpdate = false;
	}
	
	
	private boolean gameStateEqualsFollowUpAction(final FollowUpAction action, final EGameStateNeutral gamestate,
			final IVector2 curBallPos)
	{
		if ((action != null) && gamestateEqualsActionType(action.getActionType(), gamestate)
				&& (action.getTeamInFavor() == gamestate.getTeamColor()))
		{
			Optional<IVector2> optActionBallPos = action.getNewBallPosition();
			return optActionBallPos.map(actionBallPos -> ballisPlaced(actionBallPos, curBallPos)).orElse(false);
		}
		return false;
	}
	
	
	private boolean ballisPlaced(final IVector2 a, final IVector2 b)
	{
		return GeoMath.distancePP(a, b) < AutoRefConfig.getBallPlacementAccuracy();
	}
	
	
	private boolean gamestateEqualsActionType(final EActionType type, final EGameStateNeutral state)
	{
		switch (type)
		{
			case DIRECT_FREE:
				return state.isDirectKick();
			case INDIRECT_FREE:
				return state.isIndirectKick();
			case KICK_OFF:
				return state.isKickOff();
			case PENALTY:
				return state.isPenalty();
			default:
				return false;
		}
	}
	
	
	private FollowUpAction getActionForState(final EGameStateNeutral state, final IVector2 ballPos)
	{
		EActionType type = null;
		if (state.isIndirectKick())
		{
			type = EActionType.INDIRECT_FREE;
		} else if (state.isDirectKick())
		{
			type = EActionType.DIRECT_FREE;
		} else if (state.isKickOff())
		{
			type = EActionType.KICK_OFF;
		} else if (state.isPenalty())
		{
			type = EActionType.PENALTY;
		} else
		{
			return null;
		}
		return new FollowUpAction(type, state.getTeamColor(), ballPos);
	}
	
	
	@Override
	public void doReset()
	{
		firstUpdate = true;
	}
	
}
