/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 18, 2016
 * Author(s): Sion Sander
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events.impl;

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
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;


/**
 * This Rule detects attackers that touch the GoalKeeper
 * 
 * @author Simon Sander
 */
public class AttackerTouchKeeperDetector extends AGameEventDetector
{
	private static final int		priority						= 1;
	
	@Configurable(comment = "[mm] The minimal distance that is not considered a contact")
	private static final double	TOUCH_DISTANCE				= 10;
	
	@Configurable(comment = "[ms] The amount of time before a violation is reported again for the same bot")
	private static long				VIOLATOR_COOLDOWN_TIME	= 1_500;
	
	private Map<BotID, Long>		oldViolators				= new HashMap<>();
	
	
	/**
	 * 
	 */
	public AttackerTouchKeeperDetector()
	{
		super(EGameStateNeutral.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		IBotIDMap<ITrackedBot> bots = frame.getWorldFrame().getBots();
		long timestamp = frame.getTimestamp();
		Set<BotID> keepers = TeamConfig.getKeeperIDs();
		
		Set<BotID> violators = new HashSet<>();
		
		for (BotID keeperID : keepers)
		{
			if (!bots.containsKey(keeperID))
			{
				/*
				 * The keeper is omitted by the world predictor in this frame (maybe due to vision problems) or not located
				 * on the field. In this case it is skipped until it reappears on the field.
				 */
				continue;
			}
			ITrackedBot keeper = bots.get(keeperID);
			
			// Only check for violators if the keeper is positioned inside his own penalty area
			PenaltyArea penArea = NGeometry.getPenaltyArea(keeperID.getTeamColor());
			if (penArea.isPointInShape(keeper.getPos()))
			{
				violators.addAll(getViolators(bots, keeper));
			}
		}
		
		/*
		 * Update the timestamp of all violators for which a violation has already been generated but which are still
		 * violating the rule
		 */
		Sets.intersection(violators, oldViolators.keySet()).forEach(bot -> oldViolators.put(bot, timestamp));
		
		/*
		 * Remove all old violators which have reached the cooldown time
		 */
		Iterator<Map.Entry<BotID, Long>> iter = oldViolators.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry<BotID, Long> entry = iter.next();
			if (TimeUnit.NANOSECONDS.toMillis(timestamp - entry.getValue()) > VIOLATOR_COOLDOWN_TIME)
			{
				iter.remove();
			}
		}
		
		// get the first new violator
		Optional<BotID> violatorID = Sets.difference(violators, oldViolators.keySet()).stream().findFirst();
		
		if (violatorID.isPresent() && bots.containsKey(violatorID.get()))
		{
			ITrackedBot violator = bots.get(violatorID.get());
			IVector2 violatorPos = violator.getPos();
			ETeamColor violatorTeamColor = violatorID.get().getTeamColor();
			
			FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, violatorTeamColor.opposite(),
					AutoRefMath.getClosestFreekickPos(violatorPos, violatorTeamColor.opposite()));
			GameEvent violation = new GameEvent(EGameEvent.ATTACKER_TOUCH_KEEPER, frame.getTimestamp(),
					violatorID.get(), followUp);
			
			// add current validator to old validator
			oldViolators.put(violatorID.get(), timestamp);
			
			return Optional.of(violation);
			
		}
		return Optional.empty();
	}
	
	
	private Set<BotID> getViolators(final IBotIDMap<ITrackedBot> bots, final ITrackedBot target)
	{
		ETeamColor targetColor = target.getBotId().getTeamColor();
		Circle circle = new Circle(target.getPos(), TOUCH_DISTANCE + (Geometry.getBotRadius() * 2));
		
		List<ITrackedBot> attackingBots = AutoRefUtil.filterByColor(bots, targetColor.opposite());
		
		return attackingBots.stream()
				.filter(bot -> circle.isPointInShape(bot.getPos(), 0))
				.map(ToBotIDMapper.get())
				.collect(Collectors.toSet());
	}
	
	
	@Override
	public void reset()
	{
		oldViolators.clear();
	}
	
}
