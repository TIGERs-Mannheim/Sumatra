/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.configurable.Configurable;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter2D;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.PlacementSucceeded;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Detect successful ball placements
 */
public class BallPlacementSucceededDetector extends AGameEventDetector
{
	@Configurable(defValue = "150.0", comment = "Minimum distance [mm] to placement pos to accept the placement")
	private static double ballPlacementTolerance = 150.0;

	@Configurable(defValue = "0.97", comment = "Alpha value for the exponential moving average filter on the ball pos that decides if a ball is still moving")
	private static double ballMovingFilterAlpha = 0.97;

	@Configurable(defValue = "50.0", comment = "Min distance [mm] between ball and bot, before ball placement is considered successful, if next command is a free kick for the placing team")
	private static double minDistanceToBallForOwnFreeKick = 50.0;

	@Configurable(defValue = "500.0", comment = "Min distance [mm] between ball and bot, before ball placement is considered successful, else")
	private static double minDistanceToBallDefault = 500.0;

	@Configurable(defValue = "2.0", comment = "Minimum time [s] that the ball placement must take to allow robots to move to valid positions")
	private static double minBallPlacementDuration = 2.0;


	private long tStart = 0;
	private IVector2 initialBallPos;
	private ExponentialMovingAverageFilter2D ballPosFilter;
	private boolean eventRaised = false;
	private long lastCommandCounter = Integer.MAX_VALUE;


	public BallPlacementSucceededDetector()
	{
		super(EGameEventDetectorType.BALL_PLACEMENT_SUCCEEDED, EGameState.BALL_PLACEMENT);
	}


	@Override
	protected void doPrepare()
	{
		resetState(frame);
		ballPosFilter = new ExponentialMovingAverageFilter2D(ballMovingFilterAlpha, initialBallPos);
	}


	private void resetState(final IAutoRefFrame frame)
	{
		tStart = frame.getTimestamp();
		initialBallPos = frame.getWorldFrame().getBall().getPos();
		eventRaised = false;
		lastCommandCounter = frame.getRefereeMsg().getCmdCounter();
	}


	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		if (frame.getRefereeMsg().getCmdCounter() != lastCommandCounter)
		{
			// still in ball placement state, but with a new command (probably other team)
			resetState(frame);
		}

		if (eventRaised || frame.getGameState().getBallPlacementPositionNeutral() == null)
		{
			return Optional.empty();
		}

		final IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		final ETeamColor forTeam = frame.getGameState().getForTeam();
		final double remainingDistance = frame.getGameState().getBallPlacementPositionNeutral().distanceTo(ballPos);
		final double elapsedTime = (frame.getTimestamp() - tStart) / 1e9;

		ballPosFilter.setAlpha(ballMovingFilterAlpha);
		ballPosFilter.update(ballPos);


		if (remainingDistance <= ballPlacementTolerance
				&& canContinue()
				&& elapsedTime > minBallPlacementDuration)
		{
			eventRaised = true;
			double movedDistance = initialBallPos.distanceTo(ballPos);
			return Optional.of(new PlacementSucceeded(forTeam, elapsedTime, remainingDistance, movedDistance));
		}

		return Optional.empty();
	}


	private boolean canContinue()
	{
		final List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.ENGINE);

		final boolean ballStill = isBallStill();
		Color color = ballStill ? Color.green : Color.red;
		shapes.add(new DrawableCircle(Circle.createCircle(ballPosFilter.getState().getXYVector(), 50), color));

		final List<ITrackedBot> botsViolatingDistanceToBall = botsViolatingDistanceToBall();
		botsViolatingDistanceToBall.forEach(bot -> shapes
				.add(new DrawableCircle(Circle.createCircle(bot.getPos(), Geometry.getBotRadius() + 30), Color.red)));

		return ballStill && botsViolatingDistanceToBall.isEmpty();
	}


	private boolean isBallStill()
	{
		final IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		return ballPosFilter.getState().getXYVector().distanceTo(ballPos) < 5;
	}


	private List<ITrackedBot> botsViolatingDistanceToBall()
	{
		final double minDistance = Geometry.getBotRadius() +
				(isNextCommandForPlacingTeam()
						? minDistanceToBallForOwnFreeKick
						: minDistanceToBallDefault);

		return frame.getWorldFrame().getBots().values().stream()
				.filter(bot -> bot.getPos().distanceTo(frame.getWorldFrame().getBall().getPos()) < minDistance)
				.collect(Collectors.toList());
	}


	private boolean isNextCommandForPlacingTeam()
	{
		if (frame.getRefereeMsg().getCommand() == SslGcRefereeMessage.Referee.Command.BALL_PLACEMENT_BLUE)
		{
			return frame.getRefereeMsg().getNextCommand() == SslGcRefereeMessage.Referee.Command.DIRECT_FREE_BLUE
					|| frame.getRefereeMsg().getNextCommand() == SslGcRefereeMessage.Referee.Command.INDIRECT_FREE_BLUE;
		} else if (frame.getRefereeMsg().getCommand() == SslGcRefereeMessage.Referee.Command.BALL_PLACEMENT_YELLOW)
		{
			return frame.getRefereeMsg().getNextCommand() == SslGcRefereeMessage.Referee.Command.DIRECT_FREE_YELLOW
					|| frame.getRefereeMsg().getNextCommand() == SslGcRefereeMessage.Referee.Command.INDIRECT_FREE_YELLOW;
		}
		return false;
	}
}
