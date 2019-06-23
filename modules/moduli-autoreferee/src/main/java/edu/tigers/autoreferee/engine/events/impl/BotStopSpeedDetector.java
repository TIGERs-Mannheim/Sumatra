/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import static edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardType.CARD_YELLOW;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;
import com.google.common.collect.Sets;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.AutoRefUtil.ToBotIDMapper;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.CardPenalty;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.SpeedViolation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Monitors the maximum allowed bot speed during a game stoppage
 * 
 * @author "Lukas Magel"
 */
public class BotStopSpeedDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	private static final Logger log = Logger.getLogger(BotStopSpeedDetector.class);
	
	@Configurable(comment = "[ms] Wait time before reporting any events", defValue = "2500")
	private static long initialWaitTimeMs = 2_500;
	@Configurable(comment = "[ms] The number of milliseconds that a bot needs violate the stop speed limit to be reported", defValue = "300")
	private static long violationThresholdMs = 300;
	
	@Configurable(comment = "Number of infringements allowed, until a yellow card is issued", defValue = "3")
	private static int numInfringementsPerYellowCard = 3;

	static
	{
		AGameEventDetector.registerClass(BotStopSpeedDetector.class);
	}
	
	private long entryTime = 0;
	private Map<BotID, Long> currentViolators = new HashMap<>();
	private Set<BotID> lastViolators = new HashSet<>();
	private Map<ETeamColor, Integer> counter = new EnumMap<>(ETeamColor.class);
	private Map<ETeamColor, Boolean> infringementRecordedThisStopPhase = new EnumMap<>(ETeamColor.class);
	
	
	/**
	 * Create new instance
	 */
	public BotStopSpeedDetector()
	{
		super(EGameEventDetectorType.BOT_STOP_SPEED, EGameState.STOP);
		counter.put(ETeamColor.YELLOW, 0);
		counter.put(ETeamColor.BLUE, 0);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		entryTime = frame.getTimestamp();
		infringementRecordedThisStopPhase.put(ETeamColor.YELLOW, false);
		infringementRecordedThisStopPhase.put(ETeamColor.BLUE, false);
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate(final IAutoRefFrame frame)
	{
		/*
		 * We wait an initial time before reporting any events to give robots time to slow down after the STOP command
		 */
		if ((frame.getTimestamp() - entryTime) < TimeUnit.MILLISECONDS.toNanos(initialWaitTimeMs))
		{
			return Optional.empty();
		}
		
		IBotIDMap<ITrackedBot> bots = frame.getWorldFrame().getBots();
		
		Set<BotID> botIDs = AutoRefUtil.mapToID(bots);
		
		long delta = frame.getTimestamp() - frame.getPreviousFrame().getTimestamp();
		Set<BotID> frameViolators = getViolators(bots.values());
		Set<BotID> frameNonViolators = Sets.difference(botIDs, frameViolators);
		updateCurrentViolators(frameViolators, frameNonViolators, delta);
		
		
		/*
		 * Bots are considered to violate the rule if they have been speeding for more than MIN_FRAME_COUNT frames. To
		 * allow a certain cooldown and avoid spamming bots are not reported again until they have not violated the speed
		 * limit for more than MIN_FRAME_COUNT frames.
		 */
		Set<BotID> frameCountViolators = currentViolators.keySet().stream()
				.filter(id -> {
					long value = currentViolators.get(id);
					return (value >= TimeUnit.MILLISECONDS.toNanos(violationThresholdMs))
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
				counter.computeIfPresent(violator.getTeamColor(), (k, v) -> v + 1);
				infringementRecordedThisStopPhase.put(violator.getTeamColor(), true);
				log.info("New bot stop speed infringement counter: " + counter);
			}

			final List<CardPenalty> cardPenalties = counter.entrySet().stream()
					.filter(e -> e.getValue() >= numInfringementsPerYellowCard)
					.map(e -> new CardPenalty(CARD_YELLOW, e.getKey()))
					.collect(Collectors.toList());
			counter.entrySet().forEach(e -> e.setValue(e.getValue() % numInfringementsPerYellowCard));

			SpeedViolation violation = new SpeedViolation(EGameEvent.ROBOT_STOP_SPEED, frame.getTimestamp(), violator,
					null, bot.getVel().getLength(), cardPenalties);
			return Optional.of(violation);
		}
		
		return Optional.empty();
	}
	
	
	private Set<BotID> getViolators(final Collection<ITrackedBot> bots)
	{
		return bots.stream()
				.filter(bot -> bot.getFilteredState().orElse(bot.getBotState()).getVel2().getLength() > AutoRefConfig
						.getMaxBotStopSpeed())
				.map(ToBotIDMapper.get())
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
			if (value < TimeUnit.MILLISECONDS.toNanos(violationThresholdMs))
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
