/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.05.2014
 * Author(s): dirk, Chris
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;


import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.OffensiveMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.BangBangTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.TrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.DebugShapeHacker;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * blocks the direct shooting line
 * ball.velocity = 0 -> block line between ball and goal center
 * ball.velocity > 0 && shooting line intersects goal line -> block line of the bot + use overacceleration if needed
 * ball.velocity > 0 && shooting line DOES NOT intersect goal line -> block line between ball and goal center
 * 
 * @author dirk, Chris
 *         TODO StateMachine?
 */
public class BlockSkillTraj extends MoveToTrajSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@Configurable(comment = "Speed of the ball [m/s] - If the ball is faster the bot will throw himself into the shooting line.")
	private static float blockDecisionVelocity = 0.1f;
	
	@Configurable(comment = "Deacceleration of the ball [m/s^2]", spezis = { "", "GRSIM" })
	private static float deaccelerationOfBall = 1.0f;
	
	@Configurable(comment = "Max Velocity reached -> Acceleration zero tolerance")
	private static float accTolerance = 10;
	
	private final float distToGoalLine;
	
	@Configurable(comment = "Chip duration for arming kicker")
	private static int chipDuration = 3000;
	
	@Configurable()
	private static int dribbleSpeed = 5000;
	
	@Configurable()
	private static boolean keeperReactsOnAttackerPosition = true;
	
	@Configurable(comment = "Distance [mm] - If an attacker is close to the ball than this the keeper will regard the orientation of the attacker")
	private static int distanceBallAttacker = 800;
	
	@Configurable(comment = "Time to lookahead")
	private static float timeLookAhead = 0.5f;
	
	@Configurable(comment = "Savety of OverAcceleration")
	private static float savetyOverAcceleration = 0.25f;
	
	
	/**
	 * @param distToGoalCenter
	 */
	public BlockSkillTraj(final float distToGoalCenter)
	{
		super(ESkillName.BLOCK);
		distToGoalLine = distToGoalCenter;
		getMoveCon().setPenaltyAreaAllowedOur(true);
	}
	
	
	/**
	 * @param name
	 */
	protected BlockSkillTraj(final ESkillName name)
	{
		super(name);
		distToGoalLine = 500;
		getMoveCon().setPenaltyAreaAllowedOur(true);
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		// super.update(cmds);
		IVector2 nextDestination = calcDefendingDestination();
		float nextOrientation = calcDefendingOrientation();
		if ((driver.getPath() == null) || (!(driver.getPath().getFinalDestination().equals(nextDestination, 1))
			|| !AngleMath.isEqual(driver.getPath().getOrientation(driver.getPath().getTotalTime()), nextOrientation)))
		{
			// System.out.println(nextDestination.x() + " " + nextDestination.y() + " " + nextOrientation);
			finderInput.setTrackedBot(getTBot());
			finderInput.setDest(nextDestination);
			finderInput.setTargetAngle(nextOrientation);
			final TrajPathFinderInput localInput = new TrajPathFinderInput(finderInput);
			getBot().getPathFinder().calcPath(localInput);
			TrajPath path = getBot().getPathFinder().getCurPath();
			driver.setPath(path);
		}
		
		if (GeoMath.distancePP(getWorldFrame().getBall(), getPos()) > 500)
		{
			getDevices().dribble(cmds, false);
		} else
		{
			getDevices().dribble(cmds, dribbleSpeed);
		}
	}
	
	
	protected IVector2 calcDefendingDestination()
	{
		Color color = Color.RED;
		
		IVector2 ballPos = getWorldFrame().getBall().getPosByTime(timeLookAhead);
		IVector2 goalCenter = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		IVector2 goalPostLeft = AIConfig.getGeometry().getGoalOur().getGoalPostLeft();
		IVector2 goalPostRight = AIConfig.getGeometry().getGoalOur().getGoalPostRight();
		// calc the best block position to cover the middle area of the shooting area
		final float botRadius = AIConfig.getGeometry().getBotRadius();
		
		// drive the shortest way into the shooting line
		IVector2 itersectPoint = calcIntersectPoint();
		IVector2 destination = GeoMath.stepAlongLine(goalCenter, GeoMath.leadPointOnLine(getPos(), ballPos,
			itersectPoint), distToGoalLine);
			
		DebugShapeHacker.addDebugShape(new DrawableCircle(new Circle(destination, 20)));
		destination = calcAccelerationTarget(getWorldFrame().getTiger(getBot().getBotID()), destination);
		
		try
		{
			IVector2 ballVel = getWorldFrame().getBall().getVel();
			if ((ballVel.getLength2() != 0)
				&& (Math.abs(GeoMath.intersectionPoint(ballPos, ballVel, goalCenter, AVector2.Y_AXIS)
						.y()) > (((AIConfig.getGeometry().getGoalOur().getSize() / 2) + (2 * AIConfig
								.getGeometry().getBotRadius())))))
			{
			destination = catchRedirect();
			} else
			{
			destination = GeoMath.stepAlongLine(goalCenter, GeoMath.leadPointOnLine(getPos(), ballPos,
					itersectPoint), distToGoalLine);
					
			}
		} catch (MathException err)
		{
		}
		
		// if the destination is on the wrong side of the goal center -> drive directly to the correct one
		IVector2 leadPoint = GeoMath.leadPointOnLine(destination, goalCenter, ballPos);
		IVector2 leadPoint2goalCenter = goalCenter.subtractNew(leadPoint);
		IVector2 leadPoint2ball = ballPos.subtractNew(leadPoint);
		if (((leadPoint2goalCenter.x() / leadPoint2ball.x()) > 0)
			&& ((leadPoint2goalCenter.x() / leadPoint2ball.x()) < 1))
		{
			destination = destination.addNew(leadPoint2goalCenter.scaleToNew(leadPoint2goalCenter.getLength2() * 2));
		}
		// if destination is in a goal Post
		if (GeoMath.distancePP(destination, goalPostRight) < botRadius)
		{
			destination = new Vector2(AIConfig.getGeometry().getGoalOur().getGoalPostRight().x() + botRadius,
				destination.y());
		} else if (GeoMath.distancePP(destination, goalPostLeft) < botRadius)
		{
			destination = new Vector2(AIConfig.getGeometry().getGoalOur().getGoalPostRight().x() + botRadius,
				destination.y());
		}
		DebugShapeHacker.addDebugShape(new DrawableCircle(new Circle(destination, 20), color));
		return destination;
	}
	
	
	/*
	 * calcs intersectPoint on the goalline
	 * Possibilities
	 * 1. ball did not go into goal
	 * catch redirect
	 * 2. is behind the keeper
	 * 3. enemy is close to ball
	 */
	private IVector2 calcIntersectPoint()
	{
		IVector2 ballPos = getWorldFrame().getBall().getPosByTime(timeLookAhead);
		IVector2 goalCenter = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		IVector2 goalPostLeft = AIConfig.getGeometry().getGoalOur().getGoalPostLeft();
		IVector2 goalPostRight = AIConfig.getGeometry().getGoalOur().getGoalPostRight();
		float ballToLeftPost = ballPos.subtractNew(goalPostLeft).getLength2();
		float ballToRightPost = ballPos.subtractNew(goalPostRight).getLength2();
		IVector2 point1 = goalPostRight;
		IVector2 point2 = GeoMath.leadPointOnLine(goalPostRight, goalPostLeft, ballPos);
		if (ballToLeftPost < ballToRightPost)
		{
			point1 = goalPostLeft;
			point2 = GeoMath.leadPointOnLine(goalPostLeft, goalPostRight, ballPos);
		}
		IVector2 intersectPoint = GeoMath.stepAlongLine(point1, point2, point1.subtractNew(point2).getLength2() / 2);
		try
		{
			// Bot nearest to ball
			final IVector2 dir = getWorldFrame().getBall().getVel();
			TrackedTigerBot nearestBot = getTBot();
			float shortestDistance = Float.MAX_VALUE;
			for (TrackedTigerBot foe : getWorldFrame().getFoeBots().values())
			{
			float curDistance = GeoMath.distancePP(foe.getPos(), ballPos);
			IVector2 tempIntersec = GeoMath.intersectionPoint(foe.getPos(), foe.getPos().subtractNew(ballPos),
					goalCenter, AVector2.Y_AXIS);
			if (((curDistance < shortestDistance) || nearestBot.equals(getTBot()))
					// && tempIntersec.equals(checkCornerCases(tempIntersec)))
					&& GeoMath.isVectorBetween(ballPos, foe.getPos(), tempIntersec))
			{
				shortestDistance = curDistance;
				nearestBot = foe;
			}
			}
			if (!dir.equals(AVector2.ZERO_VECTOR, blockDecisionVelocity) && (dir.x() != 0))
			{
			intersectPoint = GeoMath.intersectionPoint(ballPos, dir, goalCenter, AVector2.Y_AXIS);
			
			// if the ball will not go into the goal
			// *2 because ball direction can be noisy (in reality)
			if ((Math.abs(intersectPoint.y()) > (((AIConfig.getGeometry().getGoalOur().getSize() / 2) + (2 * AIConfig
					.getGeometry().getBotRadius())))))
			{
				// block the shooting line of the Redirected FOE Bot or the Goalcenter
				intersectPoint = catchRedirect();
			}
			} else if (keeperReactsOnAttackerPosition
				&& (nearestBot.getPos().subtractNew(ballPos)
						.getLength2() < distanceBallAttacker))
			{
			// enemy bot close to ball, check if he could hit the goal
			intersectPoint = GeoMath.intersectionPoint(
					Line.newLine(nearestBot.getPos(), ballPos),
					Line.newLine(AIConfig.getGeometry().getGoalOur().getGoalCenter(),
							AIConfig.getGeometry().getGoalOur()
									.getGoalPostLeft()));
									
			intersectPoint = checkCornerCases(intersectPoint);
			
			}
		} catch (MathException err)
		{
			return intersectPoint;
		}
		DrawableCircle intersectDebugShape = new DrawableCircle(new Circle(intersectPoint, 50), Color.PINK);
		DebugShapeHacker.addDebugShape(intersectDebugShape);
		// System.out.println("Intersect:" + intersectPoint);
		return intersectPoint;
	}
	
	
	protected float calcDefendingOrientation()
	{
		return getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
	}
	
	
	private IVector2 checkCornerCases(IVector2 possibleIntersectPoint)
	{
		// check corner cases
		possibleIntersectPoint = new Vector2(possibleIntersectPoint.x(), Math.max(-(AIConfig
			.getGeometry().getGoalOur().getSize() / 2)
			+ (2 * AIConfig
					.getGeometry().getBotRadius()),
			possibleIntersectPoint.y()));
		possibleIntersectPoint = new Vector2(possibleIntersectPoint.x(), Math.min((AIConfig.getGeometry()
			.getGoalOur().getSize() / 2)
			- (2 * AIConfig
					.getGeometry().getBotRadius()),
			possibleIntersectPoint.y()));
		return possibleIntersectPoint;
	}
	
	
	/**
	 * @param bot
	 * @param intersection
	 * @return
	 */
	protected IVector2 calcAccelerationTarget(final TrackedTigerBot bot, final IVector2 intersection)
	{
		BangBangTrajectory2D traj = TrajectoryGenerator.generatePositionTrajectory(getTBot(), intersection);
		
		if ((traj.getTotalTime() - savetyOverAcceleration) <= getWorldFrame().getBall().getTimeByPos(intersection))
		{
			return intersection;
		}
		IVector2 tempPos = traj.getPosition(getWorldFrame().getBall().getTimeByPos(intersection));
		IVector2 accPos = intersection;
		float i = GeoMath.distancePP(getPos(), intersection);
		while (GeoMath.isInsideField(tempPos)
			&& (GeoMath.distancePP(tempPos, intersection) > AIConfig.getGeometry().getBallRadius()))
		{
			float timeToInterception = getWorldFrame().getBall().getTimeByPos(intersection);
			i += AIConfig.getGeometry().getBallRadius();
			accPos = GeoMath.stepAlongLine(getPos(), intersection, i);
			traj = TrajectoryGenerator.generatePositionTrajectory(getTBot(), accPos);
			if (traj.getAcceleration(timeToInterception).getLength2() <= accTolerance)
			{
			break;
			}
			tempPos = traj.getPosition(timeToInterception);
		}
		return accPos;
	}
	
	
	private IVector2 catchRedirect()
	{
		try
		{
			final IVector2 ballPos = getWorldFrame().getBall().getPosByTime(timeLookAhead);
			final IVector2 goalCenter = AIConfig.getGeometry().getGoalOur().getGoalCenter();
			TrackedBot nearestFOE = getWorldFrame()
				.getFoeBot(OffensiveMath.getBestRedirector(getWorldFrame(), getWorldFrame().getFoeBots()));
			// isBot NOT looking at goal direction?
			if (nearestFOE == null)
			{
			IVector2 destination = checkCornerCases(
					GeoMath.intersectionPoint(ballPos,
							goalCenter.subtractNew(ballPos),
							goalCenter, AVector2.Y_AXIS));
			return GeoMath.stepAlongLine(destination, ballPos, distToGoalLine);
			}
			// else bot looks at goal with ball infront of him DANGEROUS!
			// System.out.println("catchRedirect" + checkCornerCases(
			// GeoMath.intersectionPoint(nearestFOE.getPos(),
			// ballPos.subtractNew(nearestFOE.getPos()),
			// AIConfig.getGeometry().getGoalOur().getGoalCenter(), AVector2.Y_AXIS)));
			IVector2 destination = checkCornerCases(
				GeoMath.intersectionPoint(nearestFOE.getPos(),
						new Vector2(nearestFOE.getAngle()),
						goalCenter, AVector2.Y_AXIS));
						
			return GeoMath.stepAlongLine(destination, nearestFOE.getPos(),
				(AIConfig.getGeometry().getLengthOfPenaltyAreaFrontLine() / 4) * 3);
		} catch (MathException err)
		{
			return AIConfig.getGeometry().getGoalOur().getGoalCenter();
		}
	}
	
	
	protected IVector2 convertAIVector2SplineNode(final IVector2 vec)
	{
		IVector2 mVec = DistanceUnit.MILLIMETERS.toMeters(vec);
		return mVec;
	}
	
	
	protected float convertAIAngle2SplineOrientation(final float angle)
	{
		return angle;
		
	}
}
