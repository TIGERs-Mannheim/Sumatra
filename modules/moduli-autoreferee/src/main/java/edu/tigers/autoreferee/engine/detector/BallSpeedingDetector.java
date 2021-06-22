/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotKickedBallToFast;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.BallKickFitState;

import java.util.Optional;


/**
 * This rule detects ball speed violations when the game is running.
 */
public class BallSpeedingDetector extends AGameEventDetector
{
	@Configurable(comment = "[m/s] The ball is not considered to be too fast if above this threshold to prevent false positives", defValue = "12.0")
	private static double topSpeedThreshold = 12.0;

	@Configurable(comment = "Max waiting time [s]", defValue = "0.8")
	private static double maxWaitingTime = 0.8;

	@Configurable(comment = "Min waiting time [s]", defValue = "0.1")
	private static double minWaitingTime = 0.1;


	private IKickEvent lastReportedKickEvent;


	public BallSpeedingDetector()
	{
		super(EGameEventDetectorType.BALL_SPEEDING, EGameState.RUNNING);
	}


	@Override
	public Optional<IGameEvent> doUpdate()
	{
		Optional<BallKickFitState> lastKickFitState = frame.getPreviousFrame().getWorldFrame().getKickFitState();
		Optional<IKickEvent> lastKickEvent = frame.getPreviousFrame().getWorldFrame().getKickEvent();
		if (lastKickFitState.isEmpty() || lastKickEvent.isEmpty())
		{
			return Optional.empty();
		}

		Optional<IKickEvent> currentKickEvent = frame.getWorldFrame().getKickEvent();
		if (kickEventHasBeenReported(currentKickEvent.orElse(lastKickEvent.get())))
		{
			return Optional.empty();
		}

		// take the last kickFitState, because if the ball hits another robot, we still want to have the original ball
		// velocity
		double kickSpeed = lastKickFitState.get().getKickVel().getLength();
		if (isKickTooFast(kickSpeed) && kickEstimateIsReady(lastKickFitState.get()))
		{
			lastReportedKickEvent = lastKickEvent.get();
			if (lastReportedKickEvent.getKickingBot().isBot())
			{
				return Optional.of(createViolation(lastReportedKickEvent.getKickingBot(), kickSpeed));
			}
			// could not determine the violator
			return Optional.empty();
		}

		if (kickFinished())
		{
			// reset detection for this kick event
			lastReportedKickEvent = lastKickEvent.get();
		}

		return Optional.empty();
	}


	private boolean kickEventHasBeenReported(final IKickEvent currentKickEvent)
	{
		return lastReportedKickEvent != null && lastReportedKickEvent.getTimestamp() == currentKickEvent.getTimestamp();
	}


	private boolean kickEstimateIsReady(BallKickFitState lastKickFitState)
	{
		double kickFitEstimateAge = (frame.getTimestamp() - lastKickFitState.getKickTimestamp()) / 1e9;
		boolean kickEstimateAged = kickFitEstimateAge > minWaitingTime;
		boolean kickEstimateIsReady = kickFitEstimateAge > maxWaitingTime;

		// either time is up, or ball has left the field, or ball touched another bot
		return kickEstimateAged && (kickEstimateIsReady || kickFinished());
	}


	private boolean kickFinished()
	{
		var kickEvent = frame.getWorldFrame().getKickEvent();
		return kickEvent.isEmpty() || ballIsNotInsideField() || ballTouchedAnotherRobot(kickEvent.get());
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
