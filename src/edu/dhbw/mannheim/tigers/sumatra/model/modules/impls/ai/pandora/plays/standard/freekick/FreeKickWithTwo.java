/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.07.2011
 * Author(s): Vendetta
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ISubPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.IndirectShooter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.IndirectShooter.PositionLimitation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSender;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.Shooter;


/**
 * Handles FreeKicks of the Tigers,
 * will try to shoot directly if > then 50cm from enemy baseline, else try to pass because the angle
 * to the goal is to small, mostly being corner-kicks.
 * 
 * This Play will NOT handle shooting, but will rather be set to completed after the pass is shot,
 * if everything runs perfectly the IndirectShooter will have shot the ball at the goal via kickArm,
 * if not, a new Play is chosen, preferably the DirectShooterPlay.
 * 
 * @author GuntherB
 * 
 */
public class FreeKickWithTwo extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	

	/**  */
	private static final long	serialVersionUID	= -5170196936362908938L;
	
	/** how many seconds this play tries to be successful */
	private static final int	TIME_OUT				= 6;
	
	// sending side
	private BallGetterRole		getterS;
	private PassSender			senderS;
	private Shooter				shooterS;
	
	private IndirectShooter		indirectShooter;
	

	// private final float BOT_RADIUS = AIConfig.getGeometry().getBotRadius();
	// private final float BALL_RADIUS = AIConfig.getGeometry().getBallRadius();
	
	// best practice from GetAndShootRole
	private final float			GET_DISTANCE		= 500;							// BALL_RADIUS + BOT_RADIUS + 80.0f; // from
																									// 60
																									

	// GETTING PLAY WILL ONLY BE USED FOR FIRST FRAME, HOTFIX, just don't think about it
	private static enum PlayState
	{
		GETTING,
		PASSING,
		SHOOTING
	};
	
	private PlayState			playstate				= PlayState.GETTING;
	
	private GettingPlay		gettingPlay				= new GettingPlay();
	private PassingPlay		passingPlay				= new PassingPlay();
	private ShootingPlay		shootingPlay			= new ShootingPlay();
	
	private ISubPlay			currentSubPlay			= gettingPlay;
	private ARole				currentRoleSender;
	
	/** NOTE: this flag is misused, HOTFIX, it will now determine, whether the ballgetter has done it's job */
	private boolean			refIsReady				= false;
	
	private boolean			TRY_PASSING;
	
	private static boolean	PASSING_FLICK_FLACK	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 * @param wf
	 */
	public FreeKickWithTwo(AIInfoFrame aiFrame)
	{
		super(EPlay.FREEKICK_WITH_TWO, aiFrame);
		
		WorldFrame wf = aiFrame.worldFrame;
		
		getterS = new BallGetterRole(AIConfig.getGeometry().getGoalTheir().getGoalCenter(), EGameSituation.GAME); // from
		getterS.setDestTolerance(30); // set-piece
		addAggressiveRole(getterS);
		currentRoleSender = getterS;
		

		// int rectangleID = wf.ball.pos.y >= 0 ? 6 : 2; // position top when ball is on bottom, and other way
		int rectangleID = wf.ball.pos.y >= 0 ? 7 : 3; // position top when ball is on bottom, and other way
		// int rectangleID = 4;
		
		// around
		indirectShooter = new IndirectShooter(PositionLimitation.FREE, rectangleID);
		addCreativeRole(indirectShooter, new Vector2(0, 0));
		indirectShooter.findNewTarget(aiFrame);	// Set valid init destination
		
		setTimeout(TIME_OUT);
		
		// if ball is closer than 50cm to enemy baseline, try passing
		TRY_PASSING = wf.ball.pos.x > AIConfig.getGeometry().getFieldLength() / 2 - 500;
		
		PASSING_FLICK_FLACK = !PASSING_FLICK_FLACK;
		
		if (PASSING_FLICK_FLACK)
		{
			TRY_PASSING = true;
		}
		
		// NO PASSING HACK!
		
		TRY_PASSING = false;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		indirectShooter.setSenderId(getterS.getBotID());
		indirectShooter.findNewTarget(frame);
		
	}
	

	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		
		// old, referee-related flag-handling
		// if (!refIsReady && frame.refereeMsg != null && frame.refereeMsg.cmd == ERefereeCommand.Ready)
		// {
		// refIsReady = true;
		// }
		

		switch (playstate)
		{
			case GETTING:

				// new, distance-related
				Vector2 ballPos = new Vector2(frame.worldFrame.ball.pos);
				Vector2 botPos = new Vector2(frame.worldFrame.tigerBots.get(getterS.getBotID()).pos);
				float distanceBotBall = AIMath.distancePP(botPos, ballPos);
				float targetViewAngle = AIMath.angleBetweenXAxisAndLine(botPos, ballPos);
				float currentBotViewAngle = frame.worldFrame.tigerBots.get(getterS.getBotID()).angle;
				

				float angleDifference = Math.abs(currentBotViewAngle - targetViewAngle);
				
				System.out.println("FreeKickPlay: Distance is " + (distanceBotBall < GET_DISTANCE) + " || Angle is "
						+ (angleDifference < AIMath.PI / 6));
				if (getterS.hasReachedDestination(frame)) // distanceBotBall < GET_DISTANCE && angleDifference < AIMath.PI /
																		// 6) // AIMath.deg2rad(AIConfig.getTolerances().getViewAngle()))
				{
					refIsReady = true;
				}
				

				if (refIsReady && TRY_PASSING)
				{
					switchFromGettingToPassing(frame);
					
				} else
				{
					if (refIsReady)
					{
						switchToShooting(frame);
					}
				}
				break;
			
			case PASSING:
				// idle, switches by timeout
				break;
			
			case SHOOTING:
				// idle, no turning back, now
				break;
			
		}
		
		currentSubPlay.beforeUpdate(frame);
	}
	

	private void switchFromGettingToPassing(AIInfoFrame currentFrame)
	{
		playstate = PlayState.PASSING;
		currentSubPlay = passingPlay;
		
		senderS = new PassSender(EGameSituation.SET_PIECE);
		
		switchRoles(getterS, senderS, currentFrame);
		currentRoleSender = senderS;
		
		indirectShooter.setSenderId(senderS.getBotID());
		
		resetTimer();
		setTimeout(TIME_OUT);
	}
	

	private void switchToShooting(AIInfoFrame currentFrame)
	{
		playstate = PlayState.SHOOTING;
		currentSubPlay = shootingPlay;
		
		shooterS = new Shooter(EGameSituation.SET_PIECE);
		
		switchRoles(currentRoleSender, shooterS, currentFrame);
		currentRoleSender = shooterS;
		
		resetTimer();
		setTimeout(5);
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		currentSubPlay.afterUpdate(currentFrame);
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY)
		{
			changeToFailed();
		}
	}
	

	@Override
	protected void timedOut(AIInfoFrame currentFrame)
	{
		// if ready command has been given AND time is over, end this play
		// however, don't react in 'preparing'-time before ready-signal
		if (refIsReady)
		{
			switchToShooting(currentFrame);
			return;
		}
		

		// desperate shooting mode activated
		if (playstate == PlayState.SHOOTING)
		{
			float orientation = currentFrame.worldFrame.tigerBots.get(shooterS.getBotID()).angle;
			
			if (orientation > -AIMath.PI_HALF && orientation < AIMath.PI_HALF)
			{
				shooterS.forceShot();
			}
			resetTimer();
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- inner plays --------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public class GettingPlay implements ISubPlay
	{
		
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			indirectShooter.updateSenderPos(getterS.getDestination());
			if (TRY_PASSING)
			{
				getterS.setViewPoint(AIMath.getKickerPosFromBot(currentFrame.worldFrame, indirectShooter.getBotID()));
			} else
			{
				getterS.setViewPoint(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
			}
			
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			
		}
		
	}
	
	
	public class PassingPlay implements ISubPlay
	{
		
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			indirectShooter.updateSenderPos(senderS.getDestination());
			
			senderS.updateRecieverPos(AIMath.getKickerPosFromBot(currentFrame.worldFrame, indirectShooter.getBotID()));
			
			if (AIMath.distancePP(senderS.getPos(currentFrame), currentFrame.worldFrame.ball.pos) > 750)
			{
				changeToSucceeded(); // guess we shot the ball, so please just get it over with
			}
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			WorldFrame worldFrame = currentFrame.worldFrame;
			
			// if sender is ready to shoot and receiver is ready to take the ball
			if (senderS.checkReadyToShoot(worldFrame) && indirectShooter.checkSenderIsVisible(worldFrame))
			{
				// // if receiver can also see target
				// if (indirectShooter.checkTargetIsVisible(worldFrame))
				// {
				// senderS.forcePass();
				// indirectShooter.forceDirectShot();
				//
				// } else
				// {
				// // idle
				// }
				
				senderS.forcePass();
				indirectShooter.forceDirectShot();
			}
		}
		
	}
	
	
	public class ShootingPlay implements ISubPlay
	{
		
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			indirectShooter.updateSenderPos(shooterS.getDestination());
			if (AIMath.distancePP(shooterS.getPos(currentFrame), currentFrame.worldFrame.ball.pos) > 750)
			{
				changeToSucceeded(); // guess we shot the ball, so please just get it over with
			}
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			if (shooterS.isReadyToShoot(currentFrame.worldFrame))
			{
				shooterS.forceShot();
			}
		}
		
	}
	
	
	@Override
	public boolean isBallCarrying()
	{
		return true;
	}
}
