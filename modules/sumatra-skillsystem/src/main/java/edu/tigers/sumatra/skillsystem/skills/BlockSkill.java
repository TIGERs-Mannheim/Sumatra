/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;


import static edu.tigers.sumatra.math.SumatraMath.isZero;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.botskills.AMoveBotSkill;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryMath;
import edu.tigers.sumatra.units.DistanceUnit;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;


/**
 * blocks the direct shooting line
 * ball.velocity = 0 -> block line between ball and goal center
 * ball.velocity > 0 && shooting line intersects goal line -> block line of the bot + use overacceleration if needed
 * ball.velocity > 0 && shooting line DOES NOT intersect goal line -> block line between ball and goal center
 * 
 * @author dirk
 */
public class BlockSkill extends AMoveSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@Configurable(comment = "Speed of the ball [m/s] - If the ball is faster the bot will throw himself into the shooting line.")
	private static double blockDecisionVelocity = 0.1;
	
	@Configurable(comment = "Deacceleration of the ball [m/s^2]", spezis = { "", "GRSIM" })
	private static double deaccelerationOfBall = 1.0;
	
	private final double distToGoalLine;
	
	private final int maxSplineLength;
	
	@Configurable(comment = "Chip duration for arming kicker")
	private static int chipDuration = 3000;
	
	@Configurable()
	private static int dribbleSpeed = 5000;
	
	@Configurable()
	private static boolean keeperReactsOnAttackerPosition = true;
	
	@Configurable(comment = "Distance [mm] - If an attacker is close to the ball than this the keeper will regard the orientation of the attacker")
	private static int distanceBallAttacker = 500;
	
	
	/**
	 * @param distToGoalCenter
	 * @param maxSplineLength
	 */
	public BlockSkill(final double distToGoalCenter, final int maxSplineLength)
	{
		super(ESkill.BLOCK);
		distToGoalLine = distToGoalCenter;
		this.maxSplineLength = maxSplineLength;
		getMoveCon().setPenaltyAreaAllowedOur(true);
	}
	
	
	/**
	 * @param name
	 */
	protected BlockSkill(final ESkill name)
	{
		super(name);
		distToGoalLine = 500;
		maxSplineLength = 3000;
		getMoveCon().setPenaltyAreaAllowedOur(true);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		IVector2 dest = calcDefendingDestination();
		double targetAngle = calcDefendingOrientation();
		
		getShapes().get(ESkillShapesLayer.PATH).add(
				new DrawableBot(dest, targetAngle,
						getTBot().getBotId().getTeamColor().getColor(),
						Geometry.getBotRadius() + 25,
						Geometry.getBotRadius() + 25));
		AMoveBotSkill skill = setTargetPose(dest, targetAngle,
				getMoveCon().getMoveConstraints());
		skill.getKickerDribbler().setDribblerSpeed(getWorldFrame().getBall().getRpm());
	}
	
	
	protected IVector2 calcDefendingDestination()
	{
		IVector2 destination;
		boolean overAccelerationNecessary = false;
		
		IVector2 goalCenter = Geometry.getGoalOur().getCenter();
		IVector2 goalPostLeft = Geometry.getGoalOur().getLeftPost();
		IVector2 goalPostRight = Geometry.getGoalOur().getRightPost();
		IVector2 curBallPos = getWorldFrame().getBall().getPos();
		IVector2 futureBallPos = getWorldFrame().getBall().getTrajectory().getPosByTime(1);
		// calc the best block position to cover the middle area of the shooting area
		double ballToLeftPost = futureBallPos.subtractNew(goalPostLeft).getLength2();
		double ballToRightPost = futureBallPos.subtractNew(goalPostRight).getLength2();
		
		IVector2 point1 = goalPostRight;
		IVector2 point2 = LineMath.leadPointOnLine(Line.fromPoints(goalPostLeft, futureBallPos), goalPostRight);
		if (ballToLeftPost < ballToRightPost)
		{
			point1 = goalPostLeft;
			point2 = LineMath.leadPointOnLine(Line.fromPoints(goalPostRight, futureBallPos), goalPostLeft);
		}
		
		IVector2 intersectPoint = LineMath.stepAlongLine(point1, point2, point1.subtractNew(point2).getLength2() / 2.0);
		final IVector2 start;
		final IVector2 dir;
		start = futureBallPos;
		dir = getWorldFrame().getBall().getVel();
		
		List<IVector2> foeBots = getWorldFrame().getFoeBots().values().stream().map(ITrackedObject::getPos)
				.collect(Collectors.toList());
		IVector2 nearestFoe = curBallPos.nearestTo(foeBots);
		
		if (!dir.isCloseTo(AVector2.ZERO_VECTOR, blockDecisionVelocity) && (!isZero(dir.x())))
		{
			Optional<IVector2> newIntersectionPoint = LineMath.intersectionPoint(Line.fromDirection(start, dir),
					Line.fromDirection(goalCenter, AVector2.Y_AXIS));
			
			if (newIntersectionPoint.isPresent())
			{
				intersectPoint = newIntersectionPoint.get();
			} else
			{
				return intersectPoint;
			}
			
			// if the ball will not go into the goal
			// *2 because ball direction can be noisy (in reality)
			if ((Math.abs(
					intersectPoint.y()) > (((Geometry.getGoalOur().getWidth() / 2.0) + (2 * Geometry.getBotRadius()))))) // ||
																																							// (getWorldFrame().getBall().getVel().x()
																																							// >
																																							// 0))
			{
				// block the shooting line to the middle of the goal
				intersectPoint = goalCenter;
			} else
			{
				if (futureBallPos.y() > getPos().y())
				{
					// block the shooting line to the point where the ball will cross the goal line
					overAccelerationNecessary = true;
				} else
				{
					// the ball is already behind the keeper, should not happen but it happens,
					if (getWorldFrame().getBall().getVel().getLength2() > 0.5)
					{
						// very bad, the ball is on its way to the goal, no keeper and it has enough speed to reach the
						// goal
						// TODO think about a solution
					}
				}
			}
		} else if (keeperReactsOnAttackerPosition
				&& (nearestFoe.subtractNew(curBallPos).getLength2() < distanceBallAttacker))
		{
			// enemy bot close to ball, check if he could hit the goal
			intersectPoint = LineMath.intersectionPoint(
					Line.fromPoints(nearestFoe, futureBallPos),
					Line.fromPoints(Geometry.getGoalOur().getCenter(),
							Geometry.getGoalOur()
									.getLeftPost()))
					.orElse(intersectPoint);
			
			// check corner cases
			intersectPoint = Vector2.fromXY(intersectPoint.x(), Math.max(-(Geometry.getGoalOur().getWidth() / 2.0)
					+ (2 * Geometry.getBotRadius()),
					intersectPoint.y()));
			intersectPoint = Vector2.fromXY(intersectPoint.x(), Math.min((Geometry
					.getGoalOur().getWidth() / 2.0)
					- (2 * Geometry.getBotRadius()),
					intersectPoint.y()));
			
		}
		// drive the shortest way into the shooting line
		ILine ballToIntersectionLine = Line.fromPoints(curBallPos, intersectPoint);
		destination = LineMath.leadPointOnLine(ballToIntersectionLine, getPos());
		
		double distance = VectorMath.distancePP(destination, getPos());
		
		// if we are already blocking the ball we can do the fine tuning: position on the exact shooting line and
		// half a goal size away from the goal center
		if (distance < (Geometry.getBotRadius() / 2.0))
		{
			overAccelerationNecessary = false;
			// if the bot is behind the goal line but the ball is infront of it
			if (((getPos().x() < (-(Geometry.getFieldLength() / 2.0)))))
			{
				// drive out of the Goal!!!
				IVector2 leftPost = Geometry.getGoalOur().getLeftPost();
				ILine centerToLeftPostLine = Line.fromPoints(goalCenter, leftPost);
				destination = LineMath.leadPointOnLine(centerToLeftPostLine, getPos())
						.addNew(Vector2.fromX(distToGoalLine));
			} else
			{
				// perfection of the blocking position
				destination = LineMath.stepAlongLine(goalCenter, destination, distToGoalLine);
			}
		}
		
		
		if (overAccelerationNecessary)
		{
			destination = getAccelerationTarget(getWorldFrame().getTiger(getTBot().getBotId()), destination);
		}
		
		// if the destination is on the wrong side of the goal center -> drive directly to the correct one
		IVector2 leadPoint = LineMath.leadPointOnLine(Line.fromPoints(goalCenter, curBallPos), destination);
		IVector2 leadPoint2goalCenter = goalCenter.subtractNew(leadPoint);
		IVector2 leadPoint2ball = curBallPos.subtractNew(leadPoint);
		if (((leadPoint2goalCenter.x() / leadPoint2ball.x()) > 0)
				&& ((leadPoint2goalCenter.x() / leadPoint2ball.x()) < 1))
		{
			destination = destination.addNew(leadPoint2goalCenter.scaleToNew(leadPoint2goalCenter.getLength2() * 2));
		}
		
		return destination;
	}
	
	
	protected double calcDefendingOrientation()
	{
		return getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
	}
	
	
	/**
	 * @param bot
	 * @param intersection
	 * @return
	 */
	protected IVector2 getAccelerationTarget(final ITrackedBot bot, final IVector2 intersection)
	{
		double ballTime = timeOfBallToIntersection(getWorldFrame().getBall(), intersection);
		for (int i = maxSplineLength; i > (int) VectorMath.distancePP(getPos(), intersection); i = i - 100)
		{
			double timeOfBot = timeOfBotToIntersection(bot, intersection, i);
			// if (ballTime < 10)
			// {
			// // log.warn("timeOfBall: " + ballTime + ", timeOfBot: " + timeOfBot);
			// timeOfBallToIntersection(getWorldFrame().getBall(), intersection);
			// }
			if (timeOfBot > ballTime)
			{
				// fine tuning
				for (int j = i + 100; j > i; j--)
				{
					timeOfBot = timeOfBotToIntersection(bot, intersection, j);
					if (timeOfBot > ballTime)
					{
						return LineMath.stepAlongLine(getPos(), intersection, j);
					}
				}
			}
		}
		return LineMath.stepAlongLine(getPos(), intersection, maxSplineLength);
	}
	
	
	/**
	 * @param bot
	 * @param intersection
	 * @param splineLength
	 * @return
	 */
	protected double timeOfBotToIntersection(final ITrackedBot bot, final IVector2 intersection,
			final int splineLength)
	{
		IVector2 possibleAccTarget = LineMath.stepAlongLine(intersection, getPos(), -splineLength);
		ITrajectory<IVector2> path = TrajectoryGenerator.generatePositionTrajectory(bot, possibleAccTarget);
		double bot2intersect = VectorMath.distancePP(getPos(), intersection);
		return TrajectoryMath.timeAfterDrivenWay(path, bot2intersect, 0.05);
	}
	
	
	protected IVector2 convertAIVector2SplineNode(final IVector2 vec)
	{
		IVector2 mVec = DistanceUnit.MILLIMETERS.toMeters(vec);
		return mVec;
	}
	
	
	protected double convertAIAngle2SplineOrientation(final double angle)
	{
		return angle;
	}
	
	
	/**
	 * @param ball
	 * @param intersection
	 * @return
	 */
	protected double timeOfBallToIntersection(final ITrackedBall ball, final IVector2 intersection)
	{
		// TODO Use function from Mark in TrackedBall
		double distanceBallIntersection = VectorMath.distancePP(ball.getPos(), intersection);
		double ballVel = DistanceUnit.METERS.toMillimeters(ball.getVel()).getLength2();
		double deacc = DistanceUnit.METERS.toMillimeters(deaccelerationOfBall);
		double pqBeforeSqrt = ballVel / deacc;
		double pqUnderSqrt = ((ballVel / deacc) * (ballVel / deacc)) - ((2 * distanceBallIntersection) / deacc);
		// ball will stop before it reaches the bot
		if (pqUnderSqrt <= 0)
		{
			return Double.MAX_VALUE;
		}
		return pqBeforeSqrt - (Math.sqrt(pqUnderSqrt));
	}
}
