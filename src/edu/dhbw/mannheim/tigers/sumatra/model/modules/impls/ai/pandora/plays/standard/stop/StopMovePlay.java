/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.01.2011
 * Author(s): FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.stop;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.AStandardPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveWithDistanceToPointRole;


/**
 * n bots will form a block in a distance of 500 mm to the ball in order to
 * protect the goal after the game is stopped by the referee
 * @author FlorianS
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class StopMovePlay extends AStandardPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	SPACE_BETWEEN_BOTS	= 30;
	
	private final float			botRadius				= AIConfig.getGeometry().getBotRadius();
	private final float			stopRadius				= AIConfig.getGeometry().getCenterCircleRadius() + botRadius
																			+ AIConfig.getGeometry().getBallRadius() + 50;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public StopMovePlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		List<IVector2> directions = getDirections(aiFrame, numAssignedRoles);
		for (int i = 0; i < getNumAssignedRoles(); i++)
		{
			final MoveWithDistanceToPointRole role = new MoveWithDistanceToPointRole(aiFrame.worldFrame.ball.getPos(),
					stopRadius, directions.get(i));
			IVector2 dest = aiFrame.worldFrame.ball.getPos().addNew(directions.get(i).scaleToNew(stopRadius));
			addAggressiveRole(role, dest);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		if (currentFrame.worldFrame.ball.getVel().getLength2() < 0.2)
		{
			return;
		}
		
		final IVector2 ballPos = currentFrame.worldFrame.ball.getPos();
		List<IVector2> directions = getDirections(currentFrame, getRoleCount());
		
		for (int i = 0; i < getRoleCount(); i++)
		{
			final MoveWithDistanceToPointRole role = (MoveWithDistanceToPointRole) getRoles().get(i);
			IVector2 dir = directions.get(i);
			role.updateCirclePos(ballPos, stopRadius, dir);
		}
	}
	
	
	private List<IVector2> getDirections(AIInfoFrame currentFrame, int count)
	{
		final Vector2f goalCenterOur = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		final IVector2 ballPos = currentFrame.worldFrame.ball.getPos();
		final float turnAngle = (float) Math.acos(((2 * stopRadius * stopRadius) - (Math.pow((2 * botRadius)
				+ SPACE_BETWEEN_BOTS, 2)))
				/ (2 * stopRadius * stopRadius));
		
		// vector from ball to the middle of the goal
		IVector2 direction = goalCenterOur.subtractNew(ballPos);
		
		final float turnAngleStart = (-turnAngle / 2) * (count - 1);
		
		List<IVector2> directions = new ArrayList<IVector2>(count);
		for (int i = 0; i < count; i++)
		{
			directions.add(direction.turnNew(turnAngleStart + (i * turnAngle)));
		}
		return directions;
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
