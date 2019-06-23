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

import edu.dhbw.mannheim.tigers.sumatra.model.data.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.MoveRole;


/**
 * Two bots will form a block in a distance of 500 mm to the ball in order to
 * protect the goal after the game is stopped by the referee
 * @author FlorianS
 * 
 */
public class PositioningOnStoppedPlayWithTwoPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID		= -3177342585857911649L;
	private final Goal			goal						= AIConfig.getGeometry().getGoalOur();
	private final Vector2f		US_GOAL_MID				= goal.getGoalCenter();
	private final float			BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
//	private final float			SPACE_BETWEEN_BOTS	= AIConfig.getPlays().getPositioningOnStoppedPlayWithTwo()
//																			.getSpaceBetweenBots();
	
//	private Vector2				direction;
	
	private final float			RADIUS = BOT_RADIUS + 700;
//	private float					turnAngle;
	
	private MoveRole				leftBlocker;
	private ManToManMarkerRole				marker;
	
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public PositioningOnStoppedPlayWithTwoPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.POSITIONING_ON_STOPPED_PLAY_WITH_TWO, aiFrame);
		
		leftBlocker = new MoveRole();
		addAggressiveRole(leftBlocker);
		
		marker = new ManToManMarkerRole(EWAI.FIRST);
		addAggressiveRole(marker);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		IVector2 ballPos = currentFrame.worldFrame.ball.pos;
//		turnAngle = (float) Math.acos(((2 * RADIUS * RADIUS) - (Math.pow(2 * BOT_RADIUS + SPACE_BETWEEN_BOTS, 2)))
//				/ (2 * RADIUS * RADIUS));
		
		// vector from ball to the middle of the goal
		IVector2 direction = US_GOAL_MID.subtractNew(ballPos);
		
		leftBlocker.updateCirclePos(ballPos, RADIUS, direction);
		marker.updateTarget(currentFrame.tacticalInfo.getOpponentPassReceiver());
		marker.setForbiddenCircle(new Circle(currentFrame.worldFrame.ball.pos, 700+AIConfig.getGeometry().getBotRadius()));
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
