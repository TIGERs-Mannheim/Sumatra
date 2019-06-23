/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;


/**
 * Move to the PenaltyArea
 *
 * @author PhilippP, ChrisC
 */
public class MoveInsidePenaltyState extends AKeeperState
{
	private AMoveToSkill skill;
	
	
	/**
	 * @param parent : the parent keeper role
	 */
	public MoveInsidePenaltyState(KeeperRole parent)
	{
		super(parent, EKeeperState.MOVE_TO_PENALTY_AREA);
	}
	
	
	@Override
	public void doEntryActions()
	{
		skill = AMoveToSkill.createMoveToSkill();
		skill.getMoveCon().setPenaltyAreaAllowedOur(true);
		skill.getMoveCon().setGoalPostObstacle(true);
		// if game is running, get into penArea as fast as possible (in STOP, we are limited to stop speed anyway)
		skill.getMoveCon().setFastPosMode(true);
		setNewSkill(skill);
	}
	
	
	@Override
	public void doUpdate()
	{
		final Vector2 destination = Geometry.getGoalOur().getCenter()
				.addNew(Vector2.fromX(Geometry.getPenaltyAreaDepth() / 2));
		skill.getMoveCon().updateDestination(destination);
		
		ILine lineToGoal = Line.fromPoints(Geometry.getGoalOur().getCenter(), getPos());
		double targetAngle = lineToGoal.getAngle().orElse(0.0);
		skill.getMoveCon().updateTargetAngle(targetAngle);
	}
}