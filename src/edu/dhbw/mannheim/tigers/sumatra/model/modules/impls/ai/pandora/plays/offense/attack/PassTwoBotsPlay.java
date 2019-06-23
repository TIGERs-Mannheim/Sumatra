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
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSender;


/**
 * This Play tries to achieve the standard pass between two bots, trying
 * to position the {@link PassReceiver} in a position, where it can be passed
 * at and also see the goal.
 * 
 * @author GuntherB
 * 
 *         TODO GuntherB , reevaluate this class pls.. currentActiveRole, Viewports etc (by GuntherB)
 * 
 */
public class PassTwoBotsPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// roles, inhibited by the bot that startet on the sender side are marked with an "S"
	
	/**  */
	private static final long	serialVersionUID		= -8789539163775256162L;
	// -- sending side --
	private BallGetterRole		getterS;
	private PassSender			senderS;
	private PassReceiver			receiverS;
	
	// -- receiving side --
	private PassReceiver			receiverR;
	private BallGetterRole		getterR;
	
	// -- subplays --
	
	private GettingPlay			gettingPlay;
	private PassingPlay			passingPlay;
	private ReceivingPlay		receivingPlay;
	
	private ISubPlay				currentSubPlay;
	// private ARole currentActiveRoleR;
	private ARole					currentActiveRoleS;
	
	// distance until ball is considered "gotten"
	private final float			GET_DISTANCE			= AIConfig.getGeometry().getBallRadius()
																			+ AIConfig.getGeometry().getBotRadius() + 50.0f;
	// distance when a bot has to get the ball again
	private final float			GET_AGAIN_DISTANCE	= AIConfig.getGeometry().getBallRadius()
																			+ AIConfig.getGeometry().getBotRadius() + 150.0f;
	

	private final int				STARTING_RECT_R		= 2;
	private final int				STARTING_RECT_S		= 6;
	
	private enum State
	{
		GETTING,
		PASSING,
		RECEIVING
	};
	
	private State	innerState;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 */
	public PassTwoBotsPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.PASS_TWO_BOTS, aiFrame);
		
		// --- sender --
		getterS = new BallGetterRole(EGameSituation.GAME);
		senderS = new PassSender(EGameSituation.GAME);
		receiverS = new PassReceiver();
		
		addAggressiveRole(getterS);
		
		// --- receiver ---
		receiverR = new PassReceiver();
		getterR = new BallGetterRole(EGameSituation.GAME);
		
		receiverR.updateRectangle(STARTING_RECT_R);
		addCreativeRole(receiverR, FieldRasterGenerator.getInstance().getRandomPointInPosRec(STARTING_RECT_R));
		
		// --- plays ---
		gettingPlay = new GettingPlay();
		passingPlay = new PassingPlay();
		receivingPlay = new ReceivingPlay();
		
		// --- initial state ---
		innerState = State.GETTING;
		currentSubPlay = gettingPlay;
		currentActiveRoleS = getterS;
		// currentActiveRoleR = receiverR;
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		Vector2 senderPos = new Vector2(currentFrame.worldFrame.tigerBots.get(currentActiveRoleS.getBotID()).pos);
		Vector2 ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
		float distBotBall = AIMath.distancePP(senderPos, ballPos);
		
		switch (innerState)
		{
			case GETTING:
				if (distBotBall < GET_DISTANCE)
				{
					switchGetting2Passing(currentFrame);// did not get before, but is within distance
				}
				break;
			
			case PASSING:
				if (senderS.isPassDone())
				{
					switchPassing2Receiving(currentFrame);
				} else
				{
					if (distBotBall >= GET_AGAIN_DISTANCE)
					{
						switchPassing2Getting(currentFrame);// did already get, but is now not in distance anymore
					}
				}
				break;
			
			case RECEIVING:
				// nothing
				break;
		}
		
		currentSubPlay.beforeUpdate(currentFrame);
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		currentSubPlay.afterUpdate(currentFrame);
	}
	

	private void switchGetting2Passing(AIInfoFrame currentFrame)
	{
		senderS.updateRecieverPos(receiverR.getDestination());
		
		innerState = State.PASSING;
		currentActiveRoleS = senderS;
		currentSubPlay = passingPlay;
		
		switchRoles(getterS, senderS, currentFrame);
	}
	

	private void switchPassing2Getting(AIInfoFrame currentFrame)
	{
		getterS = new BallGetterRole(EGameSituation.GAME);
		
		getterS.setViewPoint(senderS.getTarget());
		
		innerState = State.GETTING;
		currentActiveRoleS = getterS;
		currentSubPlay = gettingPlay;
		
		switchRoles(senderS, getterS, currentFrame);
	}
	

	private void switchPassing2Receiving(AIInfoFrame currentFrame)
	{
		receiverS = new PassReceiver();
		receiverS.updateRectangle(STARTING_RECT_S);
		
		getterR = new BallGetterRole(EGameSituation.GAME);
		// getterR.setViewPoint(receiverR.getTarget()); i don't see this as necessary, but i probably thought of something
		// earlier...
		
		innerState = State.RECEIVING;
		currentActiveRoleS = receiverS;
		// currentActiveRoleR = getterR;
		currentSubPlay = receivingPlay;
		
		switchRoles(senderS, receiverS, currentFrame);
		switchRoles(receiverR, getterR, currentFrame);
		
		receiverS.updateSenderId(getterR.getBotID());
		// receiverS.updateTarget(newTarget); //TODO let the receiver automatically generate targets
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// INNER CLASSES => SUBPLAYS
	// --------------------------------------------------------------------------
	
	// SubPlay Getting the ball
	private class GettingPlay implements ISubPlay
	{
		
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY)
			{
				changeToFailed();
				return;
			}
			
			receiverR.updateSenderPos(getterS.getDestination());
			receiverR.updateSenderId(getterS.getBotID());
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			getterS.setViewPoint(receiverR.getDestination());
		}
	}
	
	// SubPlay Passing at receiver
	private class PassingPlay implements ISubPlay
	{
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY)
			{
				changeToFailed();
				return;
			}
			
			receiverR.updateSenderPos(senderS.getDestination());
			receiverR.updateSenderId(senderS.getBotID());
			senderS.updateRecieverPos(receiverR.getDestination());
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			
			WorldFrame worldFrame = currentFrame.worldFrame;
			
			// if sender is ready to shoot and receiver is ready to take the ball
			if (senderS.checkReadyToShoot(worldFrame) && receiverR.checkLooksAtSender(worldFrame)
					&& receiverR.checkSenderIsVisible(worldFrame))
			{
				
				// if receiver can also see target
				if (receiverR.checkTargetIsVisible(worldFrame))
				{
					senderS.forcePass();
					receiverR.forceReceive();
				} else
				{
					// if certain other things are true, like a dangerous situation, where we need to get the ball
					// going, you may force a pass here, too
				}
			}
		}
	}
	
	// SubPlay Receiving the ball
	private class ReceivingPlay implements ISubPlay
	{
		@Override
		public void beforeUpdate(AIInfoFrame currentFrame)
		{
			if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY)
			{
				changeToFailed();
				return;
			}
			
			Vector2 senderPos = new Vector2(currentFrame.worldFrame.tigerBots.get(currentActiveRoleS.getBotID()).pos);
			Vector2 ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
			float distBotBall = AIMath.distancePP(senderPos, ballPos);
			
			if (distBotBall < GET_DISTANCE)
			{
				changeToSucceeded();
				return;
			}
			
			receiverS.updateSenderPos(getterR.getDestination());
			receiverS.updateSenderId(getterR.getBotID());
		}
		

		@Override
		public void afterUpdate(AIInfoFrame currentFrame)
		{
			
		}
	}
	
	
	@Override
	public boolean isBallCarrying()
	{
		return true;
	}
}