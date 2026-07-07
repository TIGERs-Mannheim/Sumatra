package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.geometry.Geometry;


public class RamboKeeperSkill extends AMoveToSkill
{
	@Override
	public void doEntryActions()
	{
		setMoveCon(getMoveCon()
				.withPenaltyAreaOurObstacle(false)
				.withFieldBorderObstacle(false)
				.withBotsObstacle(false)
				.withBallObstacle(false));

		super.doEntryActions();
	}


	@Override
	public void doUpdate()
	{
		var ballPosition = getBall().getPos();
		var defPoint = Geometry.getGoalOur().bisection(ballPosition);
		var direction = ballPosition.subtractNew(defPoint);

		updateDestination(ballPosition);
		updateTargetAngle(0.0);
		updateTargetTime(0.0);
		updateUseComeToAStopIfFaster(true);
		setMoveConstraints(getMoveConstraints().withPrimaryDirection(direction)); // first move between goalLine

		super.doUpdate();
	}
}
