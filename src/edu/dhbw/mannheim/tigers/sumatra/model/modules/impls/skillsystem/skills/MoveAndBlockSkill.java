/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
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
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Skill to intercept ball.
 * 
 * @author Philipp Posovszky <ph.posovszky@gmail.com>
 * 
 */
public class MoveAndBlockSkill extends AMoveSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log	= Logger.getLogger(MoveAndBlockSkill.class.getName());
	
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
	private IVector2		intersectPoint;
	private EBlockModus	modus;
	private MovementCon	moveCon;
	private IVector2		destination;
	private IVector2		destinationOld	= new Vector2(0, 0);
	
	
	/**
	 * @param moveCon
	 * @param intersectPoint
	 * @param modus
	 */
	public MoveAndBlockSkill(MovementCon moveCon, IVector2 intersectPoint, EBlockModus modus)
	{
		super(ESkillName.MOVE_AND_BLOCK);
		this.modus = modus;
		this.intersectPoint = intersectPoint;
		this.moveCon = moveCon;
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
		if (!destination.equals(moveCon.getDestCon().getDestination(), 40))
		{
			destination = moveCon.getDestCon().getDestination();
			calcSkill(bot, cmds);
		}
	}
	
	
	@Override
	protected boolean isComplete(TrackedTigerBot bot)
	{
		return false;
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		// TODO: 1000 In config auslagen inklusive das mit defender nicht chippen
		if (modus != EBlockModus.DEFENDER)
		{
			getDevices().chipRoll(cmds, 1000);
		}
		
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
		destination = GeoMath.leadPointOnLine(bot.getPos(), getWorldFrame().ball.getPos(), intersectPoint);
		
		if (modus == EBlockModus.DEFENDER)
		{
			destination = AIConfig.getGeometry().getPenaltyAreaOur()
					.nearestPointOutside(new Vector2(destination), bot.getPos());
		}
		if (destination == null)
		{
			destination = intersectPoint;
		}
		
		if (!destination.equals(destinationOld, 5))
		{
			generateSpline(bot);
		}
		
		if (bot.getPos().equals(intersectPoint, 90))
		{
			// SplineTrajectoryGenerator gen = createDefaultGenerator(bot);
			// gen.setRotationTrajParams(AIConfig.getSkills(bot.getBotType()).getMaxRotateVelocity(),
			// AIConfig.getSkills(bot.getBotType()).getMaxRotateAcceleration());
			// List<IVector2> nodes = new LinkedList<IVector2>();
			// nodes.add(intersectPoint);
			// createSpline(bot, nodes, getWorldFrame().ball.getPos(), gen);
			log.warn("Move to moving ball stopped");
			stopMove(cmds);
		}
		
		destinationOld = destination;
	}
	
	
	/**
	 * Generate a new spline
	 * @param bot
	 * 
	 */
	private void generateSpline(TrackedTigerBot bot)
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
	 * @return the intersectPoint
	 */
	public final IVector2 getIntersectPoint()
	{
		return intersectPoint;
	}
	
	
	/**
	 * @param intersectPoint the intersectPoint to set
	 */
	public final void setIntersectPoint(IVector2 intersectPoint)
	{
		this.intersectPoint = intersectPoint;
	}
	
	
	/**
	 * @return the destination
	 */
	public final IVector2 getDestination()
	{
		return destination;
	}
	
	
	/**
	 * @param destination the destination to set
	 */
	public final void setDestination(IVector2 destination)
	{
		this.destination = destination;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
