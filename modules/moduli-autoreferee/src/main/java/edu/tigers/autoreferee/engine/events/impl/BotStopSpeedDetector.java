/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 13, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.Collection;
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
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.SpeedViolation;
import edu.tigers.sumatra.ids.BotID;
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
	private static final int		PRIORITY					= 1;
	private static final Logger	log						= Logger.getLogger(BotStopSpeedDetector.class);
	
	@Configurable(comment = "[ms] Wait time before reporting any events")
	private static long				initialWaitTimeMs		= 2_500;
	@Configurable(comment = "[ms] The number of milliseconds that a bot needs violate the stop speed limit to be reported")
	private static long				violationThresholdMs	= 300;
	
	static
	{
		AGameEventDetector.registerClass(BotStopSpeedDetector.class);
	}
	
	private long					entryTime			= 0;
	private Map<BotID, Long>	currentViolators	= new HashMap<>();
	private Set<BotID>			lastViolators		= new HashSet<>();
	
	
	/**
	 * Create new instance
	 */
	public BotStopSpeedDetector()
	{
		super(EGameState.STOP);
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
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate(final IAutoRefFrame frame, final List<IGameEvent> violations)
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
			
			SpeedViolation violation = new SpeedViolation(EGameEvent.BOT_STOP_SPEED, frame.getTimestamp(), violator,
					null, bot.getVel().getLength());
			return Optional.of(violation);
		}
		
		return Optional.empty();
	}
	
	
	private Set<BotID> getViolators(final Collection<ITrackedBot> bots)
	{
		return bots.stream()
				.filter(bot -> bot.getVel().getLength() > AutoRefConfig.getMaxBotStopSpeed())
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
