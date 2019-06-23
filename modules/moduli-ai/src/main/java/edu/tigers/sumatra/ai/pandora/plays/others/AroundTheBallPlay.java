/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.others;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.Goal;


/**
 * All available Robots shall move on a circle around the ball-position.
 * 
 * @author Malte, OliverS
 */
public class AroundTheBallPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Goal	goal		= Geometry.getGoalOur();
											
	private final double	radius	= Geometry.getBotToBallDistanceStop();
											
											
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public AroundTheBallPlay()
	{
		super(EPlay.AROUND_THE_BALL);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void doUpdate(final AthenaAiFrame currentFrame)
	{
		IVector2 ballPos = new Vector2(currentFrame.getWorldFrame().getBall().getPos());
		
		// direction: vector from ball to the middle of the goal!
		Vector2 direction = goal.getGoalCenter().subtractNew(ballPos);
		// sets the length of the vector to 'radius'
		direction.scaleTo(radius);
		
		double turn = AngleMath.PI / 2.0;
		
		for (final ARole role : getRoles())
		{
			final MoveRole moveRole = (MoveRole) role;
			
			final IVector2 destination = ballPos.addNew(direction.turnNew(turn));
			
			moveRole.getMoveCon().setPenaltyAreaAllowedOur(true);
			moveRole.getMoveCon().updateDestination(destination);
			turn -= AngleMath.PI / 5.0;
		}
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		return (new MoveRole(EMoveBehavior.LOOK_AT_BALL));
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
