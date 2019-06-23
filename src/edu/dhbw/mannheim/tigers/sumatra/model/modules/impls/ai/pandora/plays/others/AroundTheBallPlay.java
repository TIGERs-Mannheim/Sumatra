/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * All available Robots shall move on a circle around the ball-position.
 * (@see {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveWithDistanceToPointRole}
 * )
 * @author Malte, OliverS
 * 
 */
public class AroundTheBallPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Goal	goal		= AIConfig.getGeometry().getGoalOur();
	
	private final float	radius	= AIConfig.getGeometry().getBotToBallDistanceStop();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public AroundTheBallPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		setTimeout(Long.MAX_VALUE);
		
		for (int i = 0; i < getNumAssignedRoles(); i++)
		{
			addAggressiveRole(new MoveRole(EMoveBehavior.LOOK_AT_BALL), aiFrame.worldFrame.ball.getPos());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		IVector2 ballPos = new Vector2(currentFrame.worldFrame.ball.getPos());
		
		// direction: vector from ball to the middle of the goal!
		Vector2 direction = goal.getGoalCenter().subtractNew(ballPos);
		// sets the length of the vector to 'radius'
		direction.scaleTo(radius);
		
		float turn = AngleMath.PI / 2;
		
		for (final ARole role : getRoles())
		{
			final MoveRole moveRole = (MoveRole) role;
			
			final IVector2 destination = ballPos.addNew(direction.turnNew(turn));
			
			moveRole.updateDestination(destination);
			turn -= AngleMath.PI / 5;
		}
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
