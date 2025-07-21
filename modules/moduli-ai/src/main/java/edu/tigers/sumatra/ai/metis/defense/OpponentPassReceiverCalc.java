/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Determines the opponent's pass receiver. <br>
 * Calculations are based on planar curves. For the ball a planar curve is directly calculated from the trajectory.
 * A chipped ball's planar curve starts at the first touchdown location.<br>
 * The robot uses a planar curve segment that assumes it wants to stop as quickly as possible. Hence, its velocity is
 * used for a small lookahead.
 */
public class OpponentPassReceiverCalc extends ACalculator
{
	@Configurable(comment = "[mm] Bots with a minimum curve to (ball) curve distance below this are consired to be pass receivers", defValue = "300.0")
	private static double validReceiveDistanceFirstPass = 300.0;
	@Configurable(comment = "[mm] Bots with a minimum curve to (ball) curve distance below this are consired to be pass receivers", defValue = "450.0")
	private static double validReceiveDistanceSecondPass = 450.0;

	@Configurable(comment = "Minimum ball speed to start looking for a pass receiver. [m/s]", defValue = "1.0")
	private static double minBallSpeed = 1.0;

	@Configurable(comment = "Bots further away than this from the ball travel line are not considered as pass receivers. [mm]", defValue = "3000.0")
	private static double maxBotToBallTravelLineDistance = 3000.0;

	@Getter
	private ITrackedBot opponentPassReceiver;


	@Override
	public boolean isCalculationNecessary()
	{
		return getBall().getVel().getLength2() >= minBallSpeed;
	}


	@Override
	protected void reset()
	{
		opponentPassReceiver = null;
	}


	@Override
	public void doCalc()
	{
		List<ReceiveData> ratings = getRatings();

		Optional<ReceiveData> bestReceiver = ratings.stream()
				.filter(d -> isDistanceValid(d, validReceiveDistanceFirstPass))
				.min(Comparator.comparingDouble(ReceiveData::getDistToBall));

		if (bestReceiver.isEmpty())
		{
			bestReceiver = ratings.stream()
					.filter(d -> isDistanceValid(d, validReceiveDistanceSecondPass))
					.min(Comparator.comparingDouble(ReceiveData::getDistToBall));
		}

		opponentPassReceiver = bestReceiver.map(ReceiveData::getBot).orElse(null);

		// drawing
		for (ReceiveData data : ratings)
		{
			DrawableAnnotation distAnno = new DrawableAnnotation(data.getBot().getPos(),
					String.format("%.1f", data.getDistToBallCurve()),
					Vector2.fromXY(0, -150));
			distAnno.withCenterHorizontally(true);
			getShapes(EAiShapesLayer.DEFENSE_PASS_RECEIVER).add(distAnno);
		}

		if (bestReceiver.isPresent())
		{
			AnimatedCrosshair cross = AnimatedCrosshair
					.aCrosshairWithContinuousRotation(Circle.createCircle(bestReceiver.get().getBot().getPos(), 120), 2.0f,
							Color.CYAN);
			getShapes(EAiShapesLayer.DEFENSE_PASS_RECEIVER).add(cross);
		}
	}


	private List<ReceiveData> getRatings()
	{
		IBallTrajectory ballTrajectory = getBall().getTrajectory();

		var ballCurve = new PlanarCurve(
				ballTrajectory.getPlanarCurve().getSegments()
						.stream()
						.map(this::simplifyHoppingSegments)
						.toList()
		);

		// calculate receive ratings
		return calculateReceiveRatings(
				ballTrajectory,
				getWFrame().getOpponentBots().values(),
				maxBotToBallTravelLineDistance,
				ballCurve
		);
	}


	private boolean isDistanceValid(ReceiveData data, double validDistance)
	{
		// Per meter distance to the ball increase allowed distance to ball curve by 0.1 meter
		var allowedDistance = validDistance + data.getDistToBall() / 10;
		return data.getDistToBallCurve() < allowedDistance;
	}


	private List<ReceiveData> calculateReceiveRatings(final IBallTrajectory ballTrajectory,
			final Collection<ITrackedBot> bots,
			final double maxCheckDistance, final PlanarCurve ballCurve)
	{
		final List<ReceiveData> ratings = new ArrayList<>();
		for (ITrackedBot bot : bots)
		{
			IVector2 botPos = bot.getBotKickerPos();
			if ((ballCurve.getMinimumDistanceToPoint(botPos) > maxCheckDistance)
					|| !ballTrajectory.getTravelLine().isPointInFront(botPos))
			{
				continue;
			}

			// Calculate the time the robots need to a full stop
			double brakeTime = (bot.getVel().getLength2() / bot.getMoveConstraints().getAccMax()) + 0.01;

			// Generate a stop trajectory into the current travel direction
			PlanarCurve botCurve = PlanarCurve.fromPositionVelocityAndAcceleration(bot.getBotKickerPos(),
					bot.getVel().multiplyNew(1e3),
					bot.getVel().scaleToNew(-bot.getMoveConstraints().getAccMax() * 1e3), brakeTime);

			var distToBallCurve = ballCurve.getMinimumDistanceToCurve(botCurve);
			var distToBall = ballTrajectory.getPosByTime(0).getXYVector().distanceTo(bot.getBotKickerPos());

			ratings.add(new ReceiveData(bot, distToBallCurve, distToBall));
		}
		return ratings;
	}


	private PlanarCurveSegment simplifyHoppingSegments(PlanarCurveSegment segment)
	{
		if (segment.getHopHeight() <= 2 * RuleConstraints.getMaxRobotHeight())
		{
			return segment;
		}
		var tStart = segment.getStartTime();
		var tEnd = segment.getEndTime();
		var pEnd = segment.getPosition(tEnd);
		return PlanarCurveSegment.fromPoint(pEnd, tStart, tEnd);
	}


	@Value
	@AllArgsConstructor
	public static class ReceiveData
	{
		ITrackedBot bot;
		double distToBallCurve;
		double distToBall;
	}
}
