/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.05.2011
 * Author(s):
 * TobiasK
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PullBackRole;


/**
 * This play shall be selected if ball possession is BOTH.
 * 
 * @author TobiasK
 * 
 */
public class PullBackPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID		= 7920263055813774341L;
	
	private BallGetterRole		getter;
	private PullBackRole			pullBack;
	
	private BallPossessionCrit	ballPossessionCrit	= null;
	
	private State					innerState;
	private boolean				pullbackStarted			= false;
	
	private enum State
	{
		GETTING,
		PULLBACK
	};
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 */
	public PullBackPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.PULL_BACK, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.BOTH);
		addCriterion(ballPossessionCrit);
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		
		getter = new BallGetterRole(EGameSituation.GAME);
		pullBack = new PullBackRole();
		addAggressiveRole(getter, ballPos);
		
		innerState = State.GETTING;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame currentFrame)
	{
	
	}
	

	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		getter.update(currentFrame);
		
		switch (innerState)
		{
			case GETTING:

				if(currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.BOTH || currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY){
					TrackedBot opponentBallGetter = currentFrame.tacticalInfo.getOpponentBallGetter();
					Vector2 imaginaryViewPoint = new Vector2((float)(opponentBallGetter.pos.x + Math.cos(opponentBallGetter.angle + Math.PI) * 400),(float)(opponentBallGetter.pos.y + Math.sin(opponentBallGetter.angle + Math.PI) * 400));
					getter.setViewPoint(imaginaryViewPoint);
				}
			
				if (getter.checkAllConditions(currentFrame))
				{
					switchRoles(getter, pullBack, currentFrame);
					resetTimer();
					setTimeout(1);
					innerState = State.PULLBACK;
				}
				return;
			case PULLBACK:
				if (pullBack.checkAllConditions(currentFrame) && pullbackStarted)
				{
					changeToSucceeded();
				}
		}
	}
	
	
	
	@Override
	protected void timedOut()
	{
		pullBack.makePullback();
		pullbackStarted = true;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}