/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * This play moves all bots to the maintenance position.
 */
public abstract class AMaintenancePlay extends APlay
{
	protected final PointChecker pointChecker = new PointChecker()
			.checkBallDistances()
			.checkCustom(this::freeOfOtherBots);
	protected final TimestampTimer completionTimer = new TimestampTimer(0.5);


	protected AMaintenancePlay(EPlay play)
	{
		super(play);
	}


	private boolean freeOfOtherBots(IVector2 point)
	{
		double distance = Geometry.getBotRadius() * 2 + 10;
		var ownBots = getRoles().stream().map(ARole::getBotID).toList();
		return getWorldFrame().getBots().values().stream()
				.filter(b -> !ownBots.contains(b.getBotId()))
				.noneMatch(bot -> bot.getPosByTime(1).distanceTo(point) < distance);
	}


	@Override
	protected ARole onAddRole()
	{
		MoveRole role = new MoveRole();
		role.getMoveCon().physicalObstaclesOnly();
		return role;
	}


	protected void drawDestinations(List<IVector2> destinations, double orientation)
	{
		destinations.forEach(pos -> getShapes(EAiShapesLayer.AI_MAINTENANCE).add(
				new DrawableBotShape(pos, AngleMath.deg2rad(orientation), Geometry.getBotRadius(),
						Geometry.getOpponentCenter2DribblerDist())
		));
	}


	private PositionData adjustPositionsToFieldSize(int nBots, double x, IVector2 direction)
	{
		if (Geometry.getFieldWidth() - Geometry.getBotRadius() < Math.abs((nBots - 1) * direction.y()))
		{
			double xDistance = 0.0;
			double yDistance = -Geometry.getFieldWidth() / nBots;
			if (Math.abs(yDistance) <= 2.5 * Geometry.getBotRadius())
			{
				xDistance = Geometry.getBotRadius() * 2.5;
			}
			IVector2 startPos = Vector2.fromXY(x, Math.abs(yDistance) * (nBots - 1) / 2.0);
			Vector2 adjustedDirection = Vector2.fromXY(xDistance, yDistance);
			return new PositionData(
					adjustedDirection,
					startPos.subtractNew(adjustedDirection),
					-1
			);
		}
		return new PositionData(direction);
	}


	/**
	 * Compute bot actions based on a line defined by start pos and direction vector
	 *
	 * @param startPos
	 * @param direction
	 * @param orientation
	 */
	protected void calculateBotActions(IVector2 startPos, IVector2 direction, double orientation)
	{
		List<MoveRole> roles = new ArrayList<>(findRoles(MoveRole.class));
		List<IVector2> drawingDestinations = new ArrayList<>();
		PositionData data = adjustPositionsToFieldSize(roles.size(), startPos.x(), direction);
		IVector2 dest = data.getStartPosition().orElse(startPos.subtractNew(direction));
		roles.sort(Comparator.comparing(ARole::getBotID));

		int count = 0;
		for (MoveRole role : roles)
		{
			do
			{
				dest = dest.addNew(
						data.getDirection().multiplyNew(Vector2.fromXY(Math.pow(data.getSpacialOffsetFactor(), count), 1)));
				count++;
				drawingDestinations.add(dest);
			} while (!pointChecker.allMatch(getAiFrame().getBaseAiFrame(), dest, role.getBotID()));
			role.updateDestination(dest);
			role.updateTargetAngle(AngleMath.deg2rad(orientation));
		}
		drawDestinations(drawingDestinations, orientation);
		updateTimer(roles);
	}


	protected void updateTimer(List<MoveRole> roles)
	{
		if (roles.isEmpty() || roles.stream().allMatch(MoveRole::isSkillStateSuccess))
		{
			completionTimer.update(getWorldFrame().getTimestamp());
		} else
		{
			completionTimer.reset();
		}
	}


	@Value
	@AllArgsConstructor
	private static class PositionData
	{
		IVector2 startPosition;
		IVector2 direction;
		int spacialOffsetFactor;


		public PositionData(IVector2 direction)
		{
			this.direction = direction;
			startPosition = null;
			spacialOffsetFactor = 1;
		}


		public Optional<IVector2> getStartPosition()
		{
			return Optional.ofNullable(startPosition);
		}
	}
}
