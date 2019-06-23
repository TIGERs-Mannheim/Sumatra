/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.06.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;


import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.fieldraster.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ISubPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallDribbler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSender;


/**
 * Play for <a href="http://tigers-mannheim.de/trac/ticket/503">this </a> Ticket.
 * 
 * @author Malte, Gunther
 * */
public class PassTrainingPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID		= 4141333167212154751L;
	
	// --- receiving side
	private PassReceiver				passReceiver;
	
	// --- sending side
	private ARole						currentSenderRole;
	
	private BallGetterRole			ballGetter;
	private BallDribbler				ballDribbler;
	private PassSender				passSender;
	private PassReceiver				passiveReceiver;															// the sender waits and
																															// looks at receiver after
																															// passing - better name?
																															
	private static final int		REC1NO					= 3;
	private static final int		REC2NO					= 5;
	
	private int							currentReceiverRectId;
	private int							currentSenderRectId;
	
	private static final float		GETTING_DISTANCE		= 250;
	private static final float		BALL_LOST_DISTANCE	= 400;
	
	private FieldRasterGenerator	frg						= FieldRasterGenerator.getInstance();
	
	private static final long		TIME_IN_RECEIVEPLAY	= 5;
	
	private ISubPlay					currentSubPlay;
	private ISubPlay					prepareGetPlay;
	private ISubPlay					prepareDribblePlay;
	private ISubPlay					aimPlay;
	private ISubPlay					receivePlay;
	
	
	private enum SubPlayState
	{
		PREPARE_GET,
		PREPARE_DRIBBLE,
		AIM,
		RECEIVE
	};
	
	public SubPlayState	currentSubPlayState;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 * @param wf
	 */
	public PassTrainingPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.PASS_TRAINING, aiFrame);
		
		passReceiver = new PassReceiver();
		ballGetter = new BallGetterRole(EGameSituation.GAME);
		
		prepareGetPlay = new PrepareGetSubPlay();
		prepareDribblePlay = new PrepareDribbleSubPlay();
		aimPlay = new AimSubPlay();
		receivePlay = new ReceiveSubPlay();
		
		currentSenderRole = ballGetter;
		currentSubPlayState = SubPlayState.PREPARE_GET;
		currentSubPlay = prepareGetPlay;
		currentReceiverRectId = REC2NO;
		currentSenderRectId = REC1NO;
		

		addCreativeRole(passReceiver, frg.getRandomPointInPosRec(currentReceiverRectId));
		addAggressiveRole(ballGetter);
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		passReceiver.updateRectangle(REC2NO);
		
		passReceiver.updateSenderId(currentSenderRole.getBotID());
		passReceiver.updateSenderPos(currentSenderRole.getDestination());
		
		ballGetter.setDestTolerance(GETTING_DISTANCE);
		
	}
	

	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		Vector2f ballPos = frame.worldFrame.ball.pos;
		IVector2 senderPos = currentSenderRole.getPos(frame);
		
		switch (currentSubPlayState)
		{
			case PREPARE_GET:
				if (AIMath.distancePP(ballPos, senderPos) < GETTING_DISTANCE)
				{
					switchFromGettingToDribbling(frame);
				}
				break;
			
			case PREPARE_DRIBBLE:
				// distances and switch to Aim handled in SubPlay afterUpdate
				if (AIMath.distancePP(ballPos, senderPos) > BALL_LOST_DISTANCE)
				{
					switchFromDribblingToGetting(frame);
				}
				break;
			
			case AIM:
				// correct aiming, shooting and switching to receive handled in SubPlay afterUpdate
				if (AIMath.distancePP(ballPos, senderPos) > BALL_LOST_DISTANCE)
				{
					switchFromAimToGetting(frame);
				}
				break;
			
			case RECEIVE:
				// switching to Getting handled in TimeOut
				break;
			default:
				log.error("Undefined State in PassTrainingPlay");
				
		}
		
		currentSubPlay.beforeUpdate(frame);
	}
	

	@Override
	protected void timedOut(AIInfoFrame frame)
	{
		if (currentSubPlayState == SubPlayState.RECEIVE)
		{
			switchFromReceiveToGetting(frame);
			return;
		}
		
		if (currentSubPlayState == SubPlayState.AIM)
		{
			switchFromAimToReceive(frame);
			return;
		}
	}
	

	private void switchFromGettingToDribbling(AIInfoFrame frame)
	{
		currentSubPlayState = SubPlayState.PREPARE_DRIBBLE;
		currentSubPlay = prepareDribblePlay;
		
		ballDribbler = new BallDribbler();
		currentSenderRole = ballDribbler;
		switchRoles(ballGetter, ballDribbler, frame);
		
		// ballDribbler.updateDestination(frg.getRandomPointInPosRec(currentSenderRectId));
		ballDribbler.updateDestination(frg.getPositioningRectangle(currentSenderRectId).getMidPoint());
		
		passReceiver.updateSenderId(currentSenderRole.getBotID());
		ballDribbler.setDestConTolerance(500);
		System.out.println("PassTrainingPlay: Getting   =>  Dribbling");
	}
	

	private void switchFromDribblingToGetting(AIInfoFrame frame)
	{
		currentSubPlayState = SubPlayState.PREPARE_GET;
		currentSubPlay = prepareGetPlay;
		
		ballGetter = new BallGetterRole(EGameSituation.GAME);
		currentSenderRole = ballGetter;
		switchRoles(ballDribbler, ballGetter, frame);
		
		passReceiver.updateSenderId(currentSenderRole.getBotID());
		ballGetter.setDestTolerance(GETTING_DISTANCE);
		System.out.println("PassTrainingPlay: Dribbling =>  Getting");
	}
	

	private void switchFromAimToGetting(AIInfoFrame frame)
	{
		currentSubPlayState = SubPlayState.PREPARE_GET;
		currentSubPlay = prepareGetPlay;
		
		ballGetter = new BallGetterRole(EGameSituation.GAME);
		currentSenderRole = ballGetter;
		switchRoles(passSender, ballGetter, frame);
		
		passReceiver.updateSenderId(currentSenderRole.getBotID());
		ballGetter.setDestTolerance(GETTING_DISTANCE);
		System.out.println("PassTrainingPlay: Aiming    =>  Getting");
	}
	

	private void switchFromDribblingToAim(AIInfoFrame frame)
	{
		currentSubPlayState = SubPlayState.AIM;
		currentSubPlay = aimPlay;
		
		passSender = new PassSender(EGameSituation.GAME);
		
		switchRoles(ballDribbler, passSender, frame);
		
		currentSenderRole = passSender;
		passReceiver.updateSenderId(currentSenderRole.getBotID());
		passSender.updateRecieverPos(passReceiver.getDestination());
		System.out.println("PassTrainingPlay: Dribbling =>  Aiming");
	}
	

	private void switchFromAimToReceive(AIInfoFrame frame)
	{
		currentSubPlayState = SubPlayState.RECEIVE;
		currentSubPlay = receivePlay;
		
		passiveReceiver = new PassReceiver();
		currentSenderRole = passiveReceiver;
		switchRoles(passSender, passiveReceiver, frame);
		passiveReceiver.updateRectangle(currentSenderRectId);
		passReceiver.updateSenderId(currentSenderRole.getBotID());
		
		setTimeout(TIME_IN_RECEIVEPLAY);
		resetTimer();
		
		System.out.println("PassTrainingPlay: Aiming    =>  Receiving");
	}
	

	private void switchFromReceiveToGetting(AIInfoFrame frame)
	{
		currentSubPlayState = SubPlayState.PREPARE_GET;
		currentSubPlay = prepareGetPlay;
		
		ballGetter = new BallGetterRole(EGameSituation.GAME);
		
		switchRoles(passReceiver, ballGetter, frame);
		
		passReceiver = new PassReceiver();
		
		switchRoles(passiveReceiver, passReceiver, frame);
		
		// switching sender and receiver rectangleIDs
		
		int tmp = currentReceiverRectId;
		currentReceiverRectId = currentSenderRectId;
		currentSenderRectId = tmp;
		
		passReceiver.updateSenderId(ballGetter.getBotID());
		passReceiver.updateRectangle(currentReceiverRectId);
		
		currentSenderRole = ballGetter;
		ballGetter.setDestTolerance(GETTING_DISTANCE);
		System.out.println("PassTrainingPlay: Receveing =>  Getting");
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		currentSubPlay.afterUpdate(currentFrame);
	}
	
	
	// --------------------------------------------------------------------------
	// --- inner classes --------------------------------------------------------
	// --------------------------------------------------------------------------
	private class PrepareGetSubPlay implements ISubPlay
	{
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			passReceiver.updateSenderPos(ballGetter.getDestination());
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			// idle
		}
		

	}
	
	private class PrepareDribbleSubPlay implements ISubPlay
	{
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			passReceiver.updateSenderPos(ballDribbler.getDestination());
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			if (ballDribbler.isDone(currentFrame.worldFrame))
			{
				setTimeout(Integer.MAX_VALUE);
				switchFromDribblingToAim(currentFrame);
			}
			
		}
		

	}
	
	private class AimSubPlay implements ISubPlay
	{
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			passSender.updateRecieverPos(passReceiver.getDestination());
			passReceiver.updateSenderPos(passSender.getDestination());
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			// check wether both bots are well aimed
			if (passSender.checkReadyToShoot(currentFrame.worldFrame) && passReceiver.isWellAimed(currentFrame.worldFrame))
			{
				System.out.println("PassTrainingPlay Aiming seems allright");
				passSender.forcePass();
				passReceiver.forceReceive();
				
				setTimeout(1);
				resetTimer();
				
			} else
			{
				System.out.println("PassTrainingPlay Aiming seems NOT allright");
			}
		}
		

	}
	
	private class ReceiveSubPlay implements ISubPlay
	{
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			// idle - feel free to add any receiving-procedure in this place
			// since the passReceiver instance is the same than before, all information
			// is still to be found in the instance itself, no transitions necessary
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			// idle
		}
		

	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
