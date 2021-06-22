/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.configurable.Configurable;
import com.google.common.collect.Sets;
import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.DefenderTooCloseToKickPoint;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This rule monitors the bot to ball distance of the defending team during a free kick situation.
 */
public class DefenderToKickPointDistanceDetector extends AGameEventDetector
{
	@Configurable(comment = "Margin [mm] to be added as a tolerance", defValue = "20.0")
	private static double margin = 20;

	private final Map<BotID, Long> lastViolators = new HashMap<>();
	private IVector2 ballPos = null;
	private long tLastViolation;


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
		tLastViolation = 0;
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
		Sets.intersection(curViolators, lastViolators.keySet())
				.forEach(bot -> lastViolators.put(bot, timestamp));

		/*
		 * Remove all old violators which have reached the cool down time
		 */
		lastViolators.entrySet()
				.removeIf(entry -> (timestamp - entry.getValue()) / 1e9 > RuleConstraints.getGracePeriod());

		Set<BotID> newViolators = Sets.difference(curViolators, lastViolators.keySet());
		Optional<BotID> optViolator = newViolators.stream().findFirst();

		var timeSinceLastViolation = (timestamp - tLastViolation) / 1e9;
		if (optViolator.isPresent() && timeSinceLastViolation > RuleConstraints.getGracePeriod())
		{
			BotID violator = optViolator.get();
			lastViolators.put(violator, timestamp);
			tLastViolation = timestamp;

			ITrackedBot bot = frame.getWorldFrame().getBot(violator);
			double distance = ballPos.distanceTo(bot.getPos())
					- RuleConstraints.getStopRadius()
					- Geometry.getBotRadius();

			return Optional.of(new DefenderTooCloseToKickPoint(violator, bot.getPos(), distance));
		}

		return Optional.empty();
	}


	private Set<BotID> getViolators()
	{
		ETeamColor attackingColor = frame.getGameState().getForTeam();

		Map<BotID, ITrackedBot> bots = frame.getWorldFrame().getBots();
		List<ITrackedBot> defendingBots = AutoRefUtil.filterByColor(bots, attackingColor.opposite());

		ICircle outerCircle = Circle.createCircle(
				ballPos,
				RuleConstraints.getStopRadius() + Geometry.getBotRadius() - margin
		);

		return botsInCircle(defendingBots, outerCircle);
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
	}
}
