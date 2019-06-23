/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.02.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.OpponentPassReceiverCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.PassBlockerRole;


/**
 * This play shall be selected if our opponent possesses the ball and there is
 * a possible pass receiver for the opponent ball carrier. It includes one bots
 * man-marking the opponent ball carrier and another bot trying block a
 * possible opponent pass receiver.
 * 
 * This play contains an iteration over all opponent bots to determine which bot
 * shall be blocked from receiving a pass. If there are no opponent bots in the
 * worldFrame a default destination will be taken.
 * 
 * @author FlorianS
 * 
 */
public class BallCapturingWithOnePassBlockerPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long			serialVersionUID				= 7426927419588064446L;
	
	private ManToManMarkerRole			marker;
	private PassBlockerRole				passBlocker;
	
	private final float					BOT_RADIUS						= AIConfig.getGeometry().getBotRadius();
	private final Vector2f				FIELD_CENTER					= AIConfig.getGeometry().getCenter();
	
	private final float					radius							= 2 * BOT_RADIUS + 200;
	private Vector2						direction						= new Vector2(AIConfig.INIT_VECTOR);
	
	private BallPossessionCrit			ballPossessionCrit			= null;
	private OpponentPassReceiverCrit	opponentPassReceiverCrit	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public BallCapturingWithOnePassBlockerPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.BALLCAPTURING_WITH_ONE_PASS_BLOCKER, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.THEY);
		opponentPassReceiverCrit = new OpponentPassReceiverCrit(true);
		addCriterion(ballPossessionCrit);
		addCriterion(opponentPassReceiverCrit);
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		Vector2f fieldCenter = AIConfig.getGeometry().getCenter();
		
		Vector2 initPosMarker = new Vector2(ballPos);
		Vector2 initPosPassBlocker = new Vector2(fieldCenter);
		
		marker = new ManToManMarkerRole(EWAI.ONLY);
		passBlocker = new PassBlockerRole();
		addAggressiveRole(marker, initPosMarker);
		addAggressiveRole(passBlocker, initPosPassBlocker);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		WorldFrame worldFrame = currentFrame.worldFrame;
		
		Vector2 ballPos = new Vector2(worldFrame.ball.pos);
		TrackedBot opponentPassReceiver = currentFrame.tacticalInfo.getOpponentPassReceiver();
		
		if (opponentPassReceiver != null)
		{
			direction = ballPos.subtractNew(opponentPassReceiver.pos);
			passBlocker.updateCirclePos(opponentPassReceiver.pos, radius, direction);
		} else
		{
			direction = new Vector2(AIConfig.INIT_VECTOR);
			passBlocker.updateCirclePos(FIELD_CENTER, radius, direction);
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() != EBallPossession.THEY)
		{
			changeToSucceeded();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public boolean isBallCarrying()
	{
		return true;
	}
}