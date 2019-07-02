/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;
import com.google.common.collect.Sets;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.DefenderTooCloseToKickPoint;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule monitors the bot to ball distance of the defending team during a free kick situation.
 */
public class DefenderToKickPointDistanceDetector extends AGameEventDetector
{
	@Configurable(comment = "If disabled only bots that are on a collision course with the ball will be considered violators", defValue = "true")
	private static boolean strictMode = true;

	@Configurable(comment = "[s] The amount of time a bot can be located inside the outer circle (500mm>x>250mm from the kick pos) without logging a violation", defValue = "3.0")
	private static double maxOuterCircleLingerTime = 3.0;

	@Configurable(comment = "[s] The amount of time before a violation is reported again for the same bot", defValue = "1.5")
	private static double violatorCoolDownTime = 1.5;

	private final Map<BotID, Long> lastViolators = new HashMap<>();
	private final Map<BotID, Long> outerCircleBots = new HashMap<>();
	private IVector2 ballPos = null;


	public DefenderToKickPointDistanceDetector()
	{
		super(EGameEventDetectorType.DEFENDER_TO_KICK_POINT_DISTANCE, EnumSet.of(
				EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE, EGameState.KICKOFF));
	}


	@Override
	protected void doPrepare()
	{
		super.doPrepare();
		ballPos = frame.getWorldFrame().getBall().getPos();
	}


	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		long timestamp = frame.getTimestamp();
		Set<BotID> curViolators = getViolators();

		/*
		 * Update the timestamp of all violators for which a violation has already been generated but which are still
		 * violating the rule
		 */
		Sets.intersection(curViolators, lastViolators.keySet()).forEach(bot -> lastViolators.put(bot, timestamp));

		/*
		 * Remove all old violators which have reached the cool down time
		 */
		lastViolators.entrySet().removeIf(entry -> (timestamp - entry.getValue()) / 1e9 > violatorCoolDownTime);

		Set<BotID> newViolators = Sets.difference(curViolators, lastViolators.keySet()).immutableCopy();
		Optional<BotID> optViolator = newViolators.stream().findFirst();

		if (optViolator.isPresent())
		{
			BotID violator = optViolator.get();
			lastViolators.put(violator, timestamp);

			ITrackedBot bot = frame.getWorldFrame().getBot(violator);
			double distance = ballPos.distanceTo(bot.getPos()) - RuleConstraints.getStopRadius()
					- Geometry.getBotRadius();

			return Optional.of(new DefenderTooCloseToKickPoint(violator, bot.getPos(), distance));
		}

		return Optional.empty();
	}


	private Set<BotID> getViolators()
	{
		ETeamColor attackingColor = frame.getGameState().getForTeam();

		IBotIDMap<ITrackedBot> bots = frame.getWorldFrame().getBots();
		List<ITrackedBot> defendingBots = AutoRefUtil.filterByColor(bots, attackingColor.opposite());


		Set<BotID> violators = new HashSet<>();
		long curTimestamp = frame.getTimestamp();
		/*
		 * Only consider bots which have fully entered the circle
		 */
		ICircle outerCircle = Circle.createCircle(ballPos,
				RuleConstraints.getStopRadius() - Geometry.getBotRadius());

		if (strictMode)
		{
			violators.addAll(botsInCircle(defendingBots, outerCircle));
		} else
		{
			ICircle innerCircle = Circle.createCircle(ballPos, RuleConstraints.getStopRadius() / 2);
			Set<BotID> innerCircleViolators = botsInCircle(defendingBots, innerCircle);
			violators.addAll(innerCircleViolators);

			Set<BotID> outerCircleViolators = Sets.difference(botsInCircle(defendingBots, outerCircle),
					innerCircleViolators);
			Set<BotID> newViolators = Sets.difference(outerCircleViolators, outerCircleBots.keySet()).immutableCopy();
			Set<BotID> oldViolators = Sets.difference(outerCircleBots.keySet(), outerCircleViolators).immutableCopy();

			newViolators.forEach(id -> outerCircleBots.put(id, curTimestamp));
			oldViolators.forEach(outerCircleBots::remove);

			outerCircleBots.forEach((id, entryTimestamp) -> {
				if ((curTimestamp - entryTimestamp) / 1e9 > maxOuterCircleLingerTime)
				{
					violators.add(id);
				}
			});
		}

		return violators;
	}


	private Set<BotID> botsInCircle(final List<ITrackedBot> bots, final ICircle circle)
	{
		return bots.stream()
				.filter(bot -> circle.isPointInShape(bot.getPos()))
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
	}


	@Override
	protected void doReset()
	{
		lastViolators.clear();
		outerCircleBots.clear();
	}
}
