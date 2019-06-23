/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.06.2013
 * Author(s): jan
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV2Role;


/**
 * Do not use it!.
 * @author jan
 * 
 */

public class DoublePassPlay extends ABallDealingPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private static final Logger	log									= Logger.getLogger(DoublePassPlay.class.getName());
	private final float				BALL_MOVING_TRIGGER_BY_VISION	= 0.42f;
	// first BOT
	private PassSenderRole			firstBotPassSender;
	private BallGetterRole			firstBotGetBall;
	// first change to
	@SuppressWarnings("unused")
	private ShooterV2Role			firstBotShooter;
	private RedirectRole				firstBotRedirect;
	// second Bot
	private MoveRole					secBotReceiver;
	private BallGetterRole			secBotGetBall;
	// second change to
	private PassSenderRole			secBotPassSender;
	private final BotID				receiverId;
	private EState						state									= EState.PASS;
	private boolean					singlechange						= false;
	@SuppressWarnings("unused")
	private IVector2					redirectposition;
	private boolean					activePlay							= true;
	
	enum EState
	{
		PASS,
		SHOOT;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public DoublePassPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		receiverId = AiMath.getReceiver(aiFrame, aiFrame.worldFrame.tigerBotsAvailable);
		log.debug("Receiver: " + receiverId);
		if (receiverId == null)
		{
			changeToFailed();
			return;
		}
		
		TrackedTigerBot receiverBot = aiFrame.worldFrame.tigerBotsAvailable.get(receiverId);
		IVector2 receiverPos = receiverBot.getPos();
		
		firstBotPassSender = new PassSenderRole(AiMath.getBotKickerPos(receiverBot), true, 1.0f);
		secBotReceiver = new MoveRole(EMoveBehavior.NORMAL);
		
		addAggressiveRole(firstBotPassSender, aiFrame.worldFrame.ball.getPos());
		addAggressiveRole(secBotReceiver, receiverPos);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		if (!secBotReceiver.getBotID().equals(receiverId))
		{
			log.info("Preferred receiver not assigned. Requested: " + receiverId + " got: " + secBotReceiver.getBotID());
			changeToFailed();
			return;
		}
		secBotReceiver.updateDestination(secBotReceiver.getPos());
		secBotReceiver.updateLookAtTarget(firstBotPassSender.getPos());
		firstBotPassSender.updateReceiverPos(AiMath.getBotKickerPos(secBotReceiver.getDestination(),
				secBotReceiver.getTargetAngle()));
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		// first pass the ball to a team member. Then switch roles and "run" to a free place
		// the first receiver should pass to the now free member and this new receiver should aim the enemies goal!
		if ((frame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.THEY)
				&& (activePlay == false))
		{
			log.debug("They have the ball. Can't use the play");
			changeToFailed();
		}
		switch (state)
		{
		
			case PASS:
				activePlay = false;
				secBotReceiver.updateLookAtTarget(firstBotPassSender.getPos());
				firstBotPassSender.setReceiverReady();
				
				
				if (((firstBotPassSender.checkMovementCondition() == EConditionState.FULFILLED) || ((firstBotPassSender
						.checkMovementCondition() == EConditionState.PENDING)))
						&& ((frame.worldFrame.getBall().getVel().getLength2()) > BALL_MOVING_TRIGGER_BY_VISION))
				{
					activePlay = true;
					secBotGetBall = new BallGetterRole(frame.worldFrame.getBall().getPos(), EBallContact.DRIBBLE);
					switchRoles(secBotReceiver, secBotGetBall, frame);
					
					state = EState.SHOOT;
					log.debug("change role of first Bot from PassSender to Redirect");
					firstBotRedirect = new RedirectRole(firstBotPassSender.getPos(), false);
					
					
					switchRoles(firstBotPassSender, firstBotRedirect, frame);
					firstBotRedirect.setInitPosition(firstBotPassSender.getPos());
					singlechange = true;
				}
				
				break;
			case SHOOT:
				activePlay = false;
				if ((GeoMath.distancePP(secBotGetBall.getPos(), frame.worldFrame.getBall().getPos()) < 250)
						&& ((frame.worldFrame.getBall().getVel().getLength2()) < 0.1f))
				{
					activePlay = true;
					// change to passSender
					if (singlechange)
					{
						log.debug("2,drin");
						secBotPassSender = new PassSenderRole(AiMath.getBotKickerPos(firstBotRedirect.getBot()), true, 0.95f);
						switchRoles(secBotGetBall, secBotPassSender, frame);
						secBotPassSender.updateDestination(secBotGetBall.getPos());
						secBotPassSender.updateLookAtTarget(firstBotRedirect.getPos());
						redirectposition = firstBotRedirect.getPos();
						// set the firstBotRedirect to completed
						firstBotRedirect.setCompleted();
						singlechange = false;
					}
					
					
				}
				if (firstBotRedirect.isCompleted())
				{
					// switch receiver to getter then to shooter(first Bot)
					firstBotGetBall = new BallGetterRole(frame.worldFrame.getBall().getPos(), EBallContact.DRIBBLE);
					switchRoles(firstBotRedirect, firstBotGetBall, frame);
					firstBotGetBall.updateDestination(firstBotRedirect.getPos());
					firstBotGetBall.updateLookAtTarget(frame.worldFrame.getBall().getPos());
					secBotPassSender.setReceiverReady();
				}
				if ((frame.worldFrame.getBall().getVel().getLength2()) > BALL_MOVING_TRIGGER_BY_VISION)
				{
					secBotPassSender.setCompleted();
				}
				if ((GeoMath.distancePP(secBotGetBall.getPos(), frame.worldFrame.getBall().getPos()) < 250)
						&& ((frame.worldFrame.getBall().getVel().getLength2()) < 0.1f))
				{
					
				}
				
		}
		
		
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing to do here
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
