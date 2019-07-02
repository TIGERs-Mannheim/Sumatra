/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.gameevent.ChippedGoal;
import edu.tigers.sumatra.referee.gameevent.Goal;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.IndirectGoal;
import edu.tigers.sumatra.referee.gameevent.PossibleGoal;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.BallLeftFieldPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TimedPosition;


/**
 * Detect goals, invalid indirect goals and invalid chipped goals.
 */
public class GoalDetector extends AGameEventDetector
{
	private final Logger log = Logger.getLogger(GoalDetector.class.getName());
	
	
	private TimedPosition lastBallLeftFieldPos = null;
	private boolean indirectStillHot = false;
	private BotID indirectFreeKickBot;
	private IKickEvent lastKickEvent;
	private double maxBallHeight = 0.0;
	
	
	public GoalDetector()
	{
		super(EGameEventDetectorType.GOAL, EGameState.RUNNING);
	}
	
	
	@Override
	protected void doPrepare()
	{
		indirectStillHot = false;
		maxBallHeight = 0.0;
		
		/*
		 * Save the position of the kicker in case this RUNNING state was initiated by an INDIRECT freekick.
		 * This will allow the rule to determine if an indirect goal occurred
		 */
		List<GameState> stateHistory = frame.getStateHistory();
		if (stateHistory.size() > 1)
		{
			EGameState lastState = stateHistory.get(1).getState();
			if (lastState == EGameState.INDIRECT_FREE)
			{
				indirectFreeKickBot = frame.getWorldFrame().getBots().values().stream()
						.min(Comparator.comparingDouble(b -> b.getPos().distanceTo(frame.getWorldFrame().getBall().getPos())))
						.map(ITrackedBot::getBotId)
						.orElse(null);
				indirectStillHot = true;
			}
		}
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		updateIndirectDetection();
		updateChipDetection();
		
		return frame.getBallLeftFieldPos()
				.filter(b -> !b.getPosition().similarTo(lastBallLeftFieldPos))
				.map(this::processBallLeftField);
	}
	
	
	@Override
	protected void doReset()
	{
		lastBallLeftFieldPos = null;
	}
	
	
	private IGameEvent processBallLeftField(final BallLeftFieldPosition ballLeftFieldPosition)
	{
		lastBallLeftFieldPos = ballLeftFieldPosition.getPosition();
		
		if (ballLeftFieldPosition.getType() != BallLeftFieldPosition.EBallLeftFieldType.GOAL)
		{
			return null;
		}
		
		final Optional<IKickEvent> kickEvent = frame.getWorldFrame().getKickEvent();
		final IVector2 kickLocation = kickEvent.map(IKickEvent::getPosition).orElse(null);
		final BotID kickingBot = kickEvent.map(IKickEvent::getKickingBot).filter(AObjectID::isBot).orElse(null);
		final IVector2 location = ballLeftFieldPosition.getPosition().getPos();
		
		warnIfNoKickEventPresent();
		
		if (maxBallHeight > RuleConstraints.getMaxRobotHeight() && kickEvent.isPresent())
		{
			// ball was chipped
			return new ChippedGoal(kickingBot, location, kickLocation, maxBallHeight);
		}
		
		if (indirectStillHot && kickEvent.isPresent())
		{
			indirectStillHot = false;
			
			// The ball was kicked from an indirect free kick -> the goal is not valid
			return new IndirectGoal(kickingBot, location, kickLocation);
		}
		return createEvent(ballLeftFieldPosition, kickLocation, kickingBot);
	}
	
	
	private void updateChipDetection()
	{
		final Optional<IKickEvent> kickEvent = frame.getWorldFrame().getKickEvent();
		if (kickEvent.isPresent())
		{
			if (lastKickEvent != null && kickEvent.get().getTimestamp() != lastKickEvent.getTimestamp())
			{
				maxBallHeight = 0.0;
			}
			lastKickEvent = kickEvent.get();
		}
		maxBallHeight = Math.max(maxBallHeight, frame.getWorldFrame().getBall().getHeight());
	}
	
	
	private void warnIfNoKickEventPresent()
	{
		if (!frame.getWorldFrame().getKickEvent().isPresent())
		{
			log.warn("Goal detected, but no kick event found.");
		}
	}
	
	
	private void updateIndirectDetection()
	{
		if (indirectStillHot && touchedByTeamColleague())
		{
			indirectStillHot = false;
		}
	}
	
	
	private boolean touchedByTeamColleague()
	{
		return frame.getBotsLastTouchedBall().stream()
				.filter(b -> b.getBotID().getTeamColor() == indirectFreeKickBot.getTeamColor())
				.anyMatch(b -> !b.getBotID().equals(indirectFreeKickBot));
	}
	
	
	private ETeamColor goalForTeam(final BallLeftFieldPosition ballLeftFieldPosition)
	{
		if (ballLeftFieldPosition.getPosition().getPos().x() < 0)
		{
			// x < 0 -> inside goal of team on the negative side -> goal for the other team
			return Geometry.getNegativeHalfTeam().opposite();
		}
		return Geometry.getNegativeHalfTeam();
	}
	
	
	private IGameEvent createEvent(final BallLeftFieldPosition ballLeftFieldPosition,
			final IVector2 kickLocation, final BotID kickingBot)
	{
		final IVector2 location = ballLeftFieldPosition.getPosition().getPos();
		
		// Return Goal in Sim and PossibleGoal in real use
		if (SumatraModel.getInstance().isSimulation())
		{
			return new Goal(goalForTeam(ballLeftFieldPosition), kickingBot, location, kickLocation);
		}
		return new PossibleGoal(goalForTeam(ballLeftFieldPosition), kickingBot, location, kickLocation);
	}
}
