/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ISubPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.Shooter;


/**
 * This Play shall be selected if there is an approximate scoring chance for us.
 * It does nothing else than letting a bot aim and shot on the goal.
 * 
 * @author GuntherB
 * 
 */
public class DirectShotPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID		= -3327836207551817038L;
	
	private final float			BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
	private final float			BALL_RADIUS				= AIConfig.getGeometry().getBallRadius();
	
	private BallGetterRole		getter;
	private Shooter				shooter;
	
	private boolean				forcedShot				= false;
	private boolean				desperateShoot			= false;
	
	private GettingPlay			gettingPlay;
	private ShootingPlay			shootingPlay;
	
	private ISubPlay				currentSubPlay;
	private ARole					currentActiveSenderRole;
	
	private boolean				incAimingTolerance	= false;
	
	private enum State
	{
		GETTING,
		SHOOTING
	};
	
	private State					innerState;
	
	private BallPossessionCrit	ballPossessionCrit	= null;
	// private TigersApproximateScoringChanceCrit tigersApproximateScoringChanceCrit = null;
	
	private final float			GET_DISTANCE			= BALL_RADIUS + BOT_RADIUS + 80.0f;	// from 60
	private final float			GET_AGAIN_DISTANCE	= BALL_RADIUS + BOT_RADIUS + 150.0f;
	private final float			BALL_LOST_DISTANCE	= BALL_RADIUS + BOT_RADIUS + 500;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 */
	public DirectShotPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.DIRECT_SHOT, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.WE, EBallPossession.BOTH);
		// tigersApproximateScoringChanceCrit = new TigersApproximateScoringChanceCrit(true);
		addCriterion(ballPossessionCrit);
		// addCriterion(tigersApproximateScoringChanceCrit);
		
		IVector2 initPos = aiFrame.worldFrame.ball.pos;
		Vector2 goalCenter = new Vector2(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		
		getter = new BallGetterRole(goalCenter, EGameSituation.GAME);
		shooter = new Shooter(EGameSituation.GAME); // note: usually, only the current active role is initiated, but here
																	// we need the shooter
		// to calculate a target in parallel
		
		gettingPlay = new GettingPlay();
		shootingPlay = new ShootingPlay();
		
		// --- initial innerState ---
		currentSubPlay = gettingPlay;
		currentActiveSenderRole = getter;
		
		addAggressiveRole(getter, initPos);
		
		innerState = State.GETTING;
		
		// after 3 seconds start aiming with bigger tolerance
		setTimeout(3);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		
		Vector2 ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
		Vector2 botPos = new Vector2(currentFrame.worldFrame.tigerBots.get(currentActiveSenderRole.getBotID()).pos);
		float distanceBotBall = AIMath.distancePP(botPos, ballPos);
		
		float targetViewAngle = AIMath.angleBetweenXAxisAndLine(botPos, ballPos);
		float currentBotViewAngle = currentFrame.worldFrame.tigerBots.get(currentActiveSenderRole.getBotID()).angle;
		
		float angleDifference = Math.abs(currentBotViewAngle - targetViewAngle);
		
		switch (innerState)
		{
			case GETTING:

				System.out.println("DirectShootPlay: Distance is " + (distanceBotBall < GET_DISTANCE) + " || Angle is "
						+ (angleDifference < AIMath.PI / 6));
				if (distanceBotBall < GET_DISTANCE && angleDifference < AIMath.PI / 6) // AIMath.deg2rad(AIConfig.getTolerances().getViewAngle()))
				{
					switchFromGettingToShooting(currentFrame);
				}
				break;
			
			case SHOOTING:
				System.out.println("DirectShootPlay is in Shooting");
				if (distanceBotBall > GET_AGAIN_DISTANCE || angleDifference > (AIMath.PI / 6) * 1.2f) // AIMath.deg2rad(AIConfig.getTolerances().getViewAngle())
																																	// * 1.2f)
				{
					switchFromShootingToGetting(currentFrame);
				}
				break;
		}
		
		currentSubPlay.beforeUpdate(currentFrame);
		
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY)
		{
			System.out.println("DirectShotPlay: Failed, they got the ball");
			changeToFailed();
		}
	}
	

	private void switchFromGettingToShooting(AIInfoFrame currentFrame)
	{
		innerState = State.SHOOTING;
		currentSubPlay = shootingPlay;
		
		shooter = new Shooter(EGameSituation.GAME);
		switchRoles(getter, shooter, currentFrame);
		currentActiveSenderRole = shooter;
		
	}
	

	private void switchFromShootingToGetting(AIInfoFrame currentFrame)
	{
		innerState = State.GETTING;
		currentSubPlay = gettingPlay;
		
		Vector2 goalCenter = new Vector2(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		getter = new BallGetterRole(goalCenter, EGameSituation.GAME);
		
		switchRoles(shooter, getter, currentFrame);
		
		currentActiveSenderRole = getter;
	}
	

	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		currentSubPlay.afterUpdate(currentFrame);
	}
	

	@Override
	protected void timedOut()
	{
		// in order of appearance
		
		// after 5 seconds from start, increase your aiming tolerance
		if (incAimingTolerance == false)
		{
			incAimingTolerance = true;
			setTimeout(5);
			return;
		}
		
		// whatever, just try
		// 'You miss 100% of the shots you don't take'
		// after 5 more seconds (see above), you can just arm and shoot whereever
		if (incAimingTolerance && !desperateShoot && !forcedShot)
		{
			// System.out.println("DirectShootPlay was told to perform a desperate shot because time's up");
			desperateShoot = true;
			resetTimer();
			setTimeout(2);
			return;
		}
		
		// if role was told to shoot NOW, it has 2 second time to do so per kickauto
		// else => desperateShot
		if (forcedShot && !desperateShoot)
		{
			desperateShoot = true;
			setTimeout(2);
			resetTimer();
			// System.out.println("DirectShootPlay was told to perform a desperate shot");
			return;
		}
		
		// and one more second to perform its desperate shot
		// else => fail
		if (desperateShoot)
		{
			System.out.println("DirectShootPlay was told to perform a desperate shot and went over time");
			changeToFailed();
		}
	}
	
	// --------------------------------------------------------------------------
	// INNER CLASSES => SUBPLAYS
	// --------------------------------------------------------------------------
	
	// SubPlay Getting the ball
	private class GettingPlay implements ISubPlay
	{
		
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			getter.setViewPoint(shooter.findNewTarget(currentFrame));
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			// idle
		}
	}
	
	// SubPlay Shooting at our opponent's goal
	private class ShootingPlay implements ISubPlay
	{
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			if (incAimingTolerance)
			{
				shooter.incAimingTolerance();
			}
			
			if (desperateShoot)
			{
				shooter.shootDesperately();
			}
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			Vector2 ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
			Vector2 botPos = new Vector2(currentFrame.worldFrame.tigerBots.get(currentActiveSenderRole.getBotID()).pos);
			float distanceBotBall = AIMath.distancePP(botPos, ballPos);
			
			if (shooter.isReadyToShoot(currentFrame.worldFrame))
			{
				resetTimer();
				setTimeout(2);
				forcedShot = true;
				
				shooter.forceShot();
			}
			
			if (distanceBotBall > BALL_LOST_DISTANCE)
			{
				System.out.println("DirectShotPlay: Failed, to far from ball");
				changeToFailed();
			}
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