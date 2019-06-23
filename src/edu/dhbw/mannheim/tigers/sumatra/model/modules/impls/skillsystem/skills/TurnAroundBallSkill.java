/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Move the ball to a destination
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class TurnAroundBallSkill extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	BALL_EQUALS_TOLERANCE	= 50;
	private static final int	PATH_POINTS					= 4;
	private IVector2				lookAtTarget;
	private IVector2				lastBallPos;
	private final float			angle;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param lookAtTarget
	 */
	public TurnAroundBallSkill(IVector2 lookAtTarget)
	{
		super(ESkillName.TURN_AROUND_BALL);
		this.lookAtTarget = lookAtTarget;
		angle = 0;
	}
	
	
	/**
	 * @param angle
	 */
	public TurnAroundBallSkill(float angle)
	{
		super(ESkillName.TURN_AROUND_BALL);
		lookAtTarget = null;
		this.angle = angle;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds)
	{
		if (!lastBallPos.equals(getWorldFrame().ball.getPos(), BALL_EQUALS_TOLERANCE))
		{
			calcSpline(bot);
		}
	}
	
	
	private void calcSpline(TrackedTigerBot bot)
	{
		lastBallPos = getWorldFrame().ball.getPos();
		
		if (lookAtTarget == null)
		{
			calcSpline(bot, angle);
		} else
		{
			calcSpline(bot, lookAtTarget);
		}
	}
	
	
	private void calcSpline(TrackedTigerBot bot, float angle)
	{
		final List<IVector2> nodes = new LinkedList<IVector2>();
		IVector2 start = bot.getPos();
		float minDist = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius()
				+ AIConfig.getTolerances(getBot().getBotType()).getNextToBall();
		if (GeoMath.distancePP(bot.getPos(), getWorldFrame().ball.getPos()) < minDist)
		{
			start = GeoMath.stepAlongLine(getWorldFrame().ball.getPos(), bot.getPos(), minDist);
			nodes.add(start);
		}
		
		for (int i = 0; i < PATH_POINTS; i++)
		{
			nodes.add(GeoMath.stepAlongCircle(start, getWorldFrame().ball.getPos(), (((angle) / (PATH_POINTS - 1)) * i)));
		}
		
		lookAtTarget = GeoMath.stepAlongLine(nodes.get(nodes.size() - 1), getWorldFrame().ball.getPos(), AIConfig
				.getGeometry().getBotRadius() * 5);
		
		createSpline(bot, nodes, lookAtTarget);
	}
	
	
	private void calcSpline(TrackedTigerBot bot, IVector2 lookAt)
	{
		final List<IVector2> nodes = new LinkedList<IVector2>();
		IVector2 v1 = bot.getPos().subtractNew(getWorldFrame().ball.getPos());
		IVector2 v2 = GeoMath.stepAlongLine(getWorldFrame().ball.getPos(), lookAt, -1).subtract(
				getWorldFrame().ball.getPos());
		final float turnAngle = GeoMath.angleBetweenVectorAndVectorWithNegative(v2, v1);
		
		float minRadius = AIConfig.getGeometry().getBotRadius()
				+ AIConfig.getGeneral(getBot().getBotType()).getPositioningPostAiming();
		IVector2 start = bot.getPos();
		if (GeoMath.distancePP(bot.getPos(), getWorldFrame().ball.getPos()) < minRadius)
		{
			start = GeoMath.stepAlongLine(getWorldFrame().ball.getPos(), bot.getPos(), minRadius);
		}
		
		for (int i = 0; i < PATH_POINTS; i++)
		{
			nodes.add(GeoMath.stepAlongCircle(start, getWorldFrame().ball.getPos(),
					(((turnAngle) / (PATH_POINTS - 1)) * i)));
		}
		
		createSpline(bot, nodes, lookAt);
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		calcSpline(bot);
		return cmds;
	}
	
	
	@Override
	public List<ACommand> doCalcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		stopMove(cmds);
		return cmds;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
