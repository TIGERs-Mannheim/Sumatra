/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.boundary.IShapeBoundary;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Setter;

import java.awt.Color;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * The skill moves along the shape boundary by setting the current destination for the bot such that it
 * moves to the desired destination without blocking others companions or cutting the shape.
 */
public class MoveOnShapeBoundarySkill extends AMoveToSkill
{
	@Configurable(comment = "[mm] For destinations further away than this value, intermediate destinations will be generated", defValue = "600.0")
	private static double brakeDistance = 600.0;
	@Configurable(comment = "[mm] If we are closer to this and want to drive around a corner stop ignoring companions ahead", defValue = "180.0")
	private static double atCornerDistance = 180.0;
	@Configurable(comment = "[mm] If companion is closer to the boundary ahead than this stop ignoring him", defValue = "140.0")
	private static double distanceToSegment = 140.0;
	@Configurable(comment = "[mm] If companion is inside shape with this boundary avoid him to allow him to leave the shape", defValue = "-50")
	private static double innerBoundaryToConsider = -50;

	@Setter
	private IShapeBoundary penAreaBoundary;
	@Setter
	private IVector2 destination;
	@Setter
	private Set<BotID> companions = Set.of();


	@Override
	public void setKickParams(final KickParams kickParams)
	{
		// allow setting kick params from outside directly
		super.setKickParams(kickParams);
	}


	@Override
	public void doEntryActions()
	{
		getMoveCon().setBallObstacle(false);
		// bots are obstacle, but the considered bots are set below
		getMoveCon().setOurBotsObstacle(true);
		getMoveCon().setTheirBotsObstacle(true);

		super.doEntryActions();
	}


	@Override
	public void doUpdate()
	{
		getMoveCon().setBallObstacle(false);

		var destinationWithType = findDestination();
		updateDestination(destinationWithType.destination);
		updateTargetAngle(getAngleByOurGoal(destinationWithType.destination));
		updateIgnoredBots(destinationWithType);
		super.doUpdate();
	}


	private double getAngleByOurGoal(final IVector2 targetPos)
	{
		return Vector2.fromPoints(Geometry.getGoalOur().getCenter(), targetPos).getAngle();
	}


	private DestinationWithType findDestination()
	{
		final IVector2 finalDestination = destination;
		getShapes().get(ESkillShapesLayer.MOVE_ON_PENALTY_AREA_SKILL).add(
				new DrawableCircle(destination, 40, Color.GREEN)
		);
		final IVector2 destinationProjection = projectOnBoundary(finalDestination);
		final IVector2 positionProjection = projectOnBoundary(getPos());

		final Optional<IVector2> nextCorner = penAreaBoundary.nextIntermediateCorner(positionProjection,
				destinationProjection);
		double distToNextCorner = nextCorner.map(corner -> corner.distanceTo(positionProjection)).orElse(0.0);
		nextCorner.ifPresent(iVector2 -> getShapes().get(ESkillShapesLayer.MOVE_ON_PENALTY_AREA_SKILL).add(
				new DrawableCircle(iVector2, 50, Color.RED)
		));
		if (nextCorner.isPresent() && distToNextCorner > brakeDistance)
		{
			return new DestinationWithType(nextCorner.get(), nextCorner);
		}

		double distanceOnPenAreaBorder = penAreaBoundary.distanceBetween(destinationProjection, positionProjection);

		if (distanceOnPenAreaBorder < brakeDistance)
		{
			return new DestinationWithType(finalDestination, nextCorner);
		}

		getShapes().get(ESkillShapesLayer.MOVE_ON_PENALTY_AREA_SKILL).add(
				new DrawableCircle(destinationProjection, 50, Color.ORANGE)
		);
		return new DestinationWithType(destinationProjection, nextCorner);
	}


	private void updateIgnoredBots(DestinationWithType destinationWithType)
	{
		var allIgnored = Stream.concat(
				getMoveCon().getIgnoredBots().stream(),
				getIgnoredCompanions(destinationWithType)
						.filter(this::isBotWellOutsideShape)
		).collect(Collectors.toUnmodifiableSet());
		getMoveCon().setIgnoredBots(allIgnored);
	}


	private Stream<BotID> getIgnoredCompanions(DestinationWithType destinationWithType)
	{
		if (!isBotWellOutsideShape(getBotId()))
		{
			return Stream.of();
		}
		if (destinationWithType.nextCorner.isPresent()
				&& getPos().distanceTo(destinationWithType.nextCorner.get()) < atCornerDistance)
		{
			var posProjected = projectOnBoundary(getPos());
			var segmentTillCorner = Lines.segmentFromPoints(posProjected, destinationWithType.nextCorner.get());
			var companionsNotIgnored = companions.stream()
					.map(botID -> getWorldFrame().getBot(botID))
					.filter(bot -> segmentTillCorner.distanceTo(bot.getPos()) < distanceToSegment)
					.toList();

			var shapes = getShapes().get(ESkillShapesLayer.MOVE_ON_PENALTY_AREA_SKILL);
			shapes.add(new DrawableLine(segmentTillCorner, Color.CYAN));
			companionsNotIgnored.stream()
					.map(ITrackedBot::getPos)
					.map(pos -> Circle.createCircle(pos, Geometry.getBotRadius() + 10))
					.map(circle -> new DrawableCircle(circle, Color.CYAN))
					.forEach(shapes::add);
			return companions.stream()
					.filter(botID -> companionsNotIgnored.stream().noneMatch(bot -> bot.getBotId().equals(botID)));
		} else
		{
			return companions.stream();
		}
	}


	private boolean isBotWellOutsideShape(BotID companion)
	{
		return !penAreaBoundary.getShape().withMargin(innerBoundaryToConsider)
				.isPointInShape(getWorldFrame().getBot(companion).getPos());
	}


	private IVector2 projectOnBoundary(IVector2 pos)
	{
		return penAreaBoundary.projectPoint(Geometry.getGoalOur().getCenter(), pos);
	}


	private record DestinationWithType(IVector2 destination, Optional<IVector2> nextCorner)
	{
	}
}
