/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Provides some generic methods to the defense.
 */
public final class DefenseMath
{
	static
	{
		ConfigRegistration.registerClass("metis", DefenseMath.class);
	}


	private DefenseMath()
	{
		// Hide public constructor
	}


	/**
	 * Calculates a point to protect a line (like the goal) from a threat.
	 * Field boundaries are not checked!
	 *
	 * @param threatPos        The threat to protect the defBoundary from
	 * @param defBoundaryLeft  The left boundary
	 * @param defBoundaryRight The right boundary
	 * @param width            distance that covers half the area
	 * @return
	 */
	public static IVector2 calculateLineDefPoint(final IVector2 threatPos, final IVector2 defBoundaryLeft,
			final IVector2 defBoundaryRight,
			final double width)
	{
		IVector2 intersectionBisectorGoal = TriangleMath.bisector(threatPos, defBoundaryLeft, defBoundaryRight);

		double angleBallLeftGoal = defBoundaryLeft.subtractNew(threatPos).angleToAbs(
				intersectionBisectorGoal.subtractNew(threatPos)).orElse(0.0);

		double distBall2DefPoint = angleBallLeftGoal > 0 ? width / SumatraMath.tan(angleBallLeftGoal) : 0.0;
		distBall2DefPoint = Math.min(distBall2DefPoint, VectorMath.distancePP(threatPos, intersectionBisectorGoal));

		return LineMath.stepAlongLine(threatPos, intersectionBisectorGoal, distBall2DefPoint);
	}


	/**
	 * Get a line segment on which a threat can be protected by a defender
	 *
	 * @param threatLine       the threat line to protect
	 * @param marginToThreat   distance to keep from bot center to threat pos
	 * @param marginToPenArea  distance to keep to penArea
	 * @param maxGoOutDistance max distance to go out from goal center
	 * @return the line segment
	 */
	public static ILineSegment getThreatDefendingLine(
			final ILineSegment threatLine,
			final double marginToThreat,
			final double marginToPenArea,
			final double maxGoOutDistance)
	{
		IVector2 base = threatLine.getEnd();
		IVector2 protectionPos = Geometry.getField().nearestPointInside(threatLine.getStart());
		IPenaltyArea penArea = Geometry.getPenaltyAreaOur().withMargin(marginToPenArea);
		IVector2 end = base.nearestToOpt(penArea.lineIntersections(Lines.lineFromPoints(base, protectionPos)))
				.orElse(penArea.nearestPointOutside(base));

		IVector2 start = LineMath.stepAlongLine(protectionPos, base, marginToThreat);
		double distStart2Goal = start.distanceTo(base);
		if (distStart2Goal > maxGoOutDistance)
		{
			start = LineMath.stepAlongLine(base, start, maxGoOutDistance);
		} else if (distStart2Goal < end.distanceTo(base))
		{
			start = end;
		}
		return Lines.segmentFromPoints(start, end);
	}


	/**
	 * Calculates a rating for each bot based on Planar Curves.
	 * For the ball a planar curve is directly calculated from the trajectory.
	 * A chipped ball's planar curve starts at the first touchdown location.<br>
	 * The robot uses a planar curve segment that assumes it wants to stop as quickly as possible. Hence, its velocity is
	 * used for a small lookahead.
	 *
	 * @param ballTrajectory   the ball trajectory
	 * @param bots             bots to check
	 * @param maxCheckDistance bots with a lead point distance to the ball travel line segment greater than this are
	 *                         skipped
	 * @param passTarget       passToTarget
	 * @param tStart           time after which the kick reaches its first touchdown point (can be zero for straight)
	 * @return list of ratings (minimum distance between ball planar curve and bot brake planar curve)
	 */
	public static List<ReceiveData> calcReceiveRatingsForRestrictedStartAndEnd(
			final IBallTrajectory ballTrajectory,
			final Collection<ITrackedBot> bots,
			final double maxCheckDistance,
			final IVector2 passTarget, double tStart)
	{
		// if this is a chip kick we start the planar curve behind the first touchdown
		double tEnd = ballTrajectory.getTimeByDist(ballTrajectory.getPosByTime(0.0).getXYVector().distanceTo(passTarget));
		if (tStart >= tEnd)
		{
			return Collections.emptyList();
		}

		PlanarCurve ballCurve = ballTrajectory.getPlanarCurve().restrictToEnd(tEnd);
		if (tStart > 1e-3)
		{
			ballCurve = simplifyBallCurve(ballCurve);
		}

		// calculate receive ratings
		return calculateReceiveRatings(ballTrajectory, bots, maxCheckDistance, ballCurve);
	}


	/**
	 * Calculates a rating for each bot based on Planar Curves.
	 * For the ball a planar curve is directly calculated from the trajectory.
	 * A chipped ball's planar curve starts at the first touchdown location.<br>
	 * The robot uses a planar curve segment that assumes it wants to stop as quickly as possible. Hence, its velocity is
	 * used for a small lookahead.
	 *
	 * @param ballTrajectory   the ball trajectory
	 * @param bots             bots to check
	 * @param maxCheckDistance bots with a lead point distance to the ball travel line segment greater than this are
	 *                         skipped
	 * @return list of ratings (minimum distance between ball planar curve and bot brake planar curve)
	 */
	public static List<ReceiveData> calcReceiveRatingsForRestrictedStart(
			final IBallTrajectory ballTrajectory,
			final Collection<ITrackedBot> bots,
			final double maxCheckDistance)
	{
		PlanarCurve ballCurve = ballTrajectory.getPlanarCurve();
		ballCurve = simplifyBallCurve(ballCurve);

		// calculate receive ratings
		return calculateReceiveRatings(ballTrajectory, bots, maxCheckDistance, ballCurve);
	}


	/**
	 * Calculates a rating for each bot based on Planar Curves.
	 * For the ball a planar curve is directly calculated from the trajectory.
	 * A chipped ball's planar curve starts at the first touchdown location.<br>
	 * The robot uses a planar curve segment that assumes it wants to stop as quickly as possible. Hence, its velocity is
	 * used for a small lookahead.
	 *
	 * @param ballTrajectory   the ball trajectory
	 * @param bots             bots to check
	 * @param maxCheckDistance bots with a lead point distance to the ball travel line segment greater than this are
	 *                         skipped
	 * @return list of ratings (minimum distance between ball planar curve and bot brake planar curve)
	 */
	public static List<ReceiveData> calcReceiveRatingsNonRestricted(
			final IBallTrajectory ballTrajectory,
			final Collection<ITrackedBot> bots,
			final double maxCheckDistance)
	{
		PlanarCurve ballCurve = ballTrajectory.getPlanarCurve();

		// calculate receive ratings
		return calculateReceiveRatings(ballTrajectory, bots, maxCheckDistance, ballCurve);
	}


	private static List<ReceiveData> calculateReceiveRatings(final IBallTrajectory ballTrajectory,
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


	private static PlanarCurve simplifyBallCurve(PlanarCurve ballCurve)
	{
		List<PlanarCurveSegment> segments = ballCurve.getSegments();
		double t1 = segments.get(0).getStartTime();
		double t2 = segments.get(0).getEndTime();
		IVector2 pos = segments.get(0).getPosition(t2);
		segments.remove(0);
		segments.add(0, PlanarCurveSegment.fromPoint(pos, t1, t2));
		return new PlanarCurve(segments);
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
