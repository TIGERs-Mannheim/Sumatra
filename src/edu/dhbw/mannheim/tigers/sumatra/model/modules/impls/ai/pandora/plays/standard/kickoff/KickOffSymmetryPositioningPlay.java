/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.01.2011
 * Author(s): Vendetta
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.fieldraster.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ISubPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.IndirectShooter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSender;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.Shooter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.Shooter.EShootingMode;


/**
 * Offense Kickoff procedure - the ball Sender will kick off to one of two
 * independent robots (passReceivers), which will try to position well to score
 * a direct goal. While the Receivers roam around at the middle line, the play will
 * decide to which receiver the sender shall pass and do so.
 * 
 * 
 * @author GuntherB
 */
public class KickOffSymmetryPositioningPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// roles, inhibited by the bot that started on the sender side are marked with an "S"
	
	private static final long	serialVersionUID	= 8128898639417055928L;
	
	/** how many seconds this play tries to be successfull */
	private static final int	TIME_OUT				= 1;
	
	// -- sending side --
	private BallGetterRole		getterS;
	private PassSender			senderS;
	private Shooter				shooterS;
	
	// -- receiving side 1 --
	private IndirectShooter		shooter1;
	
	// -- receiving side 2 --
	private IndirectShooter		shooter2;
	
	// -- subplays --
	
	private PreparingPlay		preparingPlay;
//	private PassingPlay			passingPlay;
	private ShootPlay				desperateShootPlay;
	private SafeShootPlay		safeShootPlay;
	
	private ISubPlay				currentSubPlay;
	private ARole					currentRoleSender;
	
	private boolean				refIsReady			= false;
	

	/** the chosen shooter */
	private IndirectShooter		theChosenOne;
	
	
	private enum State
	{
		PREPARING,
		PASSING,
		DESPERATE_SHOOT,
		SAFE_SHOOT
	};
	
	private State	innerState;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 * @param wf
	 */
	public KickOffSymmetryPositioningPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.KICK_OF_US_SYMMETRY_POSITION, aiFrame);
		

		// --- sender --
		getterS = new BallGetterRole(EGameSituation.SET_PIECE);
		Vector2 getterStartCoord = AIConfig.getGeometry().getCenter().addNew(new Vector2(-500, 0));
		

		// --- shooter1 ---
		shooter1 = new IndirectShooter(IndirectShooter.PositionLimitation.KICKOFF, 2);
		shooter1.setRectangle(FieldRasterGenerator.getInstance().getPositioningRectangle(2));
		
		// --- shooter2 ---
		shooter2 = new IndirectShooter(IndirectShooter.PositionLimitation.KICKOFF, 6);
		shooter2.setRectangle(FieldRasterGenerator.getInstance().getPositioningRectangle(6));
		
		// --- plays ---
		preparingPlay = new PreparingPlay();
//		passingPlay = new PassingPlay();
		desperateShootPlay = new ShootPlay();
		safeShootPlay = new SafeShootPlay();
		
		// --- initial innerState ---
		innerState = State.PREPARING;
		currentSubPlay = preparingPlay;
		currentRoleSender = getterS;
		
		// --- adding roles ---
		addAggressiveRole(currentRoleSender, getterStartCoord);
		addAggressiveRole(shooter1, new Vector2(-500, AIConfig.getGeometry().getFieldWidth() / 4.0f));
		addAggressiveRole(shooter2, new Vector2(-500, AIConfig.getGeometry().getFieldWidth() / -4.0f));
		

		setTimeout(TIME_OUT);
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected void beforeFirstUpdate(AIInfoFrame currentFrame)
	{
		shooter1.updateSenderPos(getterS.getDestination());
		shooter1.setSenderId(getterS.getBotID());
		shooter1.findNewTarget(currentFrame);
		

		shooter2.updateSenderPos(getterS.getDestination());
		shooter2.setSenderId(getterS.getBotID());
		shooter2.findNewTarget(currentFrame);
		
		getterS.setDestTolerance(350);
		getterS.setViewPoint(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
	}
	

	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		
		if (!refIsReady && currentFrame.refereeMsg != null && currentFrame.refereeMsg.cmd == ERefereeCommand.Ready)
		{
			refIsReady = true;
			resetTimer();
		}
		
		switch (innerState)
		{
			case PREPARING:
				// if (shooter1.isPrepared(currentFrame.worldFrame) && refIsReady)
				// {
				// switchFromPreparingToPassing(currentFrame, shooter1);
				// } else
				// {
				// if (shooter2.isPrepared(currentFrame.worldFrame) && refIsReady)
				// {
				// switchFromPreparingToPassing(currentFrame, shooter2);
				// }
				// }
				break;
			
			case PASSING:
				if (!theChosenOne.isPrepared(currentFrame.worldFrame))
				{
					switchFromPassingToPreparing(currentFrame);
				}
				
				break;
			
		}
		
		currentSubPlay.beforeUpdate(currentFrame);
		
		// debug
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY)
		{
			changeToFailed();
		}
		
	}
	

//	private void switchFromPreparingToPassing(AIInfoFrame currentFrame, IndirectShooter chosenOne)
//	{
//		theChosenOne = chosenOne;
//		
//		innerState = State.PASSING;
//		currentSubPlay = passingPlay;
//		
//		senderS = new PassSender(EGameSituation.SET_PIECE);
//		switchRoles(getterS, senderS, currentFrame);
//		
//		currentRoleSender = senderS;
//		senderS.updateRecieverPos(theChosenOne.getDestination());
//		resetTimer();
//	}
	

	private void switchFromPassingToPreparing(AIInfoFrame currentFrame)
	{
		innerState = State.PREPARING;
		currentSubPlay = preparingPlay;
		
		getterS = new BallGetterRole(EGameSituation.SET_PIECE);
		
		switchRoles(senderS, getterS, currentFrame);
		
		currentRoleSender = senderS;
	}
	

	private void switchToShooting(AIInfoFrame currentFrame)
	{
		innerState = State.DESPERATE_SHOOT;
		currentSubPlay = desperateShootPlay;
		
		shooterS = new Shooter(EGameSituation.SET_PIECE);
		
		switchRoles(currentRoleSender, shooterS, currentFrame);
		currentRoleSender = shooterS;
		
		resetTimer();
		setTimeout(3);
	}
	

	private void switchToSafeShot(AIInfoFrame currentFrame)
	{
		innerState = State.SAFE_SHOOT;
		currentSubPlay = safeShootPlay;
		
		shooterS = new Shooter(EGameSituation.SET_PIECE);
		
		switchRoles(currentRoleSender, shooterS, currentFrame);
		
		shooterS.setMode(EShootingMode.SAFESHOT);
		
		currentRoleSender = shooterS;
		resetTimer();
		setTimeout(3);
	}
	

	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		currentSubPlay.afterUpdate(currentFrame);
		
	}
	

	@Override
	protected void timedOut(AIInfoFrame frame)
	{
		if (!refIsReady)
		{
			return;
		}
		// if ready command has been given AND time is over, end this play
		if (innerState == State.PASSING || innerState == State.PREPARING)
		{
			Vector2f endPoint = new Vector2f(AIConfig.getGeometry().getFieldLength() / 6, 0);
			if (AIMath.p2pVisibility(frame.worldFrame, frame.worldFrame.ball.pos, endPoint, 250,
					currentRoleSender.getBotID()))
			{
				System.out.println("KICKOFFSYMMETRY: Switch to Shooting");
				switchToShooting(frame);
			} else
			{
				switchToSafeShot(frame);
				System.out.println("KICKOFFSYMMETRY: Switch to SafeShooting");
			}
			

			return;
		}
		
		if (innerState == State.DESPERATE_SHOOT)
		{
			float orientation = frame.worldFrame.tigerBots.get(shooterS.getBotID()).angle;
			
			if (orientation > -AIMath.PI_QUART && orientation < AIMath.PI_QUART)
			{
				shooterS.forceShot();
			}
			resetTimer();
		}
		
		if (innerState == State.SAFE_SHOOT)
		{
			float orientation = frame.worldFrame.tigerBots.get(shooterS.getBotID()).angle;
			
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
	// INNER CLASSES => SUBPLAYS
	// --------------------------------------------------------------------------
	
	// SubPlay Getting the ball
	private class PreparingPlay implements ISubPlay
	{
		
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			shooter1.updateSenderPos(getterS.getDestination());
			shooter2.updateSenderPos(getterS.getDestination());
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			// idle
		}
		

	}
	
//	// SubPlay Passing at receiver
//	private class PassingPlay implements ISubPlay
//	{
//		@Override
//		public void beforeUpdate(AIInfoFrame currentFrame)
//		{
//			
//			shooter1.updateSenderPos(senderS.getDestination());
//			shooter2.updateSenderPos(senderS.getDestination());
//			
//			senderS.updateRecieverPos(AIMath.getKickerPosFromBot(currentFrame.worldFrame, theChosenOne.getBotID()));
//		}
//		
//
//		@Override
//		public void afterUpdate(AIInfoFrame currentFrame)
//		{
//			WorldFrame worldFrame = currentFrame.worldFrame;
//			
//			// if sender is ready to shoot and receiver is ready to take the ball
//			if (senderS.checkReadyToShoot(worldFrame) && theChosenOne.checkSenderIsVisible(worldFrame))
//			{
//				// if receiver can also see target
//				if (theChosenOne.checkTargetIsVisible(worldFrame))
//				{
//					senderS.forcePass();
//					theChosenOne.forceDirectShot();
//					
//				} else
//				{
//					// idle
//				}
//			}
//			
//			if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY)
//			{
//				changeToFailed();
//			}
//			
//			if (AIMath.distancePP(senderS.getPos(currentFrame), currentFrame.worldFrame.ball.pos) > 750
//					&& AIMath.distancePL(currentFrame.worldFrame.ball.pos, senderS.getPos(currentFrame),
//							theChosenOne.getPos(currentFrame)) > 500)
//			{
//				changeToSucceeded(); // we assume here, that the ball has been passed and shot
//			}
//		}
//	}
	
	public class ShootPlay implements ISubPlay
	{
		
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			shooter1.updateSenderPos(shooterS.getDestination());
			shooter2.updateSenderPos(shooterS.getDestination());
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
	
	public class SafeShootPlay implements ISubPlay
	{
		
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			shooter1.updateSenderPos(shooterS.getDestination());
			shooter2.updateSenderPos(shooterS.getDestination());
			
			if (AIMath.distancePP(shooterS.getPos(currentFrame), currentFrame.worldFrame.ball.pos) > 750)
			{
				changeToSucceeded(); // we assume here, that the ball has been shot
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
