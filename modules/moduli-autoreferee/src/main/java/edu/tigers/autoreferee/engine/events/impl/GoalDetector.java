/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc.PossibleGoal;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * @author "Lukas Magel"
 */
public class GoalDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	
	private BotPosition indirectKickPos = null;
	private PossibleGoal lastGoal = null;
	private boolean indirectStillHot = false;
	private boolean goalDetected = false;
	
	
	/**
	 * Create new instance
	 */
	public GoalDetector()
	{
		super(EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		/*
		 * Save the position of the kicker in case this RUNNING state was initiated by an INDIRECT freekick.
		 * This will allow the rule to determine if an indirect goal occured
		 */
		List<GameState> stateHistory = frame.getStateHistory();
		if (stateHistory.size() > 1)
		{
			EGameState lastState = stateHistory.get(1).getState();
			if (lastState == EGameState.INDIRECT_FREE)
			{
				indirectKickPos = frame.getBotLastTouchedBall();
				indirectStillHot = true;
			}
		}
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		if ((indirectKickPos != null) && indirectStillHot)
		{
			BotID kickerId = indirectKickPos.getBotID();
			BotPosition lastKickPos = frame.getBotLastTouchedBall();
			if (!kickerId.equals(lastKickPos.getBotID()))
			{
				indirectStillHot = false;
			}
		}
		
		Optional<PossibleGoal> optGoalShot = frame.getPossibleGoal();
		if (optGoalShot.isPresent() && !optGoalShot.get().equals(lastGoal))
		{
			PossibleGoal goalShot = optGoalShot.get();
			lastGoal = goalShot;
			IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
			
			if (!goalDetected)
			{
				if ((indirectKickPos != null) && indirectStillHot)
				{
					// The ball was kicked from an indirect freekick -> the goal is not valid
					BotID kicker = indirectKickPos.getBotID();
					ETeamColor kickerColor = kicker.getTeamColor();
					
					indirectStillHot = false;
					goalDetected = true;
					IVector2 kickPos = getKickPos(goalShot.getGoalColor(), kickerColor, ballPos);
					
					FollowUpAction followUp = new FollowUpAction(EActionType.DIRECT_FREE, kickerColor.opposite(), kickPos);
					GameEvent violation = new GameEvent(EGameEvent.INDIRECT_GOAL, frame.getTimestamp(), kicker,
							followUp);
					return Optional.of(violation);
				}
				
				goalDetected = true;
				ETeamColor goalColor = goalShot.getGoalColor();
				FollowUpAction followUp = new FollowUpAction(EActionType.KICK_OFF, goalColor, Geometry.getCenter());
				return Optional.of(new GameEvent(EGameEvent.GOAL, frame.getTimestamp(), goalColor.opposite(), followUp));
			}
		} else
		{
			goalDetected = false;
		}
		return Optional.empty();
	}
	
	
	private IVector2 getKickPos(final ETeamColor goalColor, final ETeamColor kickerColor, final IVector2 ballPos)
	{
		if (goalColor == kickerColor)
		{
			// The ball entered the goal of the kicker --> Corner Kick
			return AutoRefMath.getClosestCornerKickPos(ballPos);
		}
		// The ball entered the goal of the other team --> Goal Kick
		return AutoRefMath.getClosestGoalKickPos(ballPos);
	}
	
	
	@Override
	protected void doReset()
	{
		indirectKickPos = null;
		indirectStillHot = false;
		goalDetected = false;
	}
}
