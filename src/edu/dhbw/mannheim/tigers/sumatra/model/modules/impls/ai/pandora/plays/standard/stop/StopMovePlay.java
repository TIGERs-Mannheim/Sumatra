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

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.MoveRole;


/**
 * Two bots will form a block in a distance of 500 mm to the ball in order to
 * protect the goal after the game is stopped by the referee
 * @author FlorianS
 * 
 */
public class StopMovePlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID		= -3177342585857911649L;
	private final Goal			goal						= AIConfig.getGeometry().getGoalOur();
	private final Vector2f		US_GOAL_MID				= goal.getGoalCenter();
	private final float			BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
	private final float			SPACE_BETWEEN_BOTS	= AIConfig.getPlays().getPositioningOnStoppedPlayWithTwo()
																			.getSpaceBetweenBots();
	
	private Vector2				ballPos;
	private Vector2				direction;
	
	private float					radius;
	private float					turnAngle;
	
	private MoveRole				leftBlocker;
	private MoveRole				rightBlocker;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public StopMovePlay(AIInfoFrame aiFrame)
	{
		super(EPlay.STOP_MOVE, aiFrame);
		Vector2 initPos = new Vector2(AIConfig.getGeometry().getCenter());
		
		leftBlocker = new MoveRole();
		addAggressiveRole(leftBlocker, initPos.addY(500));
		rightBlocker = new MoveRole();
		addAggressiveRole(rightBlocker, initPos.addY(-1000));
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		radius = BOT_RADIUS + 500;
		ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
		turnAngle = (float) Math.acos(((2 * radius * radius) - (Math.pow(2 * BOT_RADIUS + SPACE_BETWEEN_BOTS, 2)))
				/ (2 * radius * radius));
		
		// vector from ball to the middle of the goal
		direction = US_GOAL_MID.subtractNew(ballPos);
		
		leftBlocker.updateCirclePos(ballPos, radius, direction.turnNew(-turnAngle / 2));
		rightBlocker.updateCirclePos(ballPos, radius, direction.turnNew(turnAngle / 2));
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
