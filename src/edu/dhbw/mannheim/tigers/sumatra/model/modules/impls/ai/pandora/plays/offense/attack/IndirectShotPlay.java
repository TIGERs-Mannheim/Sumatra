/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.04.2011
 * Author(s): GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.fieldraster.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.TigersPassReceiverCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.IndirectShooter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSender;


/**
 * Pass to a bot that can then directly shoot at the goal
 * No ballGetter, because it's never first priority and does not need
 * to move to the ball -> it's there when other plays are pointless..
 * 
 * @author GuntherB
 * 
 */
public class IndirectShotPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long			serialVersionUID	= 5435481433176641898L;
	
	private PassSender					senderS;
	private IndirectShooter				shooter;
	

	// criteria
	private BallPossessionCrit			ballPossCrit;
	private TigersPassReceiverCrit	tigersRecCrit;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 * @param wf
	 */
	public IndirectShotPlay(AIInfoFrame currentFrame)
	{
		super(EPlay.INDIRECT_SHOT, currentFrame);
		
		ballPossCrit = new BallPossessionCrit(EBallPossession.WE);
		tigersRecCrit = new TigersPassReceiverCrit(true);
		addCriterion(ballPossCrit);
		addCriterion(tigersRecCrit);
		
		// --- sender --
		senderS = new PassSender(EGameSituation.GAME);
		addAggressiveRole(senderS, currentFrame.worldFrame.ball.pos);
		
		// --- shooter ---
		shooter = new IndirectShooter(IndirectShooter.PositionLimitation.FREE);
		
		Vector2 startingPosition = new Vector2(0, 0); // no init vector, because that will break the positioning
																		// rectangles
		
		// where is the ball?
		if (currentFrame.tacticalInfo.getOffRightReceiverPoints().size() > 0)
		{
			if (currentFrame.worldFrame.ball.pos.y > 0)
			{
				startingPosition = currentFrame.tacticalInfo.getOffRightReceiverPoints().get(0);
			} else
			{
				startingPosition = currentFrame.tacticalInfo.getOffLeftReceiverPoints().get(0);
			}
		}
		
		shooter.setRectangle(FieldRasterGenerator.getInstance().getPositionRectFromPosition(startingPosition));
		addAggressiveRole(shooter, startingPosition);
		
		setTimeout(10);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected void beforeFirstUpdate(AIInfoFrame currentFrame)
	{
		shooter.updateSenderPos(senderS.getDestination());
		shooter.setSenderId(senderS.getBotID());
		senderS.updateRecieverPos(AIMath.getKickerPosFromBot(currentFrame.worldFrame, shooter.getBotID()));
	}
	

	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		shooter.updateSenderPos(senderS.getDestination());
		shooter.setSenderId(senderS.getBotID());
		senderS.updateRecieverPos(AIMath.getKickerPosFromBot(currentFrame.worldFrame, shooter.getBotID()));
		
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY)
		{
			changeToFailed();
		}
		
	}
	

	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		WorldFrame worldFrame = currentFrame.worldFrame;
		
		// if sender is ready to shoot and receiver is ready to take the ball
		if (senderS.checkReadyToShoot(worldFrame) && shooter.isPrepared(worldFrame))
		{
			// if receiver can also see target
			if (shooter.checkTargetIsVisible(worldFrame))
			{
				senderS.forcePass();
				shooter.forceDirectShot();
				
				resetTimer();
				setTimeout(2);

			} else
			{
				// if certain other things are true, like a dangerous situation, where we need to get the ball
				// going, you may force a pass here, too
			}
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
	

	@Override
	protected void timedOut()
	{
		changeToFailed();
	}
	
}
