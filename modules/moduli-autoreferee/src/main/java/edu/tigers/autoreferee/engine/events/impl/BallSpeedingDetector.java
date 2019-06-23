/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.Collections;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.SpeedViolation;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.BallKickFitState;


/**
 * This rule detects ball speed violations when the game is running.
 * 
 * @author "Lukas Magel"
 */
public class BallSpeedingDetector extends AGameEventDetector
{
	private static final int PRIORITY = 2;
	@Configurable(comment = "[m/s] The ball is not considered to be too fast if above this threshold to prevent false positives", defValue = "12.0")
	private static double topSpeedThreshold = 12.0d;
	
	@Configurable(comment = "Max waiting time [s]", defValue = "0.8")
	private static double maxWaitingTime = 0.8;
	
	private IKickEvent lastReportedKickEvent;
	
	static
	{
		AGameEventDetector.registerClass(BallSpeedingDetector.class);
	}
	
	
	/**
	 * Create new instance
	 */
	public BallSpeedingDetector()
	{
		super(EGameEventDetectorType.BALL_SPEEDING, EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame)
	{
		if (!frame.getPreviousFrame().getWorldFrame().getKickFitState().isPresent()
				|| !frame.getWorldFrame().getKickEvent().isPresent())
		{
			return Optional.empty();
		}
		IKickEvent currentKickEvent = frame.getWorldFrame().getKickEvent().get();
		
		// take the last kickFitState, because if the ball hits another robot, we still want to have the original ball
		// velocity
		BallKickFitState lastKickFitState = frame.getPreviousFrame().getWorldFrame().getKickFitState().get();
		double kickSpeed = lastKickFitState.getKickVel().getLength() / 1000.;
		if (isKickTooFast(kickSpeed)
				&& kickEventHasNotBeenReportedYet(currentKickEvent)
				&& kickEstimateIsReady(frame, currentKickEvent))
		{
			lastReportedKickEvent = currentKickEvent;
			SpeedViolation violation;
			if (lastKickFitState.getKickPos().distanceTo(currentKickEvent.getPosition()) < 1000)
			{
				violation = createViolation(currentKickEvent.getKickingBot(), kickSpeed,
						frame.getTimestamp());
			} else if (frame.getBotsLastTouchedBall().size() == 1)
			{
				violation = createViolation(frame.getBotsLastTouchedBall().get(0).getBotID(), kickSpeed,
						frame.getTimestamp());
			} else
			{
				// could not determine the violator
				return Optional.empty();
			}
			return Optional.of(violation);
		}
		
		return Optional.empty();
	}
	
	
	private boolean kickEventHasNotBeenReportedYet(final IKickEvent currentKickEvent)
	{
		return lastReportedKickEvent == null || lastReportedKickEvent.getTimestamp() != currentKickEvent.getTimestamp();
	}
	
	
	private boolean kickEstimateIsReady(final IAutoRefFrame frame, final IKickEvent currentKickEvent)
	{
		boolean kickEstimateIsReady = (frame.getTimestamp() - currentKickEvent.getTimestamp()) / 1e9 > maxWaitingTime;
		
		// either time is up, or ball has left the field, or ball touched another bot
		return kickEstimateIsReady || ballIsNotInsideField(frame) || ballTouchedAnotherRobot(frame, currentKickEvent);
	}
	
	
	private boolean isKickTooFast(double kickSpeed)
	{
		return kickSpeed > RuleConstraints.getMaxBallSpeed() + 0.01
				&& kickSpeed < topSpeedThreshold;
	}
	
	
	private boolean ballIsNotInsideField(IAutoRefFrame frame)
	{
		return !frame.isBallInsideField() && !frame.getPossibleGoal().isPresent();
	}
	
	
	private boolean ballTouchedAnotherRobot(IAutoRefFrame frame, final IKickEvent currentKickEvent)
	{
		return frame.getBotsLastTouchedBall().stream()
				.noneMatch(b -> b.getBotID().equals(currentKickEvent.getKickingBot()));
	}
	
	
	private SpeedViolation createViolation(BotID violator, double lastSpeedEstimate, long timestamp)
	{
		IVector2 kickPos = AutoRefMath.getClosestFreekickPos(lastReportedKickEvent.getPosition(),
				violator.getTeamColor().opposite());
		
		FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, violator.getTeamColor().opposite(),
				kickPos);
		
		return new SpeedViolation(EGameEvent.BALL_SPEED, timestamp,
				violator, action, lastSpeedEstimate, Collections.emptyList());
	}
	
	
	@Override
	public void reset()
	{
		lastReportedKickEvent = null;
	}
}
