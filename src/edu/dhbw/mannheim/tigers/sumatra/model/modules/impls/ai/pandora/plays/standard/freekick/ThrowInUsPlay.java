/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 4, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.AOffensivePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.AReceiverRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ChipKickRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;


/**
 * => goal cannot directly be scored (indirect)
 * => is taken from touch boundary (Seiten), where ball left field
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ThrowInUsPlay extends AOffensivePlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log					= Logger.getLogger(ThrowInUsPlay.class.getName());
	private static final float		INIT_POS_OFFSET	= 100;
	private static final long		TIMEOUT				= 8;
	
	private final BallGetterRole	ballGetter;
	private ARole						passSender;
	private final AReceiverRole	passReceiver;
	
	private enum EState
	{
		GET,
		PASS;
	}
	
	private EState	state	= EState.GET;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public ThrowInUsPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		setTimeout(Long.MAX_VALUE);
		ballGetter = new BallGetterRole(AIConfig.getGeometry().getGoalTheir().getGoalCenter(), EBallContact.DISTANCE);
		
		Vector2 senderInitPos = new Vector2(aiFrame.worldFrame.ball.getPos());
		if (senderInitPos.y() > 0)
		{
			senderInitPos.addY(INIT_POS_OFFSET);
		} else
		{
			senderInitPos.addY(-INIT_POS_OFFSET);
		}
		addAggressiveRole(ballGetter, senderInitPos);
		
		// enemy half in which the throw in occurs
		// try to find someone in the enemy half first.
		BotID receiverId = AiMath.getReceiverInEnemyHalf(aiFrame, aiFrame.worldFrame.tigerBotsAvailable);
		final Vector2 initPosReceiver;
		if (receiverId != null)
		{
			log.debug("ReceiverID(" + receiverId.getNumber() + ") found within the opponents half.");
		}
		
		if (receiverId == null)
		{
			receiverId = AiMath.getReceiver(aiFrame, aiFrame.worldFrame.tigerBotsAvailable);
		}
		if (receiverId == null)
		{
			log.info("No potential receiver found, set initPos and let role assigner decide");
			initPosReceiver = new Vector2(AIConfig.getGeometry().getFieldLength() / 4, AIConfig.getGeometry()
					.getFieldWidth() / 4);
			if (aiFrame.worldFrame.ball.getPos().y() > 0)
			{
				initPosReceiver.setY(initPosReceiver.y * -1);
			}
		} else
		{
			initPosReceiver = new Vector2(aiFrame.worldFrame.tigerBotsVisible.get(receiverId).getPos());
		}
		passReceiver = new RedirectRole(initPosReceiver, true);
		passReceiver.setPassUsesChipper(true);
		addAggressiveRole(passReceiver, initPosReceiver);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		ballGetter.setViewPoint(passReceiver.getDestination());
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		if ((state == EState.GET) && ballGetter.isCompleted())
		{
			state = EState.PASS;
			float distance = GeoMath.distancePP(ballGetter.getPos(), passReceiver.getPos());
			IVector2 target = GeoMath.stepAlongLine(ballGetter.getPos(), passReceiver.getPos(), distance
					* AIConfig.getRoles().getChipPassDistFactor());
			ARole passSender = null;
			if (GeoMath.p2pVisibility(frame.worldFrame, frame.worldFrame.ball.getPos(), target))
			{
				passSender = new PassSenderRole(target, false);
			} else
			{
				passSender = new ChipKickRole(target, (1 - AIConfig.getRoles().getChipPassDistFactor()) * 500);
			}
			switchRoles(ballGetter, passSender, frame);
			passReceiver.setReady();
			setTimeout(TIMEOUT);
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if ((passReceiver != null) && (passSender != null) && passSender.isCompleted())
		{
			// If the enemy got the ball and it didn't reach the receiver.
			if (!passReceiver.isCompleted()
					&& (currentFrame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.THEY))
			{
				changeToFailed();
			}
			if (passReceiver.isCompleted())
			{
				changeToFinished();
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
