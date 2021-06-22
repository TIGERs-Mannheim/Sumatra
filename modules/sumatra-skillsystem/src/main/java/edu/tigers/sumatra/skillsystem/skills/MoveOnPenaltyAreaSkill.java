/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PenAreaBoundary;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Setter;
import org.apache.commons.collections4.SetUtils;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * The skill moves along the penalty area boundary by setting the current destination for the bot such that it
 * moves to the desired destination without hitting others bots or cutting the penalty area.
 */
public class MoveOnPenaltyAreaSkill extends AMoveToSkill
{
	@Configurable(comment = "For destinations further away than this value, intermediate destinations will be generated", defValue = "600.0")
	private static double brakeDistance = 600.0;

	@Setter
	private PenAreaBoundary penAreaBoundary;
	@Setter
	private IVector2 destination;


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

		IVector2 targetPos = findDestination();
		super.updateDestination(targetPos);
		updateTargetAngle(getAngleByOurGoal(targetPos));
		getMoveCon().setIgnoredBots(allBotsBut(SetUtils.union(closeBots(), closeToPenAreaBots())));
		super.doUpdate();
	}


	private double getAngleByOurGoal(final IVector2 targetPos)
	{
		return Vector2.fromPoints(Geometry.getGoalOur().getCenter(), targetPos).getAngle();
	}


	private Set<BotID> allBotsBut(Set<BotID> butThisBots)
	{
		return getWorldFrame().getBots().keySet().stream()
				.filter(id -> !butThisBots.contains(id))
				.collect(Collectors.toSet());
	}


	private Set<BotID> closeBots()
	{
		return getWorldFrame().getBots().values().stream()
				.filter(this::notMe)
				.filter(this::closeBot)
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
	}


	private Set<BotID> closeToPenAreaBots()
	{
		return getWorldFrame().getBots().values().stream()
				.filter(this::notMe)
				.filter(this::closeToPenAreaBot)
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
	}


	private boolean closeBot(final ITrackedBot bot)
	{
		double margin = Geometry.getBotRadius() * 2 + 50;
		return bot.getPos().distanceTo(getPos()) < margin;
	}


	private boolean closeToPenAreaBot(final ITrackedBot bot)
	{
		double distanceToPenArea = penAreaBoundary.projectPoint(bot.getPos()).distanceTo(bot.getPos());
		return distanceToPenArea < Geometry.getBotRadius();
	}


	private boolean notMe(final ITrackedBot bot)
	{
		return bot.getBotId() != getBotId();
	}


	private IVector2 findDestination()
	{
		final IVector2 finalDestination = destination;
		final IVector2 destinationProjection = penAreaBoundary.projectPoint(finalDestination);
		final IVector2 positionProjection = penAreaBoundary.projectPoint(getPos());

		final Optional<IVector2> nextCorner = penAreaBoundary.nextIntermediateCorner(positionProjection,
				destinationProjection);
		double distToNextCorner = nextCorner.map(corner -> corner.distanceTo(positionProjection)).orElse(0.0);
		if (nextCorner.isPresent() && distToNextCorner > brakeDistance)
		{
			return nextCorner.get();
		}

		double distanceOnPenAreaBorder = penAreaBoundary.distanceBetween(destinationProjection, positionProjection);

		if (distanceOnPenAreaBorder < brakeDistance)
		{
			return finalDestination;
		}

		return destinationProjection;
	}
}
