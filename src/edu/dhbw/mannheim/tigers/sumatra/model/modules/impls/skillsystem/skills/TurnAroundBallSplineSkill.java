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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Turn around the ball, keeping current distance and looking through the ball
 * to the specified target or turning by specified angle
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class TurnAroundBallSplineSkill extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	BALL_EQUALS_TOLERANCE	= 50;
	private static final int	PATH_POINTS					= 4;
	private IVector2				lookAtTarget;
	private IVector2				lastBallPos;
	private final float			angle;
	
	
	@Configurable(comment = "Dist [mm] - distance to ball after aiming")
	private static float			positioningPostAiming	= 70;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param lookAtTarget
	 */
	public TurnAroundBallSplineSkill(IVector2 lookAtTarget)
	{
		super(ESkillName.TURN_AROUND_BALL_SPLINE);
		this.lookAtTarget = lookAtTarget;
		angle = 0;
	}
	
	
	/**
	 * @param angle
	 */
	public TurnAroundBallSplineSkill(float angle)
	{
		super(ESkillName.TURN_AROUND_BALL_SPLINE);
		lookAtTarget = null;
		this.angle = angle;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void periodicProcess(List<ACommand> cmds)
	{
		if (!lastBallPos.equals(getWorldFrame().getBall().getPos(), BALL_EQUALS_TOLERANCE))
		{
			calcSpline();
		}
	}
	
	
	private void calcSpline()
	{
		lastBallPos = getWorldFrame().getBall().getPos();
		
		if (lookAtTarget == null)
		{
			calcSpline(angle);
		} else
		{
			calcSpline(lookAtTarget);
		}
	}
	
	
	private void calcSpline(float angle)
	{
		final List<IVector2> nodes = new LinkedList<IVector2>();
		IVector2 start = getPos();
		float minDist = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius() + 30;
		if (GeoMath.distancePP(getPos(), getWorldFrame().getBall().getPos()) < minDist)
		{
			start = GeoMath.stepAlongLine(getWorldFrame().getBall().getPos(), getPos(), minDist);
			nodes.add(start);
		}
		
		for (int i = 0; i < PATH_POINTS; i++)
		{
			nodes.add(GeoMath.stepAlongCircle(start, getWorldFrame().getBall().getPos(),
					(((angle) / (PATH_POINTS - 1)) * i)));
		}
		
		lookAtTarget = GeoMath.stepAlongLine(nodes.get(nodes.size() - 1), getWorldFrame().getBall().getPos(), AIConfig
				.getGeometry().getBotRadius() * 5);
		
		createSpline(nodes, lookAtTarget);
	}
	
	
	private void calcSpline(IVector2 lookAt)
	{
		final List<IVector2> nodes = new LinkedList<IVector2>();
		IVector2 v1 = getPos().subtractNew(getWorldFrame().getBall().getPos());
		IVector2 v2 = GeoMath.stepAlongLine(getWorldFrame().getBall().getPos(), lookAt, -1).subtract(
				getWorldFrame().getBall().getPos());
		final float turnAngle = GeoMath.angleBetweenVectorAndVectorWithNegative(v2, v1);
		
		float minRadius = AIConfig.getGeometry().getBotRadius() + positioningPostAiming;
		IVector2 start = getPos();
		if (GeoMath.distancePP(getPos(), getWorldFrame().getBall().getPos()) < minRadius)
		{
			start = GeoMath.stepAlongLine(getWorldFrame().getBall().getPos(), getPos(), minRadius);
		}
		
		for (int i = 0; i < PATH_POINTS; i++)
		{
			nodes.add(GeoMath.stepAlongCircle(start, getWorldFrame().getBall().getPos(),
					(((turnAngle) / (PATH_POINTS - 1)) * i)));
		}
		
		createSpline(nodes, lookAt);
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(List<ACommand> cmds)
	{
		calcSpline();
		return cmds;
	}
	
	
	@Override
	public List<ACommand> doCalcExitActions(List<ACommand> cmds)
	{
		stopMove(cmds);
		return cmds;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
