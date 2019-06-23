/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.skillsystem.skills.CatchSkill;


/**
 * Block intersection point BallVel -> Goal
 *
 * @author ChrisC
 */
public class BallGoalInterceptState extends AKeeperState
{
	/**
	 * Creates a new BallGoalInterceptState
	 * 
	 * @param parent : the parent role
	 */
	public BallGoalInterceptState(KeeperRole parent)
	{
		super(parent);
	}
	
	
	@Override
	public void doEntryActions()
	{
		CatchSkill catchSkill = new CatchSkill();
		catchSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
		catchSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
		catchSkill.getMoveCon().setBotsObstacle(false);
        catchSkill.getMoveCon().setGoalPostObstacle(false);
        setNewSkill(catchSkill);
	}
	
	
	@Override
	public void doExitActions()
	{
		// nothing to do here
	}
	
	
	@Override
	public void doUpdate()
	{
		// Logic is in the CatchSkill cause of performance
	}
	
	
}