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
import java.util.ArrayList;
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
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
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
public class BlockSkillTrajV2 extends MoveToTrajSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@Configurable(comment = "Speed of the ball [m/s] - If the ball is faster the bot will throw himself into the shooting line.")
	private static float		blockDecisionVelocity				= 0.01f;
	
	@Configurable(comment = "Deacceleration of the ball [m/s^2]", spezis = { "", "GRSIM" })
	private static float		deaccelerationOfBall					= 1.0f;
	
	@Configurable(comment = "Max Velocity reached -> Acceleration zero tolerance")
	private static float		accTolerance							= 10;
	
	
	@Configurable(comment = "Chip duration for arming kicker")
	private static int		chipDuration							= 3000;
	
	@Configurable()
	private static int		dribbleSpeed							= 5000;
	
	@Configurable()
	private static boolean	keeperReactsOnAttackerPosition	= true;
	
	@Configurable(comment = "Distance [mm] - If an attacker is close to the ball than this the keeper will regard the orientation of the attacker")
	private static int		distanceBallAttacker					= 500;
	
	@Configurable(comment = "Time to lookahead")
	private static float		timeLookAhead							= 0.5f;
	
	
	private static float		distToGoalCenter						= 500;
	
	
	/**
	 * @param distToGoalCenter
	 */
	public BlockSkillTrajV2(final float distToGoalCenter)
	{
		super(ESkillName.BLOCKTRAJV2);
		BlockSkillTrajV2.distToGoalCenter = distToGoalCenter;
		getMoveCon().setPenaltyAreaAllowedOur(true);
	}
	
	
	/**
	 * @param name
	 */
	protected BlockSkillTrajV2(final ESkillName name)
	{
		super(name);
		getMoveCon().setPenaltyAreaAllowedOur(true);
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		IVector2 nextDestination = calcDefendingDestination();
		float nextOrientation = calcDefendingOrientation();
		if ((driver.getPath() == null) || (!(driver.getPath().getFinalDestination().equals(nextDestination, 1))
				|| !AngleMath.isEqual(driver.getPath().getOrientation(driver.getPath().getTotalTime()), nextOrientation)))
		{
			obstacleGen.setUseBall(getMoveCon().isBallObstacle());
			obstacleGen.setUseBots(getMoveCon().isBotsObstacle());
			obstacleGen.setUsePenAreaOur(!getMoveCon().isPenaltyAreaAllowedOur());
			obstacleGen.setUsePenAreaTheir(!getMoveCon().isPenaltyAreaAllowedTheir());
			obstacleGen.setUseGoalPostsOur(getMoveCon().isGoalPostObstacle());
			
			List<IObstacle> obstacles = obstacleGen.generateObstacles(getWorldFrame(), getBot().getBotID());
			finderInput.setObstacles(obstacles);
			
			finderInput.setTrackedBot(getTBot());
			finderInput.setDest(nextDestination);
			finderInput.setTargetAngle(nextOrientation);
			final TrajPathFinderInput localInput = new TrajPathFinderInput(finderInput);
			getBot().getPathFinder().calcPath(localInput);
			TrajPath path = getBot().getPathFinder().getCurPath();
			driver.setPath(path);
		}
		
		if (GeoMath.distancePP(getWorldFrame().getBall().getPosByTime(timeLookAhead), getPos()) > 500)
		{
			getDevices().dribble(cmds, false);
		} else
		{
			getDevices().dribble(cmds, dribbleSpeed);
		}
	}
	
	
	@SuppressWarnings("null")
	protected IVector2 calcDefendingDestination()
	{
		Color color = Color.CYAN;
		List<BotID> ignoredBots = new ArrayList<BotID>();
		ignoredBots.add(getTBot().getId());
		IVector2 destination = null;
		try
		{
			// Some Stuff for clear code
			final float botRadius = AIConfig.getGeometry().getBotRadius();
			final TrackedBall ball = getWorldFrame().getBall();
			final IVector2 ballPos = ball.getPosByTime(timeLookAhead);
			// final IVector2 goalCenter = AIConfig.getGeometry().getGoalOur().getGoalCenter();
			final IVector2 goalCenterPlusBotRadius = new Vector2(
					AIConfig.getGeometry().getGoalOur().getGoalCenter().x() +
							botRadius,
					0);
			final IVector2 goalPostLeft = AIConfig.getGeometry().getGoalOur().getGoalPostLeft();
			// final IVector2 goalPostRight = AIConfig.getGeometry().getGoalOur().getGoalPostRight();
			IVector2 intersectPoint = null;
			
			// Cover GoalCenter
			destination = GeoMath.stepAlongLine(checkCornerCases(GeoMath.intersectionPoint(ballPos,
					ballPos.subtractNew(AIConfig.getGeometry().getGoalOur().getGoalCenter()), goalCenterPlusBotRadius,
					AVector2.Y_AXIS)), ballPos, distToGoalCenter);
			
			
			// Balls Velocity is directed to Goal
			if (ball.getVel().getLength2() > blockDecisionVelocity)
			{
				intersectPoint = GeoMath.intersectionPoint(ballPos, ball.getVel(), goalCenterPlusBotRadius,
						AVector2.Y_AXIS);
			}
			
			// Ball intersect Goal //TODO Direction
			if ((intersectPoint != null) && ((Math.abs(intersectPoint.y())) < (goalPostLeft.y() + (botRadius * 2)))
					&& (ball.getVel().getLength2() > blockDecisionVelocity) && (ball.getVel().y() < 0))
			{
				color = Color.BLACK;
				destination = GeoMath.leadPointOnLine(getPos(), new Line(ballPos, ball.getVel()));
				if (destination.x() < goalCenterPlusBotRadius.x())
				{
					destination = intersectPoint;
				}
				destination = calcAccelerationTarget(getWorldFrame().getTiger(getBot().getBotID()), destination);
			} else
			{// <<<<<<<<<<FOE redirect>>>>>>>>>>
				TrackedBot recieverFOE = getWorldFrame()
						.getFoeBot(OffensiveMath.getBestRedirector(getWorldFrame(), getWorldFrame().getFoeBots()));
				if (recieverFOE != null)
				{
					color = Color.GRAY;
					// Intersection of FOE Position and Goal
					destination = checkCornerCases(
							GeoMath.intersectionPoint(recieverFOE.getPos(),
									new Vector2(recieverFOE.getPos()
											.subtractNew(AIConfig.getGeometry().getGoalOur().getGoalCenter())),
									goalCenterPlusBotRadius, AVector2.Y_AXIS));
					
					destination = checkCornerCases(
							GeoMath.stepAlongLine(destination, recieverFOE.getPos(), distToGoalCenter));
				}
			}
			DebugShapeHacker.addDebugShape(new DrawableCircle(new Circle(destination, 20), color));
		} catch (MathException err)
		{
			destination = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		}
		
		return destination;
	}
	
	
	protected float calcDefendingOrientation()
	{
		return getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
	}
	
	
	private IVector2 checkCornerCases(IVector2 possibleIntersectPoint)
	{
		final IVector2 goalPostLeft = AIConfig.getGeometry().getGoalOur().getGoalPostLeft();
		final IVector2 goalPostRight = AIConfig.getGeometry().getGoalOur().getGoalPostRight();
		// check corner cases
		if (possibleIntersectPoint.y() > goalPostLeft.y())
		{
			possibleIntersectPoint = new Vector2(goalPostLeft.x() + AIConfig.getGeometry().getBotRadius(),
					goalPostLeft.y());
		} else if (possibleIntersectPoint.y() < goalPostRight.y())
		{
			possibleIntersectPoint = new Vector2(goalPostRight.x() + AIConfig.getGeometry().getBotRadius(),
					goalPostRight.y());
		}
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
		
		if ((((traj.getTotalTime()) + timeLookAhead) <= getWorldFrame().getBall().getTimeByPos(intersection)))
		{
			return intersection;
		}
		return intersection;// GeoMath.stepAlongLine(getPos(), intersection, 1500);
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
