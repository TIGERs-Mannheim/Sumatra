/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 17, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;
import com.google.common.collect.Sets;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.AutoRefUtil.ToBotIDMapper;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.DistanceViolation;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule monitors the bot to ball distance of the defending team during a freekick situation and restarts the play
 * if necessary.
 * 
 * @author Lukas Magel
 */
public class DefenderToKickPointDistanceDetector extends APreparingGameEventDetector
{
	private static final int	priority								= 1;
	
	@Configurable(comment = "If disabled only bots that are on a collision course with the ball will be considered violators")
	private static boolean		STRICT_MODE							= true;
	
	@Configurable(comment = "[ms] The amount of time a bot can be located inside the outer circle (500mm>x>250mm from the kick pos) without logging a violation")
	private static long			MAX_OUTER_CIRCLE_LINGER_TIME	= 3_000;
	
	@Configurable(comment = "[ms] The amount of time before a violation is reported again for the same bot")
	private static long			VIOLATOR_COOLDOWN_TIME			= 1_500;
	
	private IVector2				ballPos								= null;
	private Map<BotID, Long>	lastViolators						= new HashMap<>();
	private Map<BotID, Long>	outerCircleBots					= new HashMap<>();
	
	static
	{
		AGameEventDetector.registerClass(DefenderToKickPointDistanceDetector.class);
	}
	
	
	/**
	 * 
	 */
	public DefenderToKickPointDistanceDetector()
	{
		super(EnumSet.of(
				EGameStateNeutral.DIRECT_KICK_BLUE, EGameStateNeutral.DIRECT_KICK_YELLOW,
				EGameStateNeutral.INDIRECT_KICK_BLUE, EGameStateNeutral.INDIRECT_KICK_YELLOW,
				EGameStateNeutral.KICKOFF_BLUE, EGameStateNeutral.KICKOFF_YELLOW));
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		ballPos = frame.getWorldFrame().getBall().getPos();
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		long timestamp = frame.getTimestamp();
		Set<BotID> curViolators = getViolators(frame);
		
		/*
		 * Update the timestamp of all violators for which a violation has already been generated but which are still
		 * violating the rule
		 */
		Sets.intersection(curViolators, lastViolators.keySet()).forEach(bot -> lastViolators.put(bot, timestamp));
		
		/*
		 * Remove all old violators which have reached the cooldown time
		 */
		Iterator<Map.Entry<BotID, Long>> iter = lastViolators.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry<BotID, Long> entry = iter.next();
			if (TimeUnit.NANOSECONDS.toMillis(timestamp - entry.getValue()) > VIOLATOR_COOLDOWN_TIME)
			{
				iter.remove();
			}
		}
		
		Set<BotID> newViolators = Sets.difference(curViolators, lastViolators.keySet()).immutableCopy();
		Optional<BotID> optViolator = newViolators.stream().findFirst();
		
		if (optViolator.isPresent())
		{
			BotID violator = optViolator.get();
			lastViolators.put(violator, timestamp);
			
			ITrackedBot bot = frame.getWorldFrame().getBot(violator);
			double distance = calcDistance(ballPos, bot.getPos());
			
			GameEvent violation = new DistanceViolation(EGameEvent.DEFENDER_TO_KICK_POINT_DISTANCE,
					frame.getTimestamp(), violator, null, distance);
			return Optional.of(violation);
		}
		
		return Optional.empty();
	}
	
	
	private double calcDistance(final IVector2 ballPos, final IVector2 botPos)
	{
		Circle circle = new Circle(ballPos, Geometry.getBotToBallDistanceStop());
		IVector2 nearestPointOutsideCircle = circle.nearestPointOutside(botPos);
		return GeoMath.distancePP(nearestPointOutsideCircle, botPos);
	}
	
	
	private Set<BotID> getViolators(final IAutoRefFrame frame)
	{
		EGameStateNeutral state = frame.getGameState();
		ETeamColor attackingColor = state.getTeamColor();
		
		IBotIDMap<ITrackedBot> bots = frame.getWorldFrame().getBots();
		List<ITrackedBot> defendingBots = AutoRefUtil.filterByColor(bots, attackingColor.opposite());
		
		
		Set<BotID> violators = new HashSet<>();
		long curTimestamp = frame.getTimestamp();
		/*
		 * Only consider bots which have fully entered the circle
		 */
		Circle outerCircle = new Circle(ballPos, Geometry.getBotToBallDistanceStop() - Geometry.getBotRadius());
		Circle innerCircle = new Circle(ballPos, Geometry.getBotToBallDistanceStop() / 2);
		
		if (STRICT_MODE == true)
		{
			violators.addAll(botsInCircle(defendingBots, outerCircle));
		} else
		{
			Set<BotID> innerCircleViolators = botsInCircle(defendingBots, innerCircle);
			violators.addAll(innerCircleViolators);
			
			Set<BotID> outerCircleViolators = Sets.difference(botsInCircle(defendingBots, outerCircle),
					innerCircleViolators);
			Set<BotID> newViolators = Sets.difference(outerCircleViolators, outerCircleBots.keySet()).immutableCopy();
			Set<BotID> oldViolators = Sets.difference(outerCircleBots.keySet(), outerCircleViolators).immutableCopy();
			
			newViolators.forEach(id -> outerCircleBots.put(id, curTimestamp));
			oldViolators.forEach(id -> outerCircleBots.remove(id));
			
			outerCircleBots.forEach((id, entryTimestamp) -> {
				if ((curTimestamp - entryTimestamp) > TimeUnit.MILLISECONDS.toNanos(MAX_OUTER_CIRCLE_LINGER_TIME))
				{
					violators.add(id);
				}
			});
		}
		
		return violators;
	}
	
	
	private Set<BotID> botsInCircle(final List<ITrackedBot> bots, final Circle circle)
	{
		return bots.stream()
				.filter(bot -> circle.isPointInShape(bot.getPos()))
				.map(ToBotIDMapper.get())
				.collect(Collectors.toSet());
	}
	
	
	@Override
	protected void doReset()
	{
		lastViolators.clear();
		outerCircleBots.clear();
	}
}
