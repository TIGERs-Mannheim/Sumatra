/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


public class RamboKeeperSkill extends AMoveToSkill
{
	@Override
	public void doEntryActions()
	{
		getMoveCon().setPenaltyAreaOurObstacle(false);
		getMoveCon().setFieldBorderObstacle(false);
		getMoveCon().setBotsObstacle(false);
		getMoveCon().setBallObstacle(false);

		super.doEntryActions();
	}


	@Override
	public void doUpdate()
	{
		Goal goal = Geometry.getGoalOur();
		IVector2 ballPosition = getBall().getPos();
		final IVector2 directionLeftPostGoal = Vector2.fromPoints(ballPosition, goal.getLeftPost());
		final IVector2 directionRightPostGoal = Vector2.fromPoints(ballPosition, goal.getRightPost());
		final IVector2 direction = directionLeftPostGoal.normalizeNew().addNew(directionRightPostGoal.normalizeNew());

		updateDestination(getBall().getPos());
		updateLookAtTarget(getBall());
		getMoveConstraints().setPrimaryDirection(direction); // first move between goalLine

		super.doUpdate();
	}
}
