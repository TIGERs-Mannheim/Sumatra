/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule monitors the bot to ball distance of the defending team during a freekick situation and restarts the play
 * if necessary.
 * 
 * @author Lukas Magel
 */
public class DefenderToKickPointDistanceDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	
	@Configurable(comment = "If disabled only bots that are on a collision course with the ball will be considered violators")
	private static boolean strictMode = true;
	
	@Configurable(comment = "[ms] The amount of time a bot can be located inside the outer circle (500mm>x>250mm from the kick pos) without logging a violation")
	private static long maxOuterCircleLingerTime = 3_000;
	
	@Configurable(comment = "[ms] The amount of time before a violation is reported again for the same bot")
	private static long violatorCooldownTime = 1_500;
	
	@Configurable(comment = "[mm] Distance tolerance to apply to required distance to ball")
	private static double distanceTolerance = 50;
	
	private IVector2 ballPos = null;
	private Map<BotID, Long> lastViolators = new HashMap<>();
	private Map<BotID, Long> outerCircleBots = new HashMap<>();
	
	static
	{
		AGameEventDetector.registerClass(DefenderToKickPointDistanceDetector.class);
	}
	
	
	/**
	 * Create new instance
	 */
	public DefenderToKickPointDistanceDetector()
	{
		super(EnumSet.of(
				EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE, EGameState.KICKOFF));
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
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
			if (TimeUnit.NANOSECONDS.toMillis(timestamp - entry.getValue()) > violatorCooldownTime)
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
		return VectorMath.distancePP(ballPos, botPos) - Geometry.getBotToBallDistanceStop() - Geometry.getBotRadius();
	}
	
	
	private Set<BotID> getViolators(final IAutoRefFrame frame)
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
				Geometry.getBotToBallDistanceStop() + Geometry.getBotRadius() - distanceTolerance);
		
		if (strictMode)
		{
			violators.addAll(botsInCircle(defendingBots, outerCircle));
		} else
		{
			ICircle innerCircle = Circle.createCircle(ballPos, Geometry.getBotToBallDistanceStop() / 2);
			Set<BotID> innerCircleViolators = botsInCircle(defendingBots, innerCircle);
			violators.addAll(innerCircleViolators);
			
			Set<BotID> outerCircleViolators = Sets.difference(botsInCircle(defendingBots, outerCircle),
					innerCircleViolators);
			Set<BotID> newViolators = Sets.difference(outerCircleViolators, outerCircleBots.keySet()).immutableCopy();
			Set<BotID> oldViolators = Sets.difference(outerCircleBots.keySet(), outerCircleViolators).immutableCopy();
			
			newViolators.forEach(id -> outerCircleBots.put(id, curTimestamp));
			oldViolators.forEach(outerCircleBots::remove);
			
			outerCircleBots.forEach((id, entryTimestamp) -> {
				if ((curTimestamp - entryTimestamp) > TimeUnit.MILLISECONDS.toNanos(maxOuterCircleLingerTime))
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
