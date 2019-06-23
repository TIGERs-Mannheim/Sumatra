/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc.PossibleGoal;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Detect goals and invalid indirect goals
 */
public class GoalDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	
	@Configurable(comment = "Continue the game with a kick off after goals", defValue = "false")
	private static boolean continueGameAfterGoal = false;
	
	static
	{
		AGameEventDetector.registerClass(GoalDetector.class);
	}
	
	/**
	 * we have to remember the last goal, because in passive mode, a goal may be detected by the autoRef, by game
	 * may not be stopped by the human ref -> in this case, we still want to detect new goals.
	 */
	private PossibleGoal lastGoal = null;
	private BotID attackerId = null;
	private boolean indirectStillHot = false;
	private boolean goalDetected = false;
	
	
	/**
	 * Create new instance
	 */
	public GoalDetector()
	{
		super(EGameEventDetectorType.GOAL, EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		goalDetected = false;
		indirectStillHot = false;
		attackerId = null;
		
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
				attackerId = frame.getWorldFrame().getBots().values().stream()
						.min(Comparator.comparingDouble(b -> b.getPos().distanceTo(frame.getWorldFrame().getBall().getPos())))
						.map(ITrackedBot::getBotId)
						.orElse(null);
				indirectStillHot = true;
			}
		}
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate(final IAutoRefFrame frame)
	{
		if (indirectStillHot && frame.getBotsLastTouchedBall().stream().noneMatch(b -> b.getBotID().equals(attackerId)))
		{
			indirectStillHot = false;
		}
		
		Optional<PossibleGoal> optGoalShot = frame.getPossibleGoal();
		if (optGoalShot.isPresent() && !optGoalShot.get().equals(lastGoal))
		{
			PossibleGoal goalShot = optGoalShot.get();
			lastGoal = goalShot;
			IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
			
			if (!goalDetected)
			{
				goalDetected = true;
				if (indirectStillHot)
				{
					indirectStillHot = false;
					
					// The ball was kicked from an indirect freekick -> the goal is not valid
					GameEvent violation = createIndirectGoalViolation(frame, goalShot, ballPos);
					return Optional.of(violation);
				}
				
				ETeamColor goalColor = goalShot.getGoalColor();
				
				FollowUpAction followUp = continueGame()
						? new FollowUpAction(EActionType.KICK_OFF, goalColor, Geometry.getCenter())
						: null;
				return Optional.of(new GameEvent(EGameEvent.GOAL, frame.getTimestamp(), goalColor.opposite(), followUp));
			}
		} else
		{
			goalDetected = false;
		}
		return Optional.empty();
	}
	
	
	private GameEvent createIndirectGoalViolation(final IAutoRefFrame frame, final PossibleGoal goalShot,
			final IVector2 ballPos)
	{
		ETeamColor kickerColor = attackerId.getTeamColor();
		IVector2 kickPos = getKickPos(goalShot.getGoalColor(), kickerColor, ballPos);
		
		FollowUpAction followUp = new FollowUpAction(EActionType.DIRECT_FREE, kickerColor.opposite(), kickPos);
		return new GameEvent(EGameEvent.INDIRECT_GOAL, frame.getTimestamp(), attackerId,
				followUp);
	}
	
	
	private boolean continueGame()
	{
		return "SUMATRA".equals(SumatraModel.getInstance().getEnvironment()) || continueGameAfterGoal;
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
}
