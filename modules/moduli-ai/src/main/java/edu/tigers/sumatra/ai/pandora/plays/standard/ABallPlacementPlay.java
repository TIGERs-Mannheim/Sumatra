/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.placement.BallPlacementRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Comparator;


/**
 * Base play for placing the ball.
 */
public abstract class ABallPlacementPlay extends APlay
{
	protected ABallPlacementPlay(final EPlay type)
	{
		super(type);
	}


	protected final void assignBallPlacementRoles()
	{
		IVector2 placementPos = getBallTargetPos();

		if (getRoles().isEmpty())
		{
			return;
		}

		var currentBallPlacementBot = findRoles(BallPlacementRole.class).stream()
				.map(BallPlacementRole::getBotID)
				.findFirst().orElse(BotID.noBot());
		if (getRoles().size() == 1)
		{
			reassignRole(getRoles().get(0), BallPlacementRole.class, BallPlacementRole::new);
		} else if (useAssistant())
		{
			ARole ballPlacementRole = getRoles()
					.stream()
					.min(Comparator.comparing(r -> getBall().getTrajectory().getTravelLineSegment().distanceTo(r.getPos())
							- ((r.getBotID() == currentBallPlacementBot) ? 500 : 0)))
					.map(r -> reassignRole(r, BallPlacementRole.class, BallPlacementRole::new))
					.orElseThrow();
			ARole receivingRole = allRolesExcept(ballPlacementRole)
					.stream()
					.min(Comparator.comparing(r -> r.getPos().distanceToSqr(getBallTargetPos())))
					.map(r -> reassignRole(r, MoveRole.class, MoveRole::new))
					.orElseThrow();

			MoveRole moveRole = findRoles(MoveRole.class).get(0);
			double dist2Ball = moveRole.getBot().getCenter2DribblerDist() + Geometry.getBallRadius();
			moveRole.updateDestination(LineMath.stepAlongLine(placementPos, getBall().getPos(), -dist2Ball));
			moveRole.updateLookAtTarget(getBall());
			moveRole.getMoveCon().physicalObstaclesOnly();
			moveRole.getMoveCon().setBallObstacle(false);

			findRoles(BallPlacementRole.class).forEach(r -> r.setPassMode(
					moveRole.isDestinationReached()
							? BallPlacementRole.EPassMode.READY
							: BallPlacementRole.EPassMode.WAIT));

			allRolesExcept(receivingRole, ballPlacementRole)
					.forEach(this::handleNonPlacingRole);
		} else
		{
			ARole ballPlacementRole = getRoles()
					.stream()
					.min(Comparator.comparing(r -> getBall().getTrajectory().getTravelLineSegment().distanceTo(r.getPos())
							- ((r.getBotID() == currentBallPlacementBot) ? 500 : 0)))
					.map(r -> reassignRole(r, BallPlacementRole.class, BallPlacementRole::new))
					.orElseThrow();

			allRolesExcept(ballPlacementRole)
					.forEach(this::handleNonPlacingRole);
		}

		findRoles(BallPlacementRole.class).forEach(r -> r.setBallTargetPos(placementPos));
	}


	protected final boolean ballPlacementDone()
	{
		return findRoles(BallPlacementRole.class).stream().allMatch(BallPlacementRole::isBallPlacedAndCleared);
	}


	protected boolean useAssistant()
	{
		return getRoles().size() > 1;
	}


	protected void handleNonPlacingRole(ARole role)
	{
		reassignRole(role, MoveRole.class, MoveRole::new);
	}


	protected abstract IVector2 getBallTargetPos();
}
