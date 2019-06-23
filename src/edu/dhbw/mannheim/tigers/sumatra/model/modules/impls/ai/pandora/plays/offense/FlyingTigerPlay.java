/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.06.2013
 * Author(s): Felix Pistorius
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.EnhancedFieldAnalyser;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV3Role;


/**
 * This play needs two bots. One bot will get the ball and the other one will
 * move to the front of the opponents goal. After that, the play will determine
 * to shoot directly or indirectly.
 * 
 * @author Felix Pistorius
 * 
 */
public class FlyingTigerPlay extends ABallDealingPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log			= Logger.getLogger(FlyingTigerPlay.class.getName());
	
	private BallGetterRole			ballGetter;
	private MoveRole					moveRole;
	
	private ShooterV3Role			shooterRole;
	
	private PassSenderRole			passRole;
	private RedirectRole				redirectRole;
	
	private State						state;
	private int							retry			= 0;
	
	private final static float		SHOOT_VALUE	= 0.95f;
	private final static int		MAX_RETRY	= 3;
	
	private enum State
	{
		INIT,
		DIRECT_SHOT,
		SHOT,
		INDIRECT_SHOT
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public FlyingTigerPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		state = State.INIT;
		
		IVector2 initDest = getMoveDest(aiFrame);
		
		// ballGetter = new BallGetterRole(AIConfig.getGeometry().getGoalTheir().getGoalCenter(), EBallContact.DRIBBLE);
		ballGetter = new BallGetterRole(AIConfig.getGeometry().getGoalTheir().getGoalCenter(), EBallContact.DISTANCE);
		moveRole = new MoveRole(EMoveBehavior.NORMAL);
		moveRole.initDestination(initDest);
		
		shooterRole = new ShooterV3Role(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		passRole = new PassSenderRole(initDest, true, 0.2f);
		redirectRole = new RedirectRole(initDest, false);
		
		addAggressiveRole(ballGetter, aiFrame.worldFrame.ball.getPos());
		addAggressiveRole(moveRole, initDest);
		log.info("Init Dest: " + initDest);
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
			case INIT:
				if (ballGetter.isCompleted())
				{
					state = analyzeGameSituation(frame);
					log.info("New state: " + state.toString());
				}
				break;
			case DIRECT_SHOT:
				switchRoles(ballGetter, shooterRole, frame);
				state = State.SHOT;
				break;
			case INDIRECT_SHOT:
				switchRoles(ballGetter, passRole, frame);
				switchRoles(moveRole, redirectRole, frame);
				
				passRole.updateDestination(passRole.getPos());
				passRole.updateReceiverPos(AiMath.getBotKickerPos(redirectRole.getDestination(),
						redirectRole.getTargetAngle()));
				
				passRole.setReceiverReady();
				state = State.SHOT;
				setTimeout(5);
				break;
			case SHOT:
				if (shooterRole.isCompleted() || (redirectRole.isCompleted() && passRole.isCompleted()))
				{
					log.info("play finished");
					changeToFinished();
				}
				break;
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
	}
	
	
	@Override
	protected void timedOut(AIInfoFrame frame)
	{
		log.warn("Failed timeout");
		changeToFailed();
	}
	
	
	/**
	 * 
	 * Looks for a free position in the front of the opponents goal.
	 * 
	 * @param aiFrame current AIInfoFrame
	 * @return init destination for move role
	 */
	private IVector2 getMoveDest(AIInfoFrame aiFrame)
	{
		EnhancedFieldAnalyser fa = aiFrame.tacticalInfo.getEnhancedFieldAnalyser();
		
		IVector2 start = AIConfig.getGeometry().getPenaltyAreaTheir().stepAlongPenArea(700);
		float s = AIConfig.getGeometry().getBotRadius() * 7;
		
		if ((fa != null) && (fa.getScoringQuadrantNr1() < fa.getScoringQuadrantNr4()))
		{
			start = new Vector2f(start.x(), start.y() + s);
		} else
		{
			start = new Vector2f(start.x(), (-1 * start.y()) - s);
		}
		
		return start;
	}
	
	
	/**
	 * 
	 * This method analyzes the current game situation and determines which next state will be the best.
	 * 
	 * @param frame current AIInfoFrame
	 * @return best next state
	 */
	private State analyzeGameSituation(AIInfoFrame frame)
	{
		if ((frame == null) || (frame.tacticalInfo.getValueOfBestDirectShootTarget() == null))
		{
			retry++;
			if (retry > MAX_RETRY)
			{
				changeToFailed();
			}
			return State.INIT;
		}
		
		if (frame.tacticalInfo.getValueOfBestDirectShootTarget() < SHOOT_VALUE)
		{
			return State.DIRECT_SHOT;
		}
		
		return State.INDIRECT_SHOT;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
