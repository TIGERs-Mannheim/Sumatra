/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.TigersApproximateScoringChanceCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.TigersPassReceiverCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSender;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PositioningRole;


/**
 * This play shall be selected if one of our bots is in ball possession and there
 * is no direct scoring chance.
 * 
 * @author FlorianS
 * 
 */
public class PassForwardPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long							serialVersionUID							= 7920263055813774341L;
	
	private PassSender									sender;
	private PositioningRole								receiver;
	
	private BallPossessionCrit							ballPossessionCrit						= null;
	private TigersApproximateScoringChanceCrit	tigersApproximateScoringChanceCrit	= null;
	private TigersPassReceiverCrit					tigersPassReceiverCrit					= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 */
	public PassForwardPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.PASS_FORWARD, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.WE);
		tigersApproximateScoringChanceCrit = new TigersApproximateScoringChanceCrit(false);
		tigersPassReceiverCrit = new TigersPassReceiverCrit(true);
		addCriterion(ballPossessionCrit);
		addCriterion(tigersApproximateScoringChanceCrit);
		addCriterion(tigersPassReceiverCrit);
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		Vector2f fieldCenter = AIConfig.getGeometry().getCenter();
		
		Vector2 initPosSender = new Vector2(ballPos);
		Vector2 initPosReceiver = new Vector2(fieldCenter);
		
		sender = new PassSender(EGameSituation.GAME);
		receiver = new PositioningRole(false);
		addAggressiveRole(sender, initPosSender);
		addCreativeRole(receiver, initPosReceiver);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame currentFrame)
	{
		Vector2 ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
		
		receiver.setTarget(ballPos);
		receiver.setDestination(receiver.getPos(currentFrame));
	}
	

	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		WorldFrame worldFrame = currentFrame.worldFrame;
		Vector2 ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
		// Vector2 receiverPos = new Vector2(receiver.getPos(currentFrame));
		
		receiver.setTarget(ballPos);
		receiver.setDestination(receiver.getPos(currentFrame));
		
		sender.updateRecieverPos(receiver.getDestination());
		
		// if sender is ready to shoot and receiver is ready to take the ball
		if (sender.checkReadyToShoot(worldFrame))
		{
			sender.forcePass();
		} else
		{
			sender.forceStopPass();
		}
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// fail condition: turnover
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY
				|| currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.NO_ONE)
		{
			changeToFailed();
			return;
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