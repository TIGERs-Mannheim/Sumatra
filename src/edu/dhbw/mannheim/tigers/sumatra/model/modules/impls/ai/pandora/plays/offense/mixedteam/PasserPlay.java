/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.mixedteam;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.AOffensivePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;


/**
 * Mixed Team play for passing the ball to a bot of the other sub team
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class PasserPlay extends AOffensivePlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log	= Logger.getLogger(PasserPlay.class.getName());
	private BallGetterRole			ballGetter;
	private PassSenderRole			passSender;
	private BotID						receiver;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public PasserPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		
		receiver = AiMath.getReceiver(aiFrame, AiMath.getOtherBots(aiFrame));
		if (receiver != null)
		{
			ballGetter = new BallGetterRole(aiFrame.worldFrame.tigerBotsVisible.get(receiver).getPos(),
					EBallContact.DISTANCE);
			addAggressiveRole(ballGetter, aiFrame.worldFrame.ball.getPos());
		} else
		{
			log.debug("No receiver found");
			ballGetter = new BallGetterRole(Vector2.ZERO_VECTOR, EBallContact.DISTANCE);
			addAggressiveRole(ballGetter, aiFrame.worldFrame.ball.getPos());
			changeToFailed();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame frame)
	{
		if (receiver == null)
		{
			changeToFailed();
			return;
		}
		if ((passSender == null) && ballGetter.isCompleted())
		{
			passSender = new PassSenderRole(frame.worldFrame.tigerBotsVisible.get(receiver).getPos(), true, 1);
			passSender.setReceiverReady(true);
			switchRoles(ballGetter, passSender, frame);
		}
		if ((passSender != null) && passSender.isCompleted())
		{
			changeToFinished();
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
