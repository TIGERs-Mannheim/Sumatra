package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePlanarCurve;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.planarcurve.EPlanarCurveSegmentType;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;

import java.awt.Color;
import java.awt.geom.Arc2D;
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
	@Configurable(comment = "Bots with a minimum curve to (ball) curve distance below this are consired to be pass receivers", defValue = "300.0", unit = EConfigUnit.DISTANCE_MM)
	private static double validReceiveDistanceFirstPass = 300.0;
	@Configurable(comment = "Bots with a minimum curve to (ball) curve distance below this are consired to be pass receivers", defValue = "450.0", unit = EConfigUnit.DISTANCE_MM)
	private static double validReceiveDistanceSecondPass = 450.0;
	@Configurable(comment = "Bots with a minimum curve to (ball) curve distance below this are consired to be pass receivers", defValue = "150.0", unit = EConfigUnit.DISTANCE_MM)
	private static double validReceiveDistanceAlternativesPass = 150.0;

	@Configurable(comment = "Minimum ball speed to start looking for a pass receiver.", defValue = "1.0", unit = EConfigUnit.VELOCITY_M)
	private static double minBallSpeed = 1.0;

	@Configurable(comment = "Bots further away than this from the ball travel line are not considered as pass receivers.", defValue = "3000.0", unit = EConfigUnit.DISTANCE_MM)
	private static double maxBotToBallTravelLineDistance = 3000.0;

	@Configurable(comment = "Draw extra shapes", defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean drawDebugShapes = false;

	@Getter
	private ITrackedBot opponentPassReceiver;

	@Getter
	private List<ITrackedBot> alternativeOpponentPassReceiver = List.of();


	@Override
	public boolean isCalculationNecessary()
	{
		return getBall().getVel().getLength2() >= minBallSpeed;
	}


	@Override
	protected void reset()
	{
		opponentPassReceiver = null;
		alternativeOpponentPassReceiver = List.of();
	}


	@Override
	public void doCalc()
	{
		List<ReceiveRating> ratings = getRatings();

		opponentPassReceiver = selectRating(ratings, validReceiveDistanceFirstPass)
				.orElseGet(() -> selectRating(ratings, validReceiveDistanceSecondPass)
						.orElse(null));

		alternativeOpponentPassReceiver = ratings.stream()
				.filter(d -> isDistanceValid(d, validReceiveDistanceAlternativesPass))
				.filter(data -> data.bot != opponentPassReceiver)
				.map(ReceiveRating::bot)
				.toList();

		// drawing
		var shapes = getShapes(EAiShapesLayer.DEFENSE_PASS_RECEIVER);
		if (drawDebugShapes)
		{
			drawValidReceiveDistance(validReceiveDistanceAlternativesPass, Color.BLACK);
			drawValidReceiveDistance(validReceiveDistanceSecondPass, Color.BLUE);
			drawValidReceiveDistance(validReceiveDistanceFirstPass, Color.CYAN);
		}

		for (ReceiveRating rating : ratings)
		{
			DrawableAnnotation distAnno = new DrawableAnnotation(

					rating.bot.getPos(),
					String.format("%.1f", rating.distToBallCurve),
					Vector2.fromXY(0, -150)
			);
			distAnno.withCenterHorizontally(true);
			shapes.add(distAnno);
			if (drawDebugShapes)
			{
				shapes.add(DrawableArrow.fromPositions(rating.bot.getBotKickerPos(), rating.receiveBotPos));
				shapes.add(DrawableArrow.fromPositions(rating.receiveBotPos, rating.receiveBallPos).setColor(Color.GRAY));
			}
		}

		if (opponentPassReceiver != null)
		{
			AnimatedCrosshair cross = AnimatedCrosshair
					.aCrosshairWithContinuousRotation(
							Circle.createCircle(opponentPassReceiver.getPos(), 120), 2.0f,
							Color.CYAN
					);
			shapes.add(cross);
		}
		for (var alternative : alternativeOpponentPassReceiver)
		{
			AnimatedCrosshair cross = AnimatedCrosshair
					.aCrosshairWithContinuousRotation(
							Circle.createCircle(alternative.getPos(), 120), 2.0f,
							Color.BLACK
					);
			shapes.add(cross);
		}
	}


	private Optional<ITrackedBot> selectRating(List<ReceiveRating> ratings, double validReceiveDistance)
	{
		return ratings.stream()
				.filter(rating -> isDistanceValid(rating, validReceiveDistance))
				.min(Comparator.comparingDouble(ReceiveRating::distToBall))
				.map(ReceiveRating::bot);
	}


	private boolean isDistanceValid(ReceiveRating rating, double validReceiveDistance)
	{
		// Per meter distance to the ball increase allowed distance to ball curve by 0.1 meter
		var allowedDistance = validReceiveDistance + rating.distToBall / 10;
		return rating.distToBallCurve < allowedDistance;
	}


	private List<ReceiveRating> getRatings()
	{
		IBallTrajectory ballTrajectory = getBall().getTrajectory();

		var ballCurve = new PlanarCurve(
				ballTrajectory.getPlanarCurve().getSegments()
						.stream()
						.map(this::simplifyHoppingSegments)
						.toList()
		);

		if (drawDebugShapes)
		{
			var color = Color.ORANGE;
			var shapes = getShapes(EAiShapesLayer.DEFENSE_PASS_RECEIVER);
			for (var segment : ballCurve.getSegments())
			{
				if (segment.getType() == EPlanarCurveSegmentType.POINT)
				{
					shapes.add(new DrawablePoint(segment.getPos(), color));
				} else
				{
					var segmentCurve = new PlanarCurve(List.of(segment));
					shapes.add(new DrawablePlanarCurve(segmentCurve).setColor(color));
				}
				color = color.darker();
			}
		}

		// calculate receive ratings
		return calculateReceiveRatings(
				ballTrajectory,
				getWFrame().getOpponentBots().values(),
				maxBotToBallTravelLineDistance,
				ballCurve
		);
	}


	private List<ReceiveRating> calculateReceiveRatings(
			final IBallTrajectory ballTrajectory,
			final Collection<ITrackedBot> bots,
			final double maxCheckDistance, final PlanarCurve ballCurve
	)
	{
		final List<ReceiveRating> ratings = new ArrayList<>();
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
			PlanarCurve botCurve = PlanarCurve.fromPositionVelocityAndAcceleration(
					bot.getBotKickerPos(),
					bot.getVel().multiplyNew(1e3),
					bot.getVel().scaleToNew(-bot.getMoveConstraints().getAccMax() * 1e3), brakeTime
			);

			var timeTillIntercept = ballCurve.getTimeWithMinimumDistanceToCurve(botCurve);
			var receiveBotPos = botCurve.getPos(timeTillIntercept);
			var receiveBallPos = ballCurve.getPos(timeTillIntercept);
			var distToBallCurve = receiveBotPos.distanceTo(receiveBallPos);
			var distToBall = ballTrajectory.getPosByTime(0).getXYVector().distanceTo(receiveBotPos);

			ratings.add(new ReceiveRating(bot, distToBallCurve, distToBall, receiveBotPos, receiveBallPos));
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
		var pEnd = segment.getPosition(segment.getDuration());
		return PlanarCurveSegment.fromPoint(pEnd, tStart, tEnd);
	}


	private void drawValidReceiveDistance(double validReceiveDistance, Color color)
	{
		var ballTravelLine = getBall().getTrajectory().getTravelLineSegment();
		var orthogonalDir = ballTravelLine.directionVector().getNormalVector().normalize();
		var validReceiveDistanceAtEnd = validReceiveDistance + ballTravelLine.getLength() / 10;

		var startPos1 = orthogonalDir.multiplyNew(validReceiveDistance).add(ballTravelLine.getPathStart());
		var startPos2 = orthogonalDir.multiplyNew(-validReceiveDistance).add(ballTravelLine.getPathStart());

		var endPos1 = orthogonalDir.multiplyNew(validReceiveDistanceAtEnd).add(ballTravelLine.getPathEnd());
		var endPos2 = orthogonalDir.multiplyNew(-validReceiveDistanceAtEnd).add(ballTravelLine.getPathEnd());

		var startAngle = Vector2.fromPoints(ballTravelLine.getPathEnd(), endPos1).getAngle();
		var endAngle = Vector2.fromPoints(ballTravelLine.getPathEnd(), endPos2).getAngle();

		getShapes(EAiShapesLayer.DEFENSE_PASS_RECEIVER).add(new DrawableLine(startPos1, startPos2, color));
		getShapes(EAiShapesLayer.DEFENSE_PASS_RECEIVER).add(new DrawableLine(startPos1, endPos1, color));
		getShapes(EAiShapesLayer.DEFENSE_PASS_RECEIVER).add(new DrawableLine(startPos2, endPos2, color));
		getShapes(EAiShapesLayer.DEFENSE_PASS_RECEIVER).add(new DrawableArc(
				Arc.createArc(
						ballTravelLine.getPathEnd(),
						validReceiveDistanceAtEnd,
						startAngle,
						AngleMath.difference(endAngle, startAngle)
				),
				color
		).setArcType(Arc2D.OPEN));
	}


	private record ReceiveRating(
			ITrackedBot bot,
			double distToBallCurve,
			double distToBall,
			IVector2 receiveBotPos,
			IVector2 receiveBallPos
	)
	{}
}
