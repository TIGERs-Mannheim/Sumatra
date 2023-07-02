/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.test.move;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * All available Robots shall move on a circle around the ball-position.
 */
public class AroundTheBallPlay extends APlay
{
	private final Goal goal = Geometry.getGoalOur();
	private final double radius;


	public AroundTheBallPlay(double radius)
	{
		super(EPlay.AROUND_THE_BALL);
		this.radius = radius;
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		IVector2 ballPos = Vector2.copy(getBall().getPos());

		// direction: vector from ball to the middle of the goal!
		Vector2 direction = goal.getCenter().subtractNew(ballPos);
		// sets the length of the vector to 'radius'
		direction.scaleTo(radius);

		double turn = AngleMath.PI / 2.0;

		for (final ARole role : getRoles())
		{
			final MoveRole moveRole = (MoveRole) role;

			final IVector2 destination = ballPos.addNew(direction.turnNew(turn));

			moveRole.getMoveCon().physicalObstaclesOnly();
			moveRole.updateDestination(destination);
			moveRole.updateLookAtTarget(getBall());
			turn -= AngleMath.PI / 5.0;
		}
	}
}
