/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.01.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.stop;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.MoveRole;


/**
 * Three bots will form a block in a distance of 500 mm to the ball in order to
 * protect the goal after the game is stopped by the referee
 * @author FlorianS
 * 
 */
public class PositioningOnStoppedPlayWithThreePlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID		= -8637943335063512634L;
	private final Goal			goal						= AIConfig.getGeometry().getGoalOur();
	private final Vector2f		US_GOAL_MID				= goal.getGoalCenter();
	private final float			BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
	private final float			SPACE_BETWEEN_BOTS	= AIConfig.getPlays().getPositioningOnStoppedPlayWithThree()
																			.getSpaceBetweenBots();
	
	private IVector2				ballPos;
	private Vector2				direction;
	
	private float					radius;
	private float					turnAngle;
	
	private MoveRole				centerBlocker;
	private MoveRole				leftBlocker;
	private MoveRole				rightBlocker;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public PositioningOnStoppedPlayWithThreePlay(AIInfoFrame aiFrame)
	{
		super(EPlay.POSITIONING_ON_STOPPED_PLAY_WITH_THREE, aiFrame);
		Vector2 initPos = new Vector2(AIConfig.getGeometry().getCenter());
		
		centerBlocker = new MoveRole();
		leftBlocker = new MoveRole();
		rightBlocker = new MoveRole();
		
		addAggressiveRole(centerBlocker, AIConfig.getGeometry().getCenter());
		addAggressiveRole(leftBlocker, initPos.addX(500));
		addAggressiveRole(rightBlocker, initPos.addY(-1000));
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		radius = BOT_RADIUS + 500;
		ballPos = currentFrame.worldFrame.ball.pos;
		turnAngle = (float) Math.acos(((2 * radius * radius) - (Math.pow(4 * BOT_RADIUS + 2 * SPACE_BETWEEN_BOTS, 2)))
				/ (2 * radius * radius));
		
		// vector from ball to the middle of the goal
		direction = US_GOAL_MID.subtractNew(ballPos);
		
		centerBlocker.updateCirclePos(ballPos, radius, direction);
		leftBlocker.updateCirclePos(ballPos, radius, direction.turnNew(-turnAngle / 2));
		rightBlocker.updateCirclePos(ballPos, radius, direction.turnNew(turnAngle / 2));
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public int calcPlayableScore(AIInfoFrame currentFrame)
	{
		return 0;
	}
}
