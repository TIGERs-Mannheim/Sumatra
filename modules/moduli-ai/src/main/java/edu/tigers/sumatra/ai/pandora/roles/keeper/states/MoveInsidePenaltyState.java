/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;


/**
 * Move to the PenaltyArea
 *
 * @author PhilippP, ChrisC
 */
public class MoveInsidePenaltyState extends AKeeperState
{
	
	/**
	 * @param parent : the parent keeper role
	 */
	public MoveInsidePenaltyState(KeeperRole parent)
	{
		super(parent);
	}
	
	
	@Override
	public void doEntryActions()
	{
		AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
		skill.getMoveCon().setDestinationOutsideFieldAllowed(true);
		skill.getMoveCon().setPenaltyAreaAllowedOur(true);
		skill.getMoveCon().setGoalPostObstacle(true);
		skill.getMoveCon()
				.updateDestination(Geometry.getGoalOur().getCenter().addNew(Vector2.fromX(Geometry.getBotRadius())));
		setNewSkill(skill);
	}
	
	
	@Override
	public void doExitActions()
	{
		// Nothing to do here
	}
	
	
	@Override
	public void doUpdate()
	{
		// Nothing to do here, just moving to PE
	}
}