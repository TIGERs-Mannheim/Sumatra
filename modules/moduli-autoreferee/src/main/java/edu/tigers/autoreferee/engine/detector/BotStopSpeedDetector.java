/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotTooFastInStop;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Monitors the maximum allowed bot speed during a game stoppage
 */
@Log4j2
public class BotStopSpeedDetector extends AGameEventDetector
{
	@Configurable(comment = "[s] Grace period before reporting any events", defValue = "2.0")
	private static double gracePeriod = 2.0;
	@Configurable(comment = "[s] The number of milliseconds that a bot needs violate the stop speed limit to be reported (to compensate known bad vision filter detections)", defValue = "0.3")
	private static double minViolationDuration = 0.3;

	private final Map<BotID, Violator> violatorMap = new HashMap<>();
	/**
	 * Rules state: A violation of this rule is only counted once per robot and stoppage.
	 */
	private final Map<BotID, Boolean> infringementRecordedThisStopPhase = new HashMap<>();
	private long entryTime;
	private long lastGameEventRaised;


	public BotStopSpeedDetector()
	{
		super(EGameEventDetectorType.BOT_STOP_SPEED, EGameState.STOP);
	}


	@Override
	protected void doPrepare()
	{
		lastGameEventRaised = 0;
		entryTime = frame.getTimestamp();
		infringementRecordedThisStopPhase.clear();
	}


	@Override
	public Optional<IGameEvent> doUpdate()
	{
		if ((frame.getTimestamp() - entryTime) / 1e9 < gracePeriod)
		{
			// Rules state: There is a grace period of 2 seconds for the robots to slow down.
			return Optional.empty();
		}

		Set<BotID> frameViolators = getViolators();
		// remove non-violators
		violatorMap.keySet().removeIf(botId -> !frameViolators.contains(botId));
		// add new violators
		frameViolators.forEach(botId -> violatorMap.computeIfAbsent(botId,
				id -> new Violator(
						id,
						frame.getTimestamp(),
						frame.getWorldFrame().getBot(id).getPos()
				)
		));
		// update max velocity
		violatorMap.forEach((id, v) -> v.vMax = Math.max(v.vMax, getBotVel(frame.getWorldFrame().getBot(id))));

		if ((frame.getTimestamp() - lastGameEventRaised) < gracePeriod)
		{
			return Optional.empty();
		}

		var violator = violatorMap.values().stream()
				.filter(v -> (frame.getTimestamp() - v.tStart) / 1e9 > minViolationDuration)
				.findFirst();

		violator.ifPresent(v -> lastGameEventRaised = frame.getTimestamp());
		violator.ifPresent(v -> infringementRecordedThisStopPhase.put(v.botID, true));

		return violator.map(v -> new BotTooFastInStop(v.botID, v.location, v.vMax));
	}


	private Set<BotID> getViolators()
	{
		return frame.getWorldFrame().getBots().values().stream()
				.filter(bot -> !infringementRecordedThisStopPhase.containsKey(bot.getBotId()))
				.filter(bot -> getBotVel(bot) > RuleConstraints.getStopSpeed())
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
	}


	private double getBotVel(ITrackedBot bot)
	{
		return bot.getFilteredState().orElse(bot.getBotState()).getVel2().getLength();
	}


	@RequiredArgsConstructor
	private static class Violator
	{
		final BotID botID;
		final long tStart;
		final IVector2 location;
		double vMax;
	}
}
