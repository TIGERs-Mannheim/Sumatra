/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.CriticalKeeperSkill;


/**
 * Drive direct towards the ball
 */
public class RamboState extends AKeeperState
{
	
	
	private AMoveToSkill posSkill;
	
	
	/**
	 * Create skill
	 *
	 * @param parent the parent Role
	 */
	public RamboState(final KeeperRole parent)
	{
		super(parent, EKeeperState.RAMBO);
		
	}
	
	
	@Override
	public void doEntryActions()
	{
		posSkill = AMoveToSkill.createMoveToSkill();
		posSkill.getMoveCon().getMoveConstraints().setAccMax(CriticalKeeperSkill.getKeeperAcc());
		posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
		posSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
		posSkill.getMoveCon().setBotsObstacle(false);
		posSkill.getMoveCon().setBallObstacle(false);
		posSkill.getMoveCon().setArmChip(false);
		setNewSkill(posSkill);
	}
	
	
	@Override
	public void doUpdate()
	{
		Goal goal = Geometry.getGoalOur();
		IVector2 ballPosition = getWFrame().getBall().getPos();
		final IVector2 directionLeftPostGoal = Vector2.fromPoints(ballPosition, goal.getLeftPost());
		final IVector2 directionRightPostGoal = Vector2.fromPoints(ballPosition, goal.getRightPost());
		final IVector2 direction = directionLeftPostGoal.normalizeNew().addNew(directionRightPostGoal.normalizeNew());

        IVector2 lookAtTarget = ballPosition.addNew(Vector2.fromPoints(getPos(), ballPosition));
		posSkill.getMoveCon().updateDestination(getWFrame().getBall().getPos());
		posSkill.getMoveCon().updateLookAtTarget(lookAtTarget);
		posSkill.getMoveCon().setPrimaryDirection(direction); // first move between goalLine
	}
	
	
}
