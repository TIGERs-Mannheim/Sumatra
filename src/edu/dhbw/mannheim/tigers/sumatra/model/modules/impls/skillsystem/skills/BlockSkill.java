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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SplineMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.SplineGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.PositionSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.DebugShapeHacker;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


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
	@Configurable(comment = "Speed of the ball [m/s] - If the ball is faster the bot will throw himself into the shooting line.")
	private static float		blockDecisionVelocity				= 0.1f;
	
	@Configurable(comment = "Deacceleration of the ball [m/s^2]", spezis = { "", "GRSIM" })
	private static float		deaccelerationOfBall					= 1.0f;
	
	private final float		distToGoalLine;
	
	private final int			maxSplineLength;
	
	@Configurable(comment = "Chip duration for arming kicker")
	private static int		chipDuration							= 3000;
	
	@Configurable()
	private static int		dribbleSpeed							= 5000;
	
	@Configurable()
	private static boolean	keeperReactsOnAttackerPosition	= true;
	
	@Configurable(comment = "Distance [mm] - If an attacker is close to the ball than this the keeper will regard the orientation of the attacker")
	private static int		distanceBallAttacker					= 500;
	
	
	/**
	 * @param distToGoalCenter
	 * @param maxSplineLength
	 */
	public BlockSkill(final float distToGoalCenter, final int maxSplineLength)
	{
		super(ESkillName.BLOCK);
		distToGoalLine = distToGoalCenter;
		this.maxSplineLength = maxSplineLength;
		getMoveCon().setPenaltyAreaAllowedOur(true);
	}
	
	
	/**
	 * @param name
	 */
	protected BlockSkill(final ESkillName name)
	{
		super(name);
		distToGoalLine = 500;
		maxSplineLength = 3000;
		getMoveCon().setPenaltyAreaAllowedOur(true);
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		super.setDestination(calcDefendingDestination());
		super.setOrientation(calcDefendingOrientation());
		
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
		IVector2 destination;
		boolean overAccelerationNecessary = false;
		
		IVector2 goalCenter = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		IVector2 goalPostLeft = AIConfig.getGeometry().getGoalOur().getGoalPostLeft();
		IVector2 goalPostRight = AIConfig.getGeometry().getGoalOur().getGoalPostRight();
		IVector2 ballPos = getWorldFrame().getBall().getPosByTime(1);
		// calc the best block position to cover the middle area of the shooting area
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
			final IVector2 start;
			final IVector2 dir;
			start = ballPos;
			dir = getWorldFrame().getBall().getVel();
			
			List<IVector2> foeBots = new ArrayList<IVector2>();
			for (TrackedTigerBot foe : getWorldFrame().getFoeBots().values())
			{
				foeBots.add(foe.getPos());
			}
			IVector2 nearestFoe = GeoMath.nearestPointInList(foeBots, getWorldFrame().getBall().getPos());
			
			if (!dir.equals(AVector2.ZERO_VECTOR, blockDecisionVelocity) && (dir.x() != 0))
			{
				intersectPoint = GeoMath.intersectionPoint(start, dir, goalCenter, AVector2.Y_AXIS);
				
				// if the ball will not go into the goal
				// *2 because ball direction can be noisy (in reality)
				if ((Math.abs(intersectPoint.y()) > (((AIConfig.getGeometry().getGoalOur().getSize() / 2) + (2 * AIConfig
						.getGeometry().getBotRadius()))))) // || (getWorldFrame().getBall().getVel().x() > 0))
				{
					// block the shooting line to the middle of the goal
					intersectPoint = goalCenter;
				} else
				{
					if (ballPos.y() > getPos().y())
					{
						// block the shooting line to the point where the ball will cross the goal line
						overAccelerationNecessary = true;
					} else
					{
						// the ball is already behind the keeper, should not happen but it happens,
						if (getWorldFrame().getBall().getVel().getLength2() > 0.5f)
						{
							// very bad, the ball is on its way to the goal, no keeper and it has enough speed to reach the
							// goal
							// TODO think about a solution
						}
					}
				}
			} else if (keeperReactsOnAttackerPosition
					&& (nearestFoe.subtractNew(getWorldFrame().getBall().getPos()).getLength2() < distanceBallAttacker))
			{
				// enemy bot close to ball, check if he could hit the goal
				intersectPoint = GeoMath.intersectionPoint(
						Line.newLine(nearestFoe, ballPos),
						Line.newLine(AIConfig.getGeometry().getGoalOur().getGoalCenter(),
								AIConfig.getGeometry().getGoalOur()
										.getGoalPostLeft()));
				
				// check corner cases
				intersectPoint = new Vector2(intersectPoint.x(), Math.max(-(AIConfig
						.getGeometry().getGoalOur().getSize() / 2)
						+ (2 * AIConfig
								.getGeometry().getBotRadius()),
						intersectPoint.y()));
				intersectPoint = new Vector2(intersectPoint.x(), Math.min((AIConfig.getGeometry()
						.getGoalOur().getSize() / 2)
						- (2 * AIConfig
								.getGeometry().getBotRadius()),
						intersectPoint.y()));
				
			}
		} catch (MathException err)
		{
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
			if (((getPos().x() < (-(AIConfig.getGeometry().getFieldLength() / 2)))))
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
			DebugShapeHacker.addDebugShape(new DrawableCircle(new Circle(destination, 20)));
			destination = getAccelerationTarget(getWorldFrame().getTiger(getBot().getBotID()), destination);
		}
		DebugShapeHacker.addDebugShape(new DrawableCircle(new Circle(destination, 20), Color.BLUE));
		
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
	
	
	protected float calcDefendingOrientation()
	{
		return getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
	}
	
	
	/**
	 * @param bot
	 * @param intersection
	 * @return
	 */
	protected IVector2 getAccelerationTarget(final TrackedTigerBot bot, final IVector2 intersection)
	{
		float ballTime = timeOfBallToIntersection(getWorldFrame().getBall(), intersection);
		for (int i = maxSplineLength; i > (int) GeoMath.distancePP(getPos(), intersection); i = i - 100)
		{
			float timeOfBot = timeOfBotToIntersection(bot, intersection, i);
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
						return GeoMath.stepAlongLine(getPos(), intersection, j);
					}
				}
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
	protected float timeOfBotToIntersection(final TrackedTigerBot bot, final IVector2 intersection,
			final int splineLength)
	{
		IVector2 possibleAccTarget = GeoMath.stepAlongLine(intersection, getPos(), -splineLength);
		List<IVector2> splineBasedNodes = new ArrayList<IVector2>();
		splineBasedNodes.add(possibleAccTarget);
		SplineGenerator gen = new SplineGenerator(getBotType());
		SplinePair3D spline = gen.createSpline(bot, splineBasedNodes, getWorldFrame().getBot(bot.getId())
				.getAngle(), 0);
		float bot2intersect = GeoMath.distancePP(getPos(), intersection);
		return SplineMath.timeAfterDrivenWay(spline, bot2intersect, 0.05f);
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
	
	
	protected IVector2 convertAIVector2SplineNode(final IVector2 vec)
	{
		IVector2 mVec = DistanceUnit.MILLIMETERS.toMeters(vec);
		return mVec;
	}
	
	
	protected float convertAIAngle2SplineOrientation(final float angle)
	{
		return angle;
	}
	
	
	/**
	 * @param ball
	 * @param intersection
	 * @return
	 */
	protected float timeOfBallToIntersection(final TrackedBall ball, final IVector2 intersection)
	{
		// TODO Use function from Mark in TrackedBall
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
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
		super.doCalcEntryActions(cmds);
		// getDevices().chip(cmds, new ChipParams(chipDuration, 0), EKickerMode.ARM);
		getDevices().dribble(cmds, dribbleSpeed);
	}
}
