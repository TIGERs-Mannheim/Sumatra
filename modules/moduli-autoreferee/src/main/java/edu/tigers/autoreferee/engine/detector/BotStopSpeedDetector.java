/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;
import com.google.common.collect.Sets;

import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotTooFastInStop;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Monitors the maximum allowed bot speed during a game stoppage
 */
public class BotStopSpeedDetector extends AGameEventDetector
{
	private static final Logger log = Logger.getLogger(BotStopSpeedDetector.class);
	
	@Configurable(comment = "[s] Grace period before reporting any events", defValue = "2.0")
	private static double gracePeriod = 2.0;
	@Configurable(comment = "[s] The number of milliseconds that a bot needs violate the stop speed limit to be reported", defValue = "0.3")
	private static double minViolationDuration = 0.3;
	
	
	private final Map<BotID, Long> currentViolators = new HashMap<>();
	private final Set<BotID> lastViolators = new HashSet<>();
	private final Map<ETeamColor, Boolean> infringementRecordedThisStopPhase = new EnumMap<>(ETeamColor.class);
	private long entryTime = 0;
	
	
	public BotStopSpeedDetector()
	{
		super(EGameEventDetectorType.BOT_STOP_SPEED, EGameState.STOP);
	}
	
	
	@Override
	protected void doPrepare()
	{
		entryTime = frame.getTimestamp();
		infringementRecordedThisStopPhase.put(ETeamColor.YELLOW, false);
		infringementRecordedThisStopPhase.put(ETeamColor.BLUE, false);
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		/*
		 * We wait an initial time before reporting any events to give robots time to slow down after the STOP command
		 */
		if ((frame.getTimestamp() - entryTime) / 1e9 < gracePeriod)
		{
			return Optional.empty();
		}
		
		IBotIDMap<ITrackedBot> bots = frame.getWorldFrame().getBots();
		
		long delta = frame.getTimestamp() - frame.getPreviousFrame().getTimestamp();
		Set<BotID> frameViolators = getViolators(bots.values());
		Set<BotID> frameNonViolators = Sets.difference(bots.keySet(), frameViolators);
		updateCurrentViolators(frameViolators, frameNonViolators, delta);
		
		
		/*
		 * Bots are considered to violate the rule if they have been speeding for more than MIN_FRAME_COUNT frames. To
		 * allow a certain cooldown and avoid spamming bots are not reported again until they have not violated the speed
		 * limit for more than MIN_FRAME_COUNT frames.
		 */
		Set<BotID> frameCountViolators = currentViolators.keySet().stream()
				.filter(id -> {
					long value = currentViolators.get(id);
					return (value / 1e9 >= minViolationDuration)
							|| (lastViolators.contains(id) && (value > 0));
				}).collect(Collectors.toSet());
		
		Set<BotID> oldViolators = Sets.difference(lastViolators, frameCountViolators).immutableCopy();
		lastViolators.removeAll(oldViolators);
		
		Optional<BotID> optViolator = Sets.difference(frameCountViolators, lastViolators).stream().findFirst();
		if (optViolator.isPresent())
		{
			BotID violator = optViolator.get();
			ITrackedBot bot = bots.getWithNull(violator);
			if (bot == null)
			{
				log.debug("Bot Stop Speed violator disappeard from the field: " + violator);
				return Optional.empty();
			}
			lastViolators.add(violator);
			
			if (!infringementRecordedThisStopPhase.getOrDefault(violator.getTeamColor(), true))
			{
				infringementRecordedThisStopPhase.put(violator.getTeamColor(), true);
			}
			
			return Optional.of(new BotTooFastInStop(violator, bot.getPos(), bot.getVel().getLength()));
		}
		
		return Optional.empty();
	}
	
	
	private Set<BotID> getViolators(final Collection<ITrackedBot> bots)
	{
		return bots.stream()
				.filter(bot -> bot.getFilteredState().orElse(bot.getBotState()).getVel2().getLength()
						* 1e-3 > RuleConstraints.getStopSpeed())
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
	}
	
	
	private void updateCurrentViolators(final Set<BotID> violators, final Set<BotID> nonViolators, final long timeDelta)
	{
		for (BotID violator : violators)
		{
			long value = 0;
			if (currentViolators.containsKey(violator))
			{
				value = currentViolators.get(violator);
			}
			if (value / 1e9 < minViolationDuration)
			{
				value += timeDelta;
			}
			currentViolators.put(violator, value);
		}
		
		for (BotID nonViolator : nonViolators)
		{
			if (currentViolators.containsKey(nonViolator))
			{
				long value = currentViolators.get(nonViolator);
				value -= timeDelta;
				if (value <= 0)
				{
					currentViolators.remove(nonViolator);
				} else
				{
					currentViolators.put(nonViolator, value);
				}
			}
		}
	}
	
	
	@Override
	public void doReset()
	{
		entryTime = 0;
		lastViolators.clear();
		currentViolators.clear();
	}
}
