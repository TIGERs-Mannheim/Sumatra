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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV3Role;


/**
 * Makes the direct ShotPlay more effective and try to save time during the gameplay.
 * First this play try to get the ball with the ball getter-role, afterwards it will
 * shoot to the best point.
 * 
 * @author jan
 * 
 */
public class DirectShotV3Play extends AOffensivePlay

{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log	= Logger.getLogger(DirectShotV3Play.class.getName());
	
	private final ShooterV3Role	shooter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public DirectShotV3Play(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		addCriterion(new BallPossessionCrit(EBallPossession.NO_ONE, EBallPossession.WE));
		shooter = new ShooterV3Role(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		addAggressiveRole(shooter, aiFrame.worldFrame.ball.getPos());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		shooter.setViewPoint(currentFrame.tacticalInfo.getBestDirectShootTarget());
		// if ((currentFrame.refereeMsgCached != null))
		// {
		// // check if FreeKick for us
		// if ((((currentFrame.worldFrame.getTeamProps().getTigersAreYellow()) && (currentFrame.getRefereeCmd() ==
		// Command.DIRECT_FREE_YELLOW)))
		// || ((!currentFrame.worldFrame.getTeamProps().getTigersAreYellow()) && (currentFrame.getRefereeCmd() ==
		// Command.DIRECT_FREE_BLUE)))
		// {
		// log.trace("Free Kick for us -> setPenaltyAreaAllowed(true)");
		// shooter.getMoveCon().setPenaltyAreaAllowed(true);
		// }
		// }
		
		if (shooter.isCompleted())
		{
			changeToFinished();
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
