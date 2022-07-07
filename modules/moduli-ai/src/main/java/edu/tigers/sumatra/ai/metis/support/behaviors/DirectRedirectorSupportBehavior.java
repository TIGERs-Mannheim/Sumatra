/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static edu.tigers.sumatra.ai.metis.offense.OffensiveConstants.getMaximumReasonableRedirectAngle;


/**
 * Go through all supportive goal positions and calculate the redirect angle from the attacker to that pos to the goal.
 * Take the pos with the smallest redirect angle, filter out too large distances, and take this as the position.
 * The viability is a constant 1.
 */
@RequiredArgsConstructor
public class DirectRedirectorSupportBehavior extends ASupportBehavior
{
	@Configurable(comment = "Enable this behavior?", defValue = "true")
	private static boolean enabled = true;

	@Configurable(comment = "[mm]", defValue = "8000.")
	private static double maxDistance = 8000;

	@Configurable(comment = "[mm]", defValue = "12000.")
	private static double maxBallTravelDist = 12000;

	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;
	private final Supplier<List<IVector2>> supportiveGoalPositions;


	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		return kickOrigins.get().values().stream()
				.min(Comparator.comparingDouble(e -> e.getPos().distanceTo(getWFrame().getBall().getPos())))
				.map(kickOrigin -> calcPositionForOffensivePos(getWFrame().getBot(botID), kickOrigin.getPos()))
				.orElseGet(SupportBehaviorPosition::notAvailable);
	}


	private SupportBehaviorPosition calcPositionForOffensivePos(ITrackedBot bot, IVector2 source)
	{
		IVector2 target = Geometry.getGoalTheir().getCenter();
		return supportiveGoalPositions.get().stream()
				.filter(p -> belowMaxBallTravelDistance(source, target, p))
				.filter(p -> p.distanceTo(bot.getPos()) < maxDistance)
				.filter(p -> redirectAngle(source, target, p) < getMaximumReasonableRedirectAngle())
				.min(Comparator.comparingDouble(p -> redirectAngle(source, target, p)))
				.map(pos -> SupportBehaviorPosition.fromDestinationAndRotationTarget(pos, target, 1))
				.orElse(null);
	}


	private boolean belowMaxBallTravelDistance(IVector2 offensivePos, IVector2 goalTheir, IVector2 p)
	{
		return p.distanceTo(goalTheir) + p.distanceTo(offensivePos) < maxBallTravelDist;
	}


	private double redirectAngle(IVector2 source, IVector2 target, IVector2 point)
	{
		return target.subtractNew(point).angleToAbs(source.subtractNew(point)).orElse(Math.PI);
	}


	@Override
	public boolean isEnabled()
	{
		return enabled;
	}
}
