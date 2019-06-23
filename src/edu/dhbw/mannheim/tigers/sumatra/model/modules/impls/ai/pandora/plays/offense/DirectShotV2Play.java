/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.11.2012
 * Author(s): jan
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV2Role;


/**
 * Makes the direct ShotPlay more effective and try to save time during the gameplay.
 * First this play try to get the ball with the ball getter-role, afterwards it will
 * shoot to the best point.
 * 
 * @author jan
 * 
 */
public class DirectShotV2Play extends AOffensivePlay

{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log	= Logger.getLogger(DirectShotV2Play.class.getName());
	
	private final BallGetterRole	ballGetter;
	private final ShooterV2Role	shooter;
	private EState						state	= EState.GET;
	
	enum EState
	{
		GET,
		SHOOT;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public DirectShotV2Play(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		addCriterion(new BallPossessionCrit(EBallPossession.NO_ONE, EBallPossession.WE));
		ballGetter = new BallGetterRole(AIConfig.getGeometry().getGoalTheir().getGoalCenter(), EBallContact.DRIBBLE);
		shooter = new ShooterV2Role(false, true);
		addAggressiveRole(ballGetter, aiFrame.worldFrame.ball.getPos());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		ballGetter.setViewPoint(currentFrame.tacticalInfo.getBestDirectShootTarget());
		if ((currentFrame.refereeMsgCached != null))
		{
			// check if FreeKick for us
			if ((((currentFrame.worldFrame.getTeamProps().getTigersAreYellow()) && (currentFrame.getRefereeCmd() == Command.DIRECT_FREE_YELLOW)))
					|| ((!currentFrame.worldFrame.getTeamProps().getTigersAreYellow()) && (currentFrame.getRefereeCmd() == Command.DIRECT_FREE_BLUE)))
			{
				log.trace("Free Kick for us -> setPenaltyAreaAllowed(true)");
				ballGetter.getMoveCon().setPenaltyAreaAllowed(true);
				shooter.getMoveCon().setPenaltyAreaAllowed(true);
			}
		}
		
		switch (state)
		{
			case GET:
				if (ballGetter.isCompleted())
				{
					log.debug("Change roles");
					switchRoles(ballGetter, shooter, currentFrame);
					state = EState.SHOOT;
					setTimeout(5);
				}
				// if (ballGetter.isImpossible())
				// {
				// log.debug("BallGetter is impossible");
				// changeToFailed();
				// }
				break;
			case SHOOT:
				if (shooter.isCompleted())
				{
					changeToFinished();
				}
				break;
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// idle
	}
	
	
	@Override
	protected void timedOut(AIInfoFrame frame)
	{
		log.warn("Failed timeout");
		changeToFailed();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
