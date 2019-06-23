/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 25, 2013
 * Author(s): sebastian
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;


/**
 * A more dynamic Indirect Shoot play. Unlike the V2, the destination is calculated just before the ball is about to be
 * shot.
 * 
 * @author SebastianN
 * 
 */
public class IndirectShotV3Play extends ABallDealingPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log	= Logger.getLogger(IndirectShotV3Play.class.getName());
	private PassSenderRole			passer;
	private RedirectRole				receiver;
	private final BotID				receiverId;
	private IVector2					bestTarget;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public IndirectShotV3Play(AIInfoFrame aiFrame, int numAssignedRoles)
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
		
		passer = new PassSenderRole(AiMath.getBotKickerPosDynamic(receiverPos));
		receiver = new RedirectRole(receiverPos, false, false);
		
		
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
			changeToFailed();
			return;
		}
		if (frame.tacticalInfo.getBestDirectShootTarget() != null)
		{
			bestTarget = frame.tacticalInfo.getBestDirectShootTarget();
		}
		receiver.updateDestination(receiver.getPos());
		receiver.updateLookAtTarget(passer.getPos());
		// passer.updateReceiverPos(AiMath.getBotKickerPos(receiver.getDestination(), receiver.getTargetAngle()));
		
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		if (receiver.checkMoveCondition())
		{
			changeToFinished();
		}
		if (bestTarget == null)
		{
			bestTarget = frame.tacticalInfo.getBestDirectShootTarget();
			
		} else
		{
			passer.updateReceiverPos(new Vector2(-passer.getPos().x(), passer.getPos().y()));
			receiver.updateLookAtTarget(passer.getPos());
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// Do nothing.
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
