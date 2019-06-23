/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 13, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallConquerRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassReceiverStraightRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;


/**
 * Get the ball by driving to it and try to conquer it against an opponent.
 * Will pull ball back in an elliptic curve in order to pass ball or (if this
 * play only has one role) be prepared to pass to a bot.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BallConquerPlay extends ABallGetterPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger				log					= Logger.getLogger(BallConquerPlay.class.getName());
	private static final int					PASSING_TIMEOUT	= 3;
	
	private final BallGetterRole				ballGetter;
	private final BallConquerRole				ballConquerer;
	private final PassSenderRole				passSender;
	private final PassReceiverStraightRole	passReceiver;
	private IVector2								receiverPos			= Vector2.ZERO_VECTOR;
	
	private boolean								weHaveAReceiver	= false;
	
	private EState									state					= EState.GET;
	
	private enum EState
	{
		GET,
		CONQUER,
		PASS;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public BallConquerPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		ballGetter = new BallGetterRole(aiFrame.worldFrame.ball.getPos(), EBallContact.DRIBBLE);
		ballConquerer = new BallConquerRole(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		passSender = new PassSenderRole(Vector2.ZERO_VECTOR, true);
		passReceiver = new PassReceiverStraightRole();
		
		addAggressiveRole(ballGetter, aiFrame.worldFrame.ball.getPos());
		
		if (numAssignedRoles == 2)
		{
			weHaveAReceiver = true;
			IVector2 receiverPos = calcReceiver(aiFrame, aiFrame.worldFrame.ball.getPos());
			if (receiverPos == null)
			{
				changeToFailed();
			} else
			{
				addAggressiveRole(passReceiver, receiverPos);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private void updateRoles(AIInfoFrame frame)
	{
		ballGetter.setViewPoint(frame.worldFrame.ball.getPos());
		ballConquerer.setLookAtAfter(receiverPos);
		passSender.updateReceiverPos(receiverPos);
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		updateRoles(frame);
		
		if (weHaveAReceiver)
		{
			receiverPos = passReceiver.getDestination();
		}
	}
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		updateRoles(frame);
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		switch (state)
		{
			case GET:
				if (ballGetter.isCompleted())
				{
					switchRoles(ballGetter, ballConquerer, currentFrame);
					state = EState.CONQUER;
					// set receiverPos only once to avoid toggling
					receiverPos = calcReceiver(currentFrame, ballConquerer.getPos());
					if (receiverPos == null)
					{
						log.debug("No receiver");
						changeToFailed();
						return;
					}
				}
				break;
			case CONQUER:
				if (ballConquerer.isCompleted())
				{
					if (weHaveAReceiver)
					{
						switchRoles(ballConquerer, passSender, currentFrame);
						state = EState.PASS;
						resetTimer();
						setTimeout(PASSING_TIMEOUT);
						// we do not have time to wait for the receiver
						passSender.setReceiverReady(true);
					} else
					{
						changeToFinished();
					}
				}
				break;
			case PASS:
				if (passSender.isCompleted())
				{
					passReceiver.setReady();
					if (passReceiver.isCompleted())
					{
						// changeToFinished();
						changeToSucceeded();
					}
				}
				break;
		}
	}
	
	
	private IVector2 calcReceiver(AIInfoFrame currentFrame, final IVector2 senderPos)
	{
		List<BotID> botsSorted = AiMath.getTigerBotsNearestToPointSorted(currentFrame, senderPos);
		if (botsSorted.size() <= 1)
		{
			return null;
		}
		botsSorted.remove(0);
		
		for (BotID botId : botsSorted)
		{
			IVector2 end = currentFrame.worldFrame.tigerBotsVisible.get(botId).getPos();
			float raySize = AIConfig.getGeometry().getBotRadius() * 2;
			if (GeoMath.p2pVisibility(currentFrame.worldFrame, senderPos, end, raySize))
			{
				return end;
			}
		}
		return currentFrame.worldFrame.tigerBotsVisible.get(botsSorted.get(0)).getPos();
	}
	
	
	@Override
	protected void timedOut(AIInfoFrame currentFrame)
	{
		log.info("timed out");
		// changeToFinished();
		changeToFailed();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
