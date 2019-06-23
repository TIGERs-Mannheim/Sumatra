/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.wp.ball.prediction.IBallTrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Provides some generic methods to the defense.
 */
public final class DefenseMath
{
	@Configurable(comment = "Upper direct reflect threshold (rad)", defValue = "0.4")
	private static double upperDirectReflectThreshold = 0.4;
	
	@Configurable(comment = "Max angle to stop and control a ball (rad)", defValue = "3.14")
	private static double maxBallAcceptAngle = 3.14;
	
	@Configurable(comment = "Max time to turn and shoot a ball directed (s)", defValue = "0.3")
	private static double maxBallAcceptTime = 0.3;
	
	
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
	 * @param threatPos The threat to protect the defBoundary from
	 * @param defBoundaryLeft The left boundary
	 * @param defBoundaryRight The right boundary
	 * @param width distance that covers half the area
	 * @return
	 */
	public static IVector2 calculateLineDefPoint(final IVector2 threatPos, final IVector2 defBoundaryLeft,
			final IVector2 defBoundaryRight,
			final double width)
	{
		IVector2 intersectionBisectorGoal = TriangleMath.bisector(threatPos, defBoundaryLeft, defBoundaryRight);
		
		double angleBallLeftGoal = defBoundaryLeft.subtractNew(threatPos).angleToAbs(
				intersectionBisectorGoal.subtractNew(threatPos)).orElse(0.0);
		
		double distBall2DefPoint = width / SumatraMath.tan(angleBallLeftGoal);
		distBall2DefPoint = Math.min(distBall2DefPoint, VectorMath.distancePP(threatPos, intersectionBisectorGoal));
		
		return LineMath.stepAlongLine(threatPos, intersectionBisectorGoal, distBall2DefPoint);
	}
	
	
	/**
	 * Get a line segment on which a threat can be protected by a defender
	 * 
	 * @param threatLine the threat line to protect
	 * @param marginToThreat distance to keep from bot center to threat pos
	 * @param marginToPenArea distance to keep to penArea
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
	 * Get protection line between threat and ball
	 *
	 * @param threatPos the threat position to protect
	 * @param ballPos ball position
	 * @param marginToThreat distance to keep from bot center to threat pos
	 * @param marginToPenArea distance to keep to penArea
	 * @param maxGoOutDistance max distance to go out from goal center
	 * @return
	 */
	public static ILineSegment getThreatDefendingLineToBall(final IVector2 threatPos, final IVector2 ballPos,
			final double marginToThreat,
			final double marginToPenArea,
			final double maxGoOutDistance)
	{
		IVector2 base = Geometry.getField().nearestPointInside(ballPos);
		IPenaltyArea penArea = Geometry.getPenaltyAreaOur().withMargin(marginToPenArea);
		base = base.nearestToOpt(penArea.lineIntersections(Lines.lineFromPoints(base, threatPos)))
				.orElse(penArea.nearestPointOutside(base));
		IVector2 protectionPos = Geometry.getField().nearestPointInside(threatPos);
		IVector2 end = base.nearestToOpt(penArea.lineIntersections(Lines.lineFromPoints(base, protectionPos)))
				.orElse(penArea.nearestPointOutside(base));
		end = getPosInsideMargin(end, protectionPos, maxGoOutDistance);
		IVector2 start = LineMath.stepAlongLine(protectionPos, base, marginToThreat);
		start = getPosInsideMargin(start, end, maxGoOutDistance);
		return Lines.segmentFromPoints(start, end);
	}
	
	
	private static IVector2 getPosInsideMargin(final IVector2 desiredPos, final IVector2 toBuildLine,
			final double margin)
	{
		if (desiredPos.distanceTo(Geometry.getGoalOur().getCenter()) > margin)
		{
			ILine border = Lines.lineFromDirection(
					Vector2.fromX(Geometry.getGoalOur().getCenter().x() + margin), Vector2.fromY(1));
			ILineSegment protectionLine = Lines.segmentFromPoints(desiredPos, toBuildLine);
			Optional<IVector2> intersect = protectionLine.intersectLine(border);
			return intersect.orElseGet(() -> Geometry.getFieldHalfOur().withMargin((-Geometry.getFieldLength() / 2)
					+ margin).nearestPointInside(desiredPos));
		}
		return desiredPos;
	}
	
	
	/**
	 * Construct a line based on {@link #getThreatDefendingLine(ILineSegment, double, double, double)} with parameters
	 * for center back role
	 * 
	 * @param threatLine
	 * @return the line segment
	 */
	public static ILineSegment getThreatDefendingLineForCenterBack(final ILineSegment threatLine)
	{
		return getThreatDefendingLine(threatLine,
				Geometry.getBotRadius() * 2,
				DefenseConstants.getMinGoOutDistance(),
				DefenseConstants.getMaxGoOutDistance());
	}
	
	
	/**
	 * Time a bot needs to catch a ball and rotate to a new angle
	 *
	 * @param botPos
	 * @param ballPos
	 * @return
	 */
	public static double calculateTDeflect(final IVector2 botPos, final IVector2 ballPos)
	{
		return calculateTDeflect(botPos, ballPos, DefenseMath.getBisectionGoal(botPos));
	}
	
	
	/**
	 * Time a bot needs to catch a ball and rotate to a new angle
	 *
	 * @param botPos
	 * @param ballPos
	 * @param target
	 * @return
	 */
	public static double calculateTDeflect(final IVector2 botPos, final IVector2 ballPos, final IVector2 target)
	{
		final IVector2 bot2ball = Vector2.fromPoints(botPos, ballPos);
		final IVector2 bot2target = Vector2.fromPoints(botPos, target);
		
		final double angleDiff = bot2ball.angleToAbs(bot2target).orElse(Math.PI);
		
		double factor = SumatraMath.relative(angleDiff, upperDirectReflectThreshold, maxBallAcceptAngle);
		
		return factor * maxBallAcceptTime;
	}
	
	
	/**
	 * @param source position of the ball or a bot
	 * @return bisector from source to in between the goal posts
	 */
	public static IVector2 getBisectionGoal(final IVector2 source)
	{
		final IVector2 postLeft = Geometry.getGoalOur().getLeftPost();
		final IVector2 postRight = Geometry.getGoalOur().getRightPost();
		
		return TriangleMath.bisector(source, postLeft, postRight);
	}
	
	
	/**
	 * Calculates a rating for each bot based on Planar Curves.
	 * For the ball a planar curve is directly calculated from the trajectory.
	 * A chipped ball's planar curve starts at the first touchdown location.<br>
	 * The robot uses a planar curve segment that assumes it wants to stop as quickly as possible. Hence, its velocity is
	 * used for a small lookahead.
	 * 
	 * @param ballTrajectory the ball trajectory
	 * @param bots bots to check
	 * @param maxCheckDistance bots with a lead point distance to the ball travel line segment greater than this are
	 *           skipped
	 * @return list of ratings (minimum distance between ball planar curve and bot brake planar curve)
	 */
	public static List<ReceiveData> calcReceiveRatingsFor(
			final IBallTrajectory ballTrajectory,
			final Collection<ITrackedBot> bots,
			final double maxCheckDistance)
	{
		// if this is a chip kick we start the planar curve behind the first touchdown
		List<IVector2> touchdowns = ballTrajectory.getTouchdownLocations();
		double tStart = 0.0;
		if (!touchdowns.isEmpty())
		{
			tStart = Math.max(0.0, ballTrajectory.getTimeByPos(touchdowns.get(0)));
		}
		PlanarCurve ballCurve = ballTrajectory.getPlanarCurve().restrictToStart(tStart);
		
		// calculate receive ratings
		List<ReceiveData> ratings = new ArrayList<>();
		
		for (ITrackedBot bot : bots)
		{
			IVector2 botPos = bot.getBotKickerPos();
			if ((ballCurve.getMinimumDistanceToPoint(botPos) > maxCheckDistance)
					|| !ballTrajectory.getTravelLine().isPointInFront(botPos))
			{
				continue;
			}
			
			ratings.add(new ReceiveData(bot, ballCurve, ballTrajectory.getPosByTime(0).getXYVector()));
		}
		return ratings;
	}
	
	/** Data holder for OpponentPassReceiverCalc */
	public static class ReceiveData
	{
		private final ITrackedBot bot;
		private final double distToBallCurve;
		private final double distToBall;
		
		
		/**
		 * Default
		 * 
		 * @param ballCurve
		 * @param ballPos
		 * @param bot
		 */
		public ReceiveData(final ITrackedBot bot, final PlanarCurve ballCurve, final IVector2 ballPos)
		{
			this.bot = bot;
			
			// Calculate the time the robots need to a full stop
			double brakeTime = (bot.getVel().getLength2() / bot.getMoveConstraints().getAccMax()) + 0.01;
			
			// Generate a stop trajectory into the current travel direction
			PlanarCurve botCurve = PlanarCurve.fromPositionVelocityAndAcceleration(bot.getBotKickerPos(),
					bot.getVel().multiplyNew(1e3),
					bot.getVel().scaleToNew(-bot.getMoveConstraints().getAccMax() * 1e3), brakeTime);
			
			distToBallCurve = ballCurve.getMinimumDistanceToCurve(botCurve);
			distToBall = ballPos.distanceTo(bot.getBotKickerPos());
		}
		
		
		public ITrackedBot getBot()
		{
			return bot;
		}
		
		
		public double getDistToBallCurve()
		{
			return distToBallCurve;
		}
		
		
		public double getDistToBall()
		{
			return distToBall;
		}
	}
}
