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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;


/**
 * Two bots will form a block in a distance of 500 mm to the ball in order to
 * protect the goal after the game is stopped by the referee
 * @author FlorianS
 * 
 */
public class StopMarkerPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID		= -3177342585857911649L;
//	private final Goal			goal						= AIConfig.getGeometry().getGoalOur();
//	private final Vector2f		US_GOAL_MID				= goal.getGoalCenter();
//	private final float			BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
//	private final float			SPACE_BETWEEN_BOTS	= AIConfig.getPlays().getPositioningOnStoppedPlayWithTwo()
//																			.getSpaceBetweenBots();
//	
//	private Vector2				ballPos;
//	private Vector2				direction;
//	
//	private float					radius;
//	private float					turnAngle;
//	
//	private MoveRole				leftBlocker;
	private ManToManMarkerRole				marker;
	private ManToManMarkerRole				marker1;
	
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public StopMarkerPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.STOP_MARKER, aiFrame);
//		Vector2 initPos = new Vector2(AIConfig.getGeometry().getCenter());
		
		marker1 = new ManToManMarkerRole(EWAI.FIRST);
		addAggressiveRole(marker1);	//, initPos.addY(500));
		marker = new ManToManMarkerRole(EWAI.FIRST);
		addAggressiveRole(marker);	//, initPos.addY(-1000));
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
//		radiuson = US_GOAL_MID.subtractNew(ballPos);
		
//		leftBlocker.updateCirclePos(ballPos, radius, direction);
		marker.updateTarget(currentFrame.tacticalInfo.getOpponentPassReceiver());
		marker.setForbiddenCircle(new Circle(currentFrame.worldFrame.ball.pos, 500+AIConfig.getGeometry().getBotRadius()));
		
		marker1.updateTarget(currentFrame.tacticalInfo.getDangerousOpponents().get(2));
		marker1.setForbiddenCircle(new Circle(currentFrame.worldFrame.ball.pos, 500+AIConfig.getGeometry().getBotRadius()));
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
