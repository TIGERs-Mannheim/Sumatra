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

import java.util.Random;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.AOffensivePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.TurnAroundBallRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.AReceiverRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ChipKickRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;


/**
 * Corner with confusion of opponents.
 * Two possible Receiver can take the ball and shoot to the goal. The sender decides in the last second who receives the
 * ball. Opponents do not know which bot receives the ball.
 * 
 * => goal cannot directly be scored (indirect)
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class CornerPlay extends AOffensivePlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log					= Logger.getLogger(CornerPlay.class.getName());
	private static final float		INIT_POS_OFFSET	= 100;
	private final BallGetterRole	ballGetter;
	private TurnAroundBallRole		turnAroundBall;
	private ChipKickRole				passSender;
	private final AReceiverRole	possiblePassReceiverOne;
	private final AReceiverRole	possiblePassReceiverTwo;
	private int							randSelection;
	
	
	private enum EState
	{
		GET,
		CONFUSE,
		PASS;
	}
	
	/** First get the ball */
	private EState	state	= EState.GET;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public CornerPlay(AIInfoFrame aiFrame, int numAssignedRoles)
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
		
		final Vector2 initPosReceiverOne;
		final Vector2 initPosReceiverTwo;
		BotID receiverOneId = AiMath.getReceiver(aiFrame, aiFrame.worldFrame.tigerBotsAvailable);
		BotID receiverTwoId;
		if (receiverOneId == null)
		{
			log.info("No potential receiver found, set initPos and let role assigner decide");
			initPosReceiverOne = receiverPos(aiFrame, 1 / 4, 1 / 3);
			initPosReceiverTwo = receiverPos(aiFrame, 1 / 4, 2 / 3);
		} else
		{
			initPosReceiverOne = new Vector2(aiFrame.worldFrame.tigerBotsVisible.get(receiverOneId).getPos());
			IBotIDMap<TrackedTigerBot> botsAvailable = new BotIDMap<TrackedTigerBot>(aiFrame.worldFrame.tigerBotsAvailable);
			botsAvailable.remove(receiverOneId);
			receiverTwoId = AiMath.getReceiver(aiFrame, botsAvailable);
			if (receiverTwoId == null)
			{
				initPosReceiverTwo = receiverPos(aiFrame, 1 / 4, 1 / 4);
			} else
			{
				initPosReceiverTwo = new Vector2(aiFrame.worldFrame.tigerBotsVisible.get(receiverTwoId).getPos());
			}
		}
		possiblePassReceiverOne = new RedirectRole(initPosReceiverOne, true);
		possiblePassReceiverOne.setPassUsesChipper(true);
		addAggressiveRole(possiblePassReceiverOne, initPosReceiverOne);
		
		possiblePassReceiverTwo = new RedirectRole(initPosReceiverTwo, true);
		possiblePassReceiverOne.setPassUsesChipper(true);
		addAggressiveRole(possiblePassReceiverTwo, initPosReceiverTwo);
	}
	
	
	private Vector2 receiverPos(AIInfoFrame frame, float lengthDividend, float widthDividend)
	{
		Vector2 pos = new Vector2(AIConfig.getGeometry().getFieldLength() * lengthDividend, AIConfig.getGeometry()
				.getFieldWidth() * widthDividend);
		if (frame.worldFrame.ball.getPos().y() > 0)
		{
			pos.setY(pos.y * -1);
		}
		return pos;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		switch (state)
		{
			case GET:
				if (ballGetter.isCompleted())
				{
					state = EState.PASS;
				}
				break;
			case CONFUSE:
				if ((frame.refereeMsg != null) && (frame.refereeMsg.getCommand() == Command.NORMAL_START))
				{
					turnAroundBall = new TurnAroundBallRole(1);
					switchRoles(ballGetter, turnAroundBall, frame);
					state = EState.PASS;
				} else if ((turnAroundBall != null) && turnAroundBall.isCompleted())
				{
					state = EState.PASS;
				}
				break;
			
			case PASS:
				randSelection = new Random().nextInt(2);
				float distance;
				IVector2 target;
				if (randSelection == 0)
				{
					distance = GeoMath.distancePP(ballGetter.getPos(), possiblePassReceiverOne.getPos());
					target = GeoMath.stepAlongLine(ballGetter.getPos(), possiblePassReceiverOne.getPos(), distance
							* AIConfig.getRoles().getChipPassDistFactor());
					passSender = new ChipKickRole(target, (1 - AIConfig.getRoles().getChipPassDistFactor()) + 500);
					switchRoles(ballGetter, passSender, frame);
					possiblePassReceiverOne.setReady();
				} else
				{
					distance = GeoMath.distancePP(ballGetter.getPos(), possiblePassReceiverTwo.getPos());
					target = GeoMath.stepAlongLine(ballGetter.getPos(), possiblePassReceiverTwo.getPos(), distance
							* AIConfig.getRoles().getChipPassDistFactor());
					passSender = new ChipKickRole(target, (1 - AIConfig.getRoles().getChipPassDistFactor()) + 500);
					switchRoles(ballGetter, passSender, frame);
					possiblePassReceiverTwo.setReady();
				}
				
				setTimeout(5);
				break;
			default:
				break;
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (randSelection == 0)
		{
			if ((possiblePassReceiverOne != null) && (passSender != null) && possiblePassReceiverOne.isCompleted()
					&& passSender.isCompleted())
			{
				changeToFinished();
			}
		} else
		{
			if ((possiblePassReceiverTwo != null) && (passSender != null) && possiblePassReceiverTwo.isCompleted()
					&& passSender.isCompleted())
			{
				changeToFinished();
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
