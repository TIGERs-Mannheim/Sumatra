/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.TigersApproximateScoringChanceCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.TigersPassReceiverCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSender;


/**
 * If the ball carrier can't do a direct shot and also can't pass the ball to
 * another bot to achieve a goal, this play should be selected. In this play
 * the ball carrier passes the ball back to the own defenders so that they can
 * try to shoot on the goal or pass the ball to another of our bots.
 * 
 * @author TobiasK
 */
public class PassToKeeperPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID		= -4523311461224047481L;
	
	private BallGetterRole		getterRole;
	private PassSender			passSenderRole;
	
	private final float			GOTTEN_DISTANCE		= AIConfig.getGeometry().getBallRadius()
																			+ AIConfig.getGeometry().getBotRadius() + 80.0f;
	private final float			FINISHED_BALLSPEED	= AIConfig.getPlays().getPassToKeeper().getFinishedBallspeed();
	private int						botID;
	private Vector2f				keeperPosition;
	
	private enum State
	{
		GETTING,
		SHOOTING
	};
	
	private State											innerState;
	
	private BallPossessionCrit							ballPossessionCrit						= null;
	private TigersApproximateScoringChanceCrit	tigersApproximateScoringChanceCrit	= null;
	private TigersPassReceiverCrit					tigersPassReceiverCrit					= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 */
	public PassToKeeperPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.PASS_TO_KEEPER, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.WE);
		tigersApproximateScoringChanceCrit = new TigersApproximateScoringChanceCrit(false);
		tigersPassReceiverCrit = new TigersPassReceiverCrit(false);
		addCriterion(ballPossessionCrit);
		addCriterion(tigersApproximateScoringChanceCrit);
		addCriterion(tigersPassReceiverCrit);
		
		Vector2 viewPoint = new Vector2(new Vector2(AIConfig.getGeometry().getGoalOur().getGoalPostRight().x, AIConfig
				.getGeometry().getGoalOur().getGoalPostRight().y + 1000));
		Vector2 initPosGetter = new Vector2(aiFrame.worldFrame.ball.pos);
		
		getterRole = new BallGetterRole(viewPoint, EGameSituation.GAME);
		passSenderRole = new PassSender(EGameSituation.GAME);
		addAggressiveRole(getterRole, initPosGetter);
		
		innerState = State.GETTING;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		WorldFrame worldFrame = currentFrame.worldFrame;
		Vector2 ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
		Vector2 botPos;
		keeperPosition = currentFrame.worldFrame.tigerBots.get(AIConfig.getGeneral().getKeeperId()).pos;
		
		switch (innerState)
		{
			case GETTING:
				botID = getterRole.getBotID();
				botPos = new Vector2(currentFrame.worldFrame.tigerBots.get(botID).pos);
				float distBotBall = AIMath.distancePP(botPos, ballPos);
				float distBotDest = AIMath.distancePP(getterRole.getDestination(), botPos);
				
				boolean obstacleInTheWay = AIMath.p2pVisibility(worldFrame, botPos, keeperPosition);
				
				if (distBotBall < GOTTEN_DISTANCE && distBotDest < 80 && !obstacleInTheWay)
				{
					switchRoles(getterRole, passSenderRole, currentFrame);
					innerState = State.SHOOTING;
					
				} else
				{
					getterRole.setViewPoint(keeperPosition);
				}
				break;
			
			case SHOOTING:

				passSenderRole.updateRecieverPos(keeperPosition);
				
				if (currentFrame.worldFrame.ball.vel.x > FINISHED_BALLSPEED)
				{
					changeToSucceeded();
				} else
				{
					passSenderRole.forcePass();
				}
		}
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// fail condition: turnover
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY
				|| currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.NO_ONE)
		{
			changeToFailed();
			return;
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean isBallCarrying()
	{
		return true;
	}
}
