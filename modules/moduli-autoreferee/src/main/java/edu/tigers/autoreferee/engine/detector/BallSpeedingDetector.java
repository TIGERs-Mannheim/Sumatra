/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotKickedBallTooFast;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.KickedBall;

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


	private KickedBall lastReportedKickEvent;


	public BallSpeedingDetector()
	{
		super(EGameEventDetectorType.BALL_SPEEDING, EGameState.RUNNING);
	}


	@Override
	public Optional<IGameEvent> doUpdate()
	{
		var lastKickedBall = frame.getPreviousFrame().getWorldFrame().getKickedBall();
		if (lastKickedBall.isEmpty())
		{
			return Optional.empty();
		}

		var currentKickedBall = frame.getWorldFrame().getKickedBall();
		if (kickEventHasBeenReported(currentKickedBall.orElse(lastKickedBall.get())))
		{
			return Optional.empty();
		}

		// take the last kickFitState, because if the ball hits another robot, we still want to have the original ball
		// velocity
		double kickSpeed = lastKickedBall.get().getKickVel().getLength();
		if (isKickTooFast(kickSpeed) && kickEstimateIsReady(lastKickedBall.get()))
		{
			lastReportedKickEvent = lastKickedBall.get();
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
			lastReportedKickEvent = lastKickedBall.get();
		}

		return Optional.empty();
	}


	private boolean kickEventHasBeenReported(final KickedBall currentKickEvent)
	{
		return lastReportedKickEvent != null && lastReportedKickEvent.getTimestamp() == currentKickEvent.getTimestamp();
	}


	private boolean kickEstimateIsReady(KickedBall lastKickedBall)
	{
		double kickFitEstimateAge = (frame.getTimestamp() - lastKickedBall.getKickTimestamp()) / 1e9;
		boolean kickEstimateAged = kickFitEstimateAge > minWaitingTime;
		boolean kickEstimateIsReady = kickFitEstimateAge > maxWaitingTime;

		// either time is up, or ball has left the field, or ball touched another bot
		return kickEstimateAged && (kickEstimateIsReady || kickFinished());
	}


	private boolean kickFinished()
	{
		var kickEvent = frame.getWorldFrame().getKickedBall();
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


	private boolean ballTouchedAnotherRobot(final KickedBall currentKickEvent)
	{
		return frame.getBotsLastTouchedBall().stream()
				.noneMatch(b -> b.getBotID().equals(currentKickEvent.getKickingBot()));
	}


	private IGameEvent createViolation(BotID violator, double lastSpeedEstimate)
	{
		BotKickedBallTooFast.EKickType kickType = frame.getPreviousFrame().getWorldFrame().getKickedBall()
				.map(s -> s.getKickVel().z() > 0).orElse(false)
				? BotKickedBallTooFast.EKickType.CHIPPED
				: BotKickedBallTooFast.EKickType.STRAIGHT;

		return new BotKickedBallTooFast(violator, lastReportedKickEvent.getPosition(), lastSpeedEstimate, kickType);
	}


	@Override
	public void doReset()
	{
		lastReportedKickEvent = null;
	}
}
