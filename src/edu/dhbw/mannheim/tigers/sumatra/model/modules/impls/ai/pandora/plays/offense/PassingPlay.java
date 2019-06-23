/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 2, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import java.awt.Color;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible.TargetVisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.ScoringChanceCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.ScoringChanceCrit.EPrecision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.ScoringChanceCrit.EScoringChance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.BotInRectangleCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.BotInRectangleCrit.EField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlayState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassReceiverStraightRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;


/**
 * This Play will pass a ball from one bot to another.
 * The play will try to find a receiver for it self (by setting the initPos appropriate)
 * The receiver will take the ball on the dribbler
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class PassingPlay extends AOffensivePlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log					= Logger.getLogger(PassingPlay.class.getName());
	
	private PassSenderRole						passSender;
	private final PassReceiverStraightRole	receiver;
	private BallGetterRole						ballGetter;
	
	private final TargetVisibleCon			targetVisibleCon	= new TargetVisibleCon();
	
	private enum State
	{
		GET,
		PASS,
		PASS_FORCED,
	}
	
	private State	state	= State.GET;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public PassingPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		// we have the ball
		addCriterion(new BallPossessionCrit(EBallPossession.WE));
		// but we have no chance to score
		addCriterion(new ScoringChanceCrit(ETeam.TIGERS, EPrecision.APPROXIMATE, EScoringChance.NO));
		addCriterion(new BotInRectangleCrit(EField.THEIR_HALF, true));
		
		IVector2 ballPos = aiFrame.worldFrame.ball.getPos();
		
		receiver = new PassReceiverStraightRole();
		ballGetter = new BallGetterRole(ballPos, EBallContact.DISTANCE);
		
		BotID receiverId = AiMath.getReceiver(aiFrame, aiFrame.worldFrame.tigerBotsAvailable);
		
		if (receiverId == null)
		{
			log.debug("No receiverPos found.");
			// add roles nevertheless, because PlayFinder hates Plays without roles ;)
			addAggressiveRole(ballGetter, aiFrame.worldFrame.ball.getPos());
			addAggressiveRole(receiver, Vector2.ZERO_VECTOR);
			changeToFailed();
		} else
		{
			IVector2 receiverPos = aiFrame.worldFrame.tigerBotsVisible.get(receiverId).getPos();
			passSender = new PassSenderRole(receiverPos, true, 1.0f);
			addAggressiveRole(ballGetter, getPasserPos(ballPos, receiverPos));
			addAggressiveRole(receiver, receiverPos);
		}
		if (aiFrame.worldFrame.ball.getPos().x() > 0)
		{
			log.info("Ball is in their half, cancel");
			changeToFailed();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private IVector2 getPasserPos(IVector2 ballPos, IVector2 receiverPos)
	{
		return GeoMath.stepAlongLine(ballPos, receiverPos, -(AIConfig.getGeometry().getBotRadius() + AIConfig
				.getDefaultBotConfig().getGeneral().getPositioningPreAiming()));
	}
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		super.beforeFirstUpdate(frame);
		targetVisibleCon.addToIgnore(receiver.getBotID());
		ballGetter.setViewPoint(receiver.getDestination());
		
		if (GeoMath.distancePP(ballGetter.getPos(), receiver.getPos()) < 1000)
		{
			log.info("Passer and receiver were too near");
			changeToFailed();
		}
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		if (getPlayState() != EPlayState.RUNNING)
		{
			return;
		}
		
		IVector2 goalCenter = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		passSender.updateReceiverPos(receiver.getDestination());
		frame.addDebugShape(new DrawableLine(new Line(receiver.getDestination(), goalCenter.subtractNew(receiver
				.getDestination())), Color.blue, true));
		
		if ((state == State.GET) && ballGetter.isCompleted())
		{
			switchRoles(ballGetter, passSender, frame);
			state = State.PASS;
			resetTimer();
			setTimeout(5);
		}
		if ((state == State.PASS) || (state == State.PASS_FORCED))
		{
			passSender.setReceiverReady(receiver.isReady());
			if (passSender.isCompleted())
			{
				receiver.setReady();
			}
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (getPlayState() != EPlayState.RUNNING)
		{
			return;
		}
		if (receiver.isCompleted())
		{
			log.info("Passing succeeded");
			changeToSucceeded();
			return;
		}
		if (passSender.isCompleted())
		{
			// passSender is done, give receiver some seconds to get the ball
			setTimeout(5);
		}
		targetVisibleCon.updateTarget(receiver.getPos());
		if ((state == State.PASS)
				&& (targetVisibleCon.checkCondition(currentFrame.worldFrame, passSender.getBotID()) != EConditionState.FULFILLED))
		{
			log.info("I can not see my target anymore");
			changeToFailed();
		}
	}
	
	
	@Override
	protected void timedOut(AIInfoFrame currentFrame)
	{
		if (state == State.PASS)
		{
			state = State.PASS_FORCED;
			resetTimer();
			setTimeout(1);
		} else
		{
			changeToFinished();
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
