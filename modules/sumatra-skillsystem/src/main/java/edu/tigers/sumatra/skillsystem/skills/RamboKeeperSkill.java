/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;


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
		var ballPosition = getBall().getPos();
		var defPoint = Geometry.getGoalOur().bisection(ballPosition);
		var direction = ballPosition.subtractNew(defPoint);

		var destination = TrajectoryGenerator.generateVirtualPositionToReachPointInTime(
				getTBot(),
				ballPosition,
				0.0
		);

		updateDestination(destination);
		updateLookAtTarget(getBall());
		getMoveConstraints().setPrimaryDirection(direction); // first move between goalLine

		super.doUpdate();
	}
}
