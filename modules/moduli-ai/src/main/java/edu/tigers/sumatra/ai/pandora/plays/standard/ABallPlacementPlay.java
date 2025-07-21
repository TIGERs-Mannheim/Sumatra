/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.common.KeepDistanceToBall;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.ballplacement.BallPlacementBotChooser;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.placement.BallPlacementRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Setter;

import java.util.Optional;


/**
 * Base play for placing the ball.
 */
public abstract class ABallPlacementPlay extends APlay
{
	private final KeepDistanceToBall keepDistanceToBall = new KeepDistanceToBall(new PointChecker()
			.checkBallDistanceStatic()
			.checkInsideField()
			.checkPointFreeOfBots());

	@Setter
	private BotID forcedSoloPlacementBot;

	@Setter
	private Double placementTolerance;

	private BotID lastPrimaryBot;
	private BotID lastAssistantBot;


	protected ABallPlacementPlay(final EPlay type)
	{
		super(type);
	}


	protected final void assignBallPlacementRoles()
	{
		IVector2 placementPos = getBallTargetPos();

		if (getRoles().isEmpty() || placementPos == null)
		{
			return;
		}

		var ballPlacementBotChooser = new BallPlacementBotChooser(
				getBall(),
				getBallTargetPos(),
				getRoles().stream().map(ARole::getBot).toList(),
				lastPrimaryBot,
				lastAssistantBot
		);

		Optional<BotID> primaryBot = Optional.ofNullable(forcedSoloPlacementBot)
				.or(ballPlacementBotChooser::choosePrimary);
		if (primaryBot.isEmpty())
		{
			lastPrimaryBot = null;
			lastAssistantBot = null;
			return;
		}
		lastPrimaryBot = primaryBot.get();
		var primaryRole = reassignRole(getRole(lastPrimaryBot), BallPlacementRole.class, BallPlacementRole::new);
		var useAssistant = forcedSoloPlacementBot == null;

		ballPlacementBotChooser.getOrderedAssistants(lastPrimaryBot).stream()
				.filter(id -> useAssistant)
				.findFirst()
				.ifPresentOrElse(
						assistant -> {
							lastAssistantBot = assistant;
							ARole assistantRole = assignAssistant(getRole(assistant), placementPos);
							allRolesExcept(assistantRole, primaryRole).forEach(this::handleNonPlacingRole);
						},
						() -> {
							lastAssistantBot = null;
							primaryRole.setPassMode(BallPlacementRole.EPassMode.NONE);
							allRolesExcept(primaryRole).forEach(this::handleNonPlacingRole);
						}
				);

		findRoles(BallPlacementRole.class).forEach(r -> {
			if (placementTolerance != null)
			{
				r.setPlacementTolerance(placementTolerance);
			}
			r.setBallTargetPos(placementPos);
		});
	}


	private MoveRole assignAssistant(ARole receivingRole, IVector2 placementPos)
	{
		MoveRole receivingMoveRole = reassignRole(receivingRole, MoveRole.class, MoveRole::new);

		double dist2Ball = receivingMoveRole.getBot().getCenter2DribblerDist() + Geometry.getBallRadius();
		receivingMoveRole.updateDestination(LineMath.stepAlongLine(placementPos, getBall().getPos(), -dist2Ball));
		receivingMoveRole.updateLookAtTarget(getBall());
		boolean isReadyForPass = receivingMoveRole.isDestinationReached();

		receivingMoveRole.getMoveCon().physicalObstaclesOnly();
		receivingMoveRole.getMoveCon().setBallObstacle(!isReadyForPass);
		if (isReadyForPass)
		{
			receivingMoveRole.setDestinationAdjuster((aiFrame, destination, botID) -> destination);
		} else
		{
			receivingMoveRole.setDestinationAdjuster(keepDistanceToBall::findNextFreeDest);
		}

		findRoles(BallPlacementRole.class).forEach(r -> r.setPassMode(
				isReadyForPass
						? BallPlacementRole.EPassMode.READY
						: BallPlacementRole.EPassMode.WAIT));
		return receivingMoveRole;
	}


	protected final boolean ballPlacementDone()
	{
		var placementRoles = findRoles(BallPlacementRole.class);
		return !placementRoles.isEmpty() && placementRoles.stream().allMatch(BallPlacementRole::isBallPlacedAndCleared);
	}


	protected void handleNonPlacingRole(ARole role)
	{
		reassignRole(role, MoveRole.class, MoveRole::new);
	}


	protected abstract IVector2 getBallTargetPos();
}
