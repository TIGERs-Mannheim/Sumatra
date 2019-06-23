/*
 * *********************************************************
 * # * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 2, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Skill to intercept ball.
 * 
 * @author Philipp Posovszky <ph.posovszky@gmail.com>
 * 
 */
public class MoveAndBlockV2Skill extends AMoveSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log			= Logger.getLogger(MoveAndBlockV2Skill.class.getName());
	
	private ILine						shootLine	= null;
	
	/**
	 * To Choose the Modus for the MoveAndBlockSkill
	 * 
	 * @author PhilippP {ph.posovszky@gmail.com}
	 * 
	 */
	public enum EBlockModus
	{
		/**  */
		KEEPER_INTERSEC,
		/**  */
		DEFENDER
	}
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private EBlockModus	modus;
	private IVector2		destinationOld	= new Vector2(0, 0);
	
	
	/**
	 * @param modus
	 */
	public MoveAndBlockV2Skill(EBlockModus modus)
	{
		super(ESkillName.MOVE_AND_BLOCK);
		this.modus = modus;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds)
	{
		calcSkill(bot, cmds);
	}
	
	
	@Override
	protected boolean isComplete(TrackedTigerBot bot)
	{
		return false;
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		// TODO: 1000 In config auslagenn
		getDevices().chipRoll(cmds, 1000);
		
		calcSkill(bot, cmds);
		
		return cmds;
	}
	
	
	/**
	 * Calculate the skill
	 * 
	 * @param bot
	 * @param cmds
	 */
	private void calcSkill(TrackedTigerBot bot, List<ACommand> cmds)
	{
		IVector2 destination;
		if (((bot.getPos().x() > -2300) || (bot.getPos().x() < -3150)) && (modus == EBlockModus.KEEPER_INTERSEC))
		{
			destination = new Vector2(AIConfig.getGeometry().getGoalOur().getGoalCenter()
					.addNew(Vector2.X_AXIS.scaleToNew(300)));
		} else
		{
			
			if ((getWorldFrame().ball != null) && (getWorldFrame().ball.getPos().x() > -3025))
			{
				IVector2 intersectPoint = AIConfig.getGeometry().getGoalOur().getGoalCenter();
				try
				{
					final IVector2 start;
					final IVector2 dir;
					if (shootLine != null)
					{
						start = shootLine.supportVector();
						dir = shootLine.directionVector();
					} else
					{
						start = getWorldFrame().getBall().getPos();
						dir = getWorldFrame().getBall().getVel();
					}
					if (!dir.equals(Vector2.ZERO_VECTOR, 0.1f) && (dir.x() != 0))
					{
						intersectPoint = GeoMath.intersectionPoint(start, dir, bot.getPos(), AVector2.Y_AXIS);
						// TODO MAGICE NUMBER
						if (Math.abs(intersectPoint.y()) > ((AIConfig.getGeometry().getGoalOur().getSize() / 2) + 200))
						{
							intersectPoint = AIConfig.getGeometry().getGoalOur().getGoalCenter();
						}
					}
				} catch (MathException err)
				{
					log.warn("", err);
					return;
				}
				destination = GeoMath.leadPointOnLine(bot.getPos(), getWorldFrame().ball.getPos(), intersectPoint);
				
				float distance = GeoMath.distancePP(destination, bot.getPos());
				
				if (distance < AIConfig.getGeometry().getBotRadius())
				{
					destination = GeoMath.stepAlongLine(AIConfig.getGeometry().getGoalOur().getGoalCenter(), destination,
							300);
				}
				
				// TODO erst auf linie fahren, dann entfernung prüfen und wenn <300 radius erhöhen
				// if (AIConfig.getGeometry().getGoalOur().getGoalCenter().equals(destination, 300))
				// {
				// destination = GeoMath.stepAlongLine(destination, AIConfig.getGeometry().getGoalOur().getGoalCenter(),
				// -300);
				// }
				if (modus == EBlockModus.DEFENDER)
				{
					destination = AIConfig.getGeometry().getPenaltyAreaOur()
							.nearestPointOutside(new Vector2(destination), bot.getPos());
				}
				if (destination == null)
				{
					destination = intersectPoint;
				}
				
			} else
			{
				destination = new Vector2(AIConfig.getGeometry().getGoalOur().getGoalCenter()
						.addNew(Vector2.X_AXIS.scaleToNew(300)));
			}
		}
		
		if (!destination.equals(destinationOld, 5))
		{
			destinationOld = destination;
			generateSpline(bot, destination);
		}
		
		// if (bot.getPos().equals(intersectPoint, 90))
		// {
		// SplineTrajectoryGenerator gen = createDefaultGenerator(bot);
		// gen.setRotationTrajParams(AIConfig.getSkills(bot.getBotType()).getMaxRotateVelocity(),
		// AIConfig.getSkills(bot.getBotType()).getMaxRotateAcceleration());
		// List<IVector2> nodes = new LinkedList<IVector2>();
		// nodes.add(intersectPoint);
		// createSpline(bot, nodes, getWorldFrame().ball.getPos(), gen);
		// log.warn("Move to moving ball stopped");
		// stopMove(cmds);
		// }
	}
	
	
	/**
	 * Generate a new spline
	 * @param bot
	 * 
	 */
	private void generateSpline(TrackedTigerBot bot, IVector2 destination)
	{
		SplineTrajectoryGenerator gen = createDefaultGenerator(bot);
		gen.setRotationTrajParams(AIConfig.getSkills(bot.getBotType()).getMaxRotateVelocity(),
				AIConfig.getSkills(bot.getBotType()).getMaxRotateAcceleration());
		List<IVector2> nodes = new LinkedList<IVector2>();
		
		// IVector2 secondPoint = GeoMath.stepAlongLine(destination, bot.getPos(), -100);
		nodes.add(destination);
		// nodes.add(getAccelerationTarget(bot, destination));
		createSpline(bot, nodes, getWorldFrame().ball.getPos(), gen);
	}
	
	
	@SuppressWarnings("unused")
	private IVector2 getAccelerationTarget(TrackedTigerBot bot, IVector2 intersection)
	{
		int maxSplineLength = 2000;
		float ballTime = timeOfBallToIntersection(getWorldFrame().getBall(), intersection);
		for (int i = (int) GeoMath.distancePP(bot.getPos(), intersection); i < maxSplineLength; i++)
		{
			float timeOfBot = timeOfBotToIntersection(bot, intersection, i);
			if (timeOfBot < ballTime)
			{
				return GeoMath.stepAlongLine(intersection, bot.getPos(), -i);
			}
		}
		return GeoMath.stepAlongLine(intersection, bot.getPos(), -maxSplineLength);
	}
	
	
	private float timeOfBotToIntersection(TrackedTigerBot bot, IVector2 intersection, int splineLength)
	{
		IVector2 possibleAccTarget = GeoMath.stepAlongLine(intersection, bot.getPos(), -splineLength);
		List<IVector2> splineBasedNodes = new ArrayList<IVector2>();
		splineBasedNodes.add(possibleAccTarget);
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(AIConfig.getSkills(bot.getBotType()).getMaxLinearVelocity(),
				AIConfig.getSkills(bot.getBotType()).getMaxLinearAcceleration());
		gen.setReducePathScore(0.0f);
		gen.setRotationTrajParams(AIConfig.getSkills(bot.getBotType()).getMaxRotateVelocity(),
				AIConfig.getSkills(bot.getBotType()).getMaxRotateAcceleration());
		SplinePair3D spline = createSplineWithoutDrivingIt(bot, splineBasedNodes, bot.getAngle(), gen);
		float bot2intersect = GeoMath.distancePP(bot.getPos(), intersection);
		return spline.getPositionTrajectory().lengthToTime(bot2intersect);
	}
	
	
	private float timeOfBallToIntersection(TrackedBall ball, IVector2 intersection)
	{
		float distanceBallIntersection = GeoMath.distancePP(ball.getPos(), intersection);
		float ballVel = DistanceUnit.METERS.toMillimeters(ball.getVel()).getLength2();
		return distanceBallIntersection / ballVel;
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		return cmds;
	}
	
	
	/**
	 * @return the shootLine
	 */
	public final ILine getShootLine()
	{
		return shootLine;
	}
	
	
	/**
	 * @param shootLine the shootLine to set
	 */
	public final void setShootLine(ILine shootLine)
	{
		this.shootLine = shootLine;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
