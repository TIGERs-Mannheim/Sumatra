/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotKickedBallToFast;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.BallKickFitState;


/**
 * This rule detects ball speed violations when the game is running.
 */
public class BallSpeedingDetector extends AGameEventDetector
{
	@Configurable(comment = "[m/s] The ball is not considered to be too fast if above this threshold to prevent false positives", defValue = "12.0")
	private static double topSpeedThreshold = 12.0;
	
	@Configurable(comment = "Max waiting time [s]", defValue = "0.8")
	private static double maxWaitingTime = 0.8;
	
	
	private IKickEvent lastReportedKickEvent;
	
	
	public BallSpeedingDetector()
	{
		super(EGameEventDetectorType.BALL_SPEEDING, EGameState.RUNNING);
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		if (!frame.getPreviousFrame().getWorldFrame().getKickFitState().isPresent()
				|| !frame.getWorldFrame().getKickEvent().isPresent())
		{
			return Optional.empty();
		}
		
		IKickEvent currentKickEvent = frame.getWorldFrame().getKickEvent().get();
		
		if (kickEventHasBeenReported(currentKickEvent))
		{
			return Optional.empty();
		}
		
		// take the last kickFitState, because if the ball hits another robot, we still want to have the original ball
		// velocity
		BallKickFitState lastKickFitState = frame.getPreviousFrame().getWorldFrame().getKickFitState().get();
		double kickSpeed = lastKickFitState.getKickVel().getLength() / 1000.;
		if (isKickTooFast(kickSpeed)
				&& kickEstimateIsReady(currentKickEvent))
		{
			lastReportedKickEvent = currentKickEvent;
			IGameEvent violation;
			if (lastKickFitState.getKickPos().distanceTo(currentKickEvent.getPosition()) < 1000)
			{
				violation = createViolation(currentKickEvent.getKickingBot(), kickSpeed);
			} else if (frame.getBotsLastTouchedBall().size() == 1)
			{
				violation = createViolation(frame.getBotsLastTouchedBall().get(0).getBotID(), kickSpeed);
			} else
			{
				// could not determine the violator
				return Optional.empty();
			}
			return Optional.of(violation);
		}
		
		if (ballTouchedAnotherRobot(currentKickEvent))
		{
			// reset detection for this kick event
			lastReportedKickEvent = currentKickEvent;
		}
		
		return Optional.empty();
	}
	
	
	private boolean kickEventHasBeenReported(final IKickEvent currentKickEvent)
	{
		return lastReportedKickEvent != null && lastReportedKickEvent.getTimestamp() == currentKickEvent.getTimestamp();
	}
	
	
	private boolean kickEstimateIsReady(final IKickEvent currentKickEvent)
	{
		boolean kickEstimateIsReady = (frame.getTimestamp() - currentKickEvent.getTimestamp()) / 1e9 > maxWaitingTime;
		
		// either time is up, or ball has left the field, or ball touched another bot
		return kickEstimateIsReady || ballIsNotInsideField() || ballTouchedAnotherRobot(currentKickEvent);
	}
	
	
	private boolean isKickTooFast(double kickSpeed)
	{
		return kickSpeed > RuleConstraints.getMaxBallSpeed() + 0.01
				&& kickSpeed < topSpeedThreshold;
	}
	
	
	private boolean ballIsNotInsideField()
	{
		return !frame.isBallInsideField();
	}
	
	
	private boolean ballTouchedAnotherRobot(final IKickEvent currentKickEvent)
	{
		return frame.getBotsLastTouchedBall().stream()
				.noneMatch(b -> b.getBotID().equals(currentKickEvent.getKickingBot()));
	}
	
	
	private IGameEvent createViolation(BotID violator, double lastSpeedEstimate)
	{
		BotKickedBallToFast.EKickType kickType = frame.getPreviousFrame().getWorldFrame().getKickFitState()
				.map(s -> s.getKickVel().z() > 0).orElse(false)
						? BotKickedBallToFast.EKickType.CHIPPED
						: BotKickedBallToFast.EKickType.STRAIGHT;
		
		return new BotKickedBallToFast(violator, lastReportedKickEvent.getPosition(), lastSpeedEstimate, kickType);
	}
	
	
	@Override
	public void doReset()
	{
		lastReportedKickEvent = null;
	}
}
