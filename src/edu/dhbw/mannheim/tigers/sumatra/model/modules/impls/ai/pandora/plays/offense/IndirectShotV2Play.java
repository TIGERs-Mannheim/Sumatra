/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.07.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.NormalStartCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;


/**
 * This plays consists of a passer that passes the ball to a receiver.
 * The receiver redirect the ball directly to a good position in the goal.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class IndirectShotV2Play extends ABallDealingPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log			= Logger.getLogger(IndirectShotV2Play.class.getName());
	private PassSenderRole			passer;
	private RedirectRole				receiver;
	private final BotID				receiverId;
	
	private boolean					passPhase	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public IndirectShotV2Play(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		// addCriterion(new ScoringChanceCrit(ETeam.TIGERS, EPrecision.APPROXIMATE, EScoringChance.NO));
		
		receiverId = AiMath.getReceiver(aiFrame, aiFrame.worldFrame.tigerBotsAvailable);
		log.debug("Receiver: " + receiverId);
		if (receiverId == null)
		{
			log.debug("No receiver found");
			changeToFailed();
			return;
		}
		
		TrackedTigerBot receiverBot = aiFrame.worldFrame.tigerBotsAvailable.getWithNull(receiverId);
		if (receiverBot == null)
		{
			log.warn("No receiver bot was found. Abort IndirectShotV2");
			changeToFailed();
			return;
		}
		IVector2 receiverPos = receiverBot.getPos();
		
		passer = new PassSenderRole(AiMath.getBotKickerPos(receiverBot), true, 1.0f);
		receiver = new RedirectRole(receiverPos, true);
		
		addCriterion(new NormalStartCrit());
		
		addAggressiveRole(passer, aiFrame.worldFrame.ball.getPos());
		addAggressiveRole(receiver, receiverPos);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		if (!receiver.getBotID().equals(receiverId))
		{
			log.info("Preferred receiver not assigned. Requested: " + receiverId + " got: " + receiver.getBotID());
			changeToFailed();
			return;
		}
		
		passer.updateReceiverPos(AiMath.getBotKickerPos(receiver.getDestination(), receiver.getTargetAngle()));
		
		if (receiver.getPos().x() < 0)
		{
			log.info("Receiver is on our side of the field. For security, the play will be stopped");
			changeToFailed();
		}
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		if (receiver.getDestination().equals(receiver.getPos(), 100))
		// if (receiver.checkMoveCondition())
		{
			passer.setReceiverReady();
		}
		if (receiver.isCompleted())
		{
			if (!passer.isCompleted())
			{
				log.info("Failed because receiver completed before passer");
				changeToFailed();
			} else
			{
				changeToFinished();
			}
		}
		if (passer.isCompleted())
		{
			if (!passPhase)
			{
				resetTimer();
				setTimeout(5);
				passPhase = true;
			}
			receiver.setReady();
		} else
		{
			passer.updateReceiverPos(AiMath.getBotKickerPos(receiver.getDestination(), receiver.getTargetAngle()));
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
