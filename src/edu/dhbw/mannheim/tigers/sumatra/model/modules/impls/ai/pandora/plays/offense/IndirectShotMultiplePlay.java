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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.NormalStartCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;


/**
 * This plays consists of a passer that passes the ball to a receiver.
 * The receiver redirect the ball directly to a good position in the goal.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class IndirectShotMultiplePlay extends ABallDealingPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log					= Logger.getLogger(IndirectShotMultiplePlay.class.getName());
	private PassSenderRole			passer;
	private List<RedirectRole>		receivers			= new ArrayList<RedirectRole>(2);
	private final BotID				receiverId;
	
	private ARole						currentReceiver	= null;
	
	private boolean					passPhase			= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public IndirectShotMultiplePlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		addCriterion(new NormalStartCrit());
		
		receiverId = AiMath.getReceiver(aiFrame, aiFrame.worldFrame.tigerBotsAvailable);
		log.debug("Receiver: " + receiverId);
		if (receiverId == null)
		{
			log.debug("No receiver found");
			changeToFailed();
			return;
		}
		
		
		TrackedTigerBot receiverBot = null;
		for (int i = 1; i < getNumAssignedRoles(); i++)
		{
			receiverBot = aiFrame.worldFrame.tigerBotsAvailable.getWithNull(receiverId);
			if (receiverBot == null)
			{
				log.warn("No receiver bot was found. Abort IndirectShotV2");
				changeToFailed();
				return;
			}
			IVector2 receiverPos = receiverBot.getPos();
			RedirectRole receiver = new RedirectRole(receiverPos, false);
			receivers.add(receiver);
			addAggressiveRole(receiver, receiverPos);
		}
		if (receiverBot == null)
		{
			log.warn("something is wrong");
			changeToFailed();
			return;
		}
		passer = new PassSenderRole(AiMath.getBotKickerPos(receiverBot), true, 1.0f);
		addAggressiveRole(passer, aiFrame.worldFrame.ball.getPos());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		float dist = Float.MAX_VALUE;
		for (ARole role : getRoles())
		{
			if (role.getType() == ERole.REDIRECTER)
			{
				float d = GeoMath.distancePP(passer.getDestination(), role.getDestination());
				if (d < dist)
				{
					dist = d;
					currentReceiver = role;
				}
			}
		}
		if (currentReceiver == null)
		{
			log.warn("should not happen ;)");
			changeToFailed();
			return;
		}
		
		passer.updateReceiverPos(AiMath.getBotKickerPos(currentReceiver.getDestination(),
				currentReceiver.getTargetAngle()));
		
		// if (receiver.getPos().x() < 0)
		// {
		// log.info("Receiver is on our side of the field. For security, the play will be stopped");
		// changeToFailed();
		// }
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		if (currentReceiver.getDestination().equals(currentReceiver.getPos(), 100))
		// if (receiver.checkMoveCondition())
		{
			passer.setReceiverReady();
		}
		if (currentReceiver.isCompleted())
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
				setTimeout(7);
				passPhase = true;
			}
			((RedirectRole) currentReceiver).setReady();
		} else
		{
			passer.updateReceiverPos(AiMath.getBotKickerPos(currentReceiver.getDestination(),
					currentReceiver.getTargetAngle()));
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
