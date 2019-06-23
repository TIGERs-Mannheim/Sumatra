/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.05.2014
 * Author(s): dirk
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.ShapeLayer;


/**
 * blocks the direct shooting line
 * ball.velocity = 0 -> block line between ball and goal center
 * ball.velocity > 0 && shooting line intersects goal line -> block line of the bot + use overacceleration if needed
 * ball.velocity > 0 && shooting line DOES NOT intersect goal line -> block line between ball and goal center
 * 
 * @author dirk
 */
public class BlockSkill extends PositionSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log							= Logger.getLogger(BlockSkill.class.getName());
	
	@Configurable(comment = "Speed of the ball [m/s] - If the ball is faster the bot will throw himself into the shooting line.")
	private static float				blockDecisionVelocity	= 0.1f;
	
	@Configurable(comment = "Deacceleration of the ball [m/s^2]", speziType = EBotType.class, spezis = { "GRSIM" })
	private static float				deaccelerationOfBall		= 1.0f;
	
	private final int					distToGoalLine;
	
	private final int					maxSplineLength;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param distToGoalCenter
	 * @param maxSplineLength
	 */
	public BlockSkill(final int distToGoalCenter, final int maxSplineLength)
	{
		super(ESkillName.BLOCK);
		distToGoalLine = distToGoalCenter;
		this.maxSplineLength = maxSplineLength;
	}
	
	
	@Override
	public final void doCalcActions(final List<ACommand> cmds)
	{
		super.setDestination(calcDefendingDestination());
		super.setOrientation(getWorldFrame().getBall().getPos().subtractNew(getPos())
				.getAngle());
	}
	
	
	private IVector2 calcDefendingDestination()
	{
		IVector2 destination;
		boolean overAccelerationNecessary = false;
		
		IVector2 goalCenter = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		IVector2 intersectPoint = goalCenter;
		try
		{
			final IVector2 start;
			final IVector2 dir;
			start = getWorldFrame().getBall().getPos();
			dir = getWorldFrame().getBall().getVel();
			
			if (!dir.equals(AVector2.ZERO_VECTOR, blockDecisionVelocity) && (dir.x() != 0))
			{
				intersectPoint = GeoMath.intersectionPoint(start, dir, goalCenter, AVector2.Y_AXIS);
				
				// if the ball will not go into the goal
				if (Math.abs(intersectPoint.y()) > ((AIConfig.getGeometry().getGoalOur().getSize() / 2) + AIConfig
						.getGeometry().getBotRadius()))
				{
					// block the shooting line to the middle of the goal
					intersectPoint = goalCenter;
				} else
				{
					// block the shooting line to the point where the ball will cross the goal line
					overAccelerationNecessary = true;
				}
			}
		} catch (MathException err)
		{
			log.warn("Math Error. Please inform Dirk", err);
			return intersectPoint;
		}
		// drive the shortest way into the shooting line
		destination = GeoMath.leadPointOnLine(getPos(), getWorldFrame().getBall().getPos(), intersectPoint);
		
		float distance = GeoMath.distancePP(destination, getPos());
		
		// if we are already blocking the ball we can do the fine tuning: position on the exact shooting line and
		// half a goal size away from the goal center
		if (distance < (AIConfig.getGeometry().getBotRadius() / 2))
		{
			overAccelerationNecessary = false;
			// if the bot is behind the goal line but the ball is infront of it
			if (((getPos().x() < -(AIConfig.getGeometry().getFieldLength() / 2))))
			// && !(getWorldFrame().getBall().getPos().x() < -(AIConfig.getGeometry().getFieldLength() / 2))))// ||
			{
				// drive out of the Goal!!!
				destination = GeoMath.leadPointOnLine(getPos(), goalCenter,
						AIConfig.getGeometry().getGoalOur().getGoalPostLeft()).addX(distToGoalLine);
			} else
			{
				// perfection of the blocking position
				destination = GeoMath.stepAlongLine(goalCenter, destination, distToGoalLine);
			}
		}
		
		
		if (overAccelerationNecessary)
		{
			ShapeLayer.addDebugShape(new DrawableCircle(new Circle(destination, 20)));
			destination = getAccelerationTarget(getWorldFrame().getTiger(getBot().getBotID()), destination);
		}
		ShapeLayer.addDebugShape(new DrawableCircle(new Circle(destination, 20), Color.BLUE));
		
		// if the destination is on the wrong side of the goal center -> drive directly to the correct one
		IVector2 leadPoint = GeoMath.leadPointOnLine(destination, goalCenter, getWorldFrame().getBall().getPos());
		IVector2 leadPoint2goalCenter = goalCenter.subtractNew(leadPoint);
		IVector2 leadPoint2ball = getWorldFrame().getBall().getPos().subtractNew(leadPoint);
		if (((leadPoint2goalCenter.x() / leadPoint2ball.x()) > 0)
				&& ((leadPoint2goalCenter.x() / leadPoint2ball.x()) < 1))
		{
			destination = destination.addNew(leadPoint2goalCenter.scaleToNew(leadPoint2goalCenter.getLength2() * 2));
		}
		
		return destination;
	}
	
	
	/**
	 * @param bot
	 * @param intersection
	 * @return
	 */
	private IVector2 getAccelerationTarget(final TrackedTigerBot bot, final IVector2 intersection)
	{
		float ballTime = timeOfBallToIntersection(getWorldFrame().getBall(), intersection);
		for (int i = (int) GeoMath.distancePP(getPos(), intersection); i < maxSplineLength; i++)
		{
			float timeOfBot = timeOfBotToIntersection(bot, intersection, i);
			if (ballTime < 10)
			{
				// log.warn("timeOfBall: " + ballTime + ", timeOfBot: " + timeOfBot);
				timeOfBallToIntersection(getWorldFrame().getBall(), intersection);
			}
			if (timeOfBot < ballTime)
			{
				
				// if (i != (int) GeoMath.distancePP(getPos(), intersection))
				// {
				// log.warn("timeOfBot: " + timeOfBot + ", timeOfBall: " + ballTime + ", originalTime: "
				// + timeOfBotToIntersection(bot, intersection, i));
				// }
				return GeoMath.stepAlongLine(getPos(), intersection, i);
			}
		}
		return GeoMath.stepAlongLine(getPos(), intersection, maxSplineLength);
	}
	
	
	/**
	 * @param bot
	 * @param intersection
	 * @param splineLength
	 * @return
	 */
	private float timeOfBotToIntersection(final TrackedTigerBot bot, final IVector2 intersection,
			final int splineLength)
	{
		IVector2 possibleAccTarget = GeoMath.stepAlongLine(intersection, getPos(), -splineLength);
		List<IVector2> splineBasedNodes = new ArrayList<IVector2>();
		splineBasedNodes.add(possibleAccTarget);
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(Sisyphus.maxLinearVelocity, Sisyphus.maxLinearAcceleration);
		gen.setReducePathScore(0.0f);
		gen.setRotationTrajParams(Sisyphus.maxRotateVelocity, Sisyphus.maxRotateAcceleration);
		SplinePair3D spline = createSplineWithoutDrivingIt(bot, splineBasedNodes, getWorldFrame().getBot(bot.getId())
				.getAngle(), gen);
		float bot2intersect = GeoMath.distancePP(getPos(), intersection);
		return spline.getPositionTrajectory().lengthToTime(bot2intersect);
	}
	
	
	protected final SplinePair3D createSplineWithoutDrivingIt(final TrackedTigerBot bot, final List<IVector2> nodes,
			final float finalOrientation,
			final SplineTrajectoryGenerator gen)
	{
		List<IVector2> nodesMM = new ArrayList<IVector2>(nodes.size() + 1);
		nodesMM.add(convertAIVector2SplineNode(getPos()));
		
		// use position on spline instead of current position (which may be wrong due to delay)
		// if ((getPositionTraj() != null) && (getTrajectoryTime() < getPositionTraj().getTotalTime()))
		// {
		// nodesMM.add(getPositionTraj().getValueByTime(getTrajectoryTime()));
		// } else
		// {
		// nodesMM.add(DistanceUnit.MILLIMETERS.toMeters(bot.getPos()));
		// }
		
		for (IVector2 vec : nodes)
		{
			nodesMM.add(convertAIVector2SplineNode(vec));
		}
		
		return gen.create(nodesMM, getWorldFrame().getBot(bot.getId()).getVel(), AVector2.ZERO_VECTOR,
				convertAIAngle2SplineOrientation(getWorldFrame().getBot(bot.getId()).getAngle()),
				convertAIAngle2SplineOrientation(finalOrientation), getWorldFrame().getBot(bot.getId()).getaVel(), 0f);
	}
	
	
	private IVector2 convertAIVector2SplineNode(final IVector2 vec)
	{
		IVector2 mVec = DistanceUnit.MILLIMETERS.toMeters(vec);
		return mVec;
	}
	
	
	private float convertAIAngle2SplineOrientation(final float angle)
	{
		return angle;
	}
	
	
	/**
	 * @param ball
	 * @param intersection
	 * @return
	 */
	private float timeOfBallToIntersection(final TrackedBall ball, final IVector2 intersection)
	{
		float distanceBallIntersection = GeoMath.distancePP(ball.getPos(), intersection);
		float ballVel = DistanceUnit.METERS.toMillimeters(ball.getVel()).getLength2();
		float deacc = DistanceUnit.METERS.toMillimeters(deaccelerationOfBall);
		float pqBeforeSqrt = ballVel / deacc;
		float pqUnderSqrt = ((ballVel / deacc) * (ballVel / deacc)) - ((2 * distanceBallIntersection) / deacc);
		// ball will stop before it reaches the bot
		if (pqUnderSqrt <= 0)
		{
			return Float.MAX_VALUE;
		}
		return pqBeforeSqrt - ((float) Math.sqrt(pqUnderSqrt));
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
