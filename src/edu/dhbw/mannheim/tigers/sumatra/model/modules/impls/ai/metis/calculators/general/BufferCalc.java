/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 6, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BufferCalc extends ACalculator
{
	@Configurable(comment = "Number of frames to keep in buffer for bots")
	private static int	maxBufferSizeBots		= 120;
	@Configurable(comment = "Number of frames to keep in buffer for balls")
	private static int	maxBufferSizeBalls	= 120;
	@Configurable(comment = "max time to keep bots in buffer")
	private static int	maxPastTimeBots		= 3000;
	@Configurable(comment = "Points will only be kept, if the distance is higher than this.")
	private static float	betweenPointsTol		= 20;
	
	private Set<BotID>	allSeenBotIds			= new HashSet<>();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		// 1. take last buffer
		for (Map.Entry<BotID, SortedMap<Long, IVector2>> entry : baseAiFrame.getPrevFrame().getTacticalField()
				.getBotPosBuffer().entrySet())
		{
			if (!entry.getValue().isEmpty())
			{
				newTacticalField.getBotPosBuffer().put(entry.getKey(), new TreeMap<>(entry.getValue()));
			}
		}
		
		// refresh all seen bots set
		// allSeenBotIds.addAll(baseAiFrame.getWorldFrame().getTigerBotsVisible().keySet());
		allSeenBotIds.addAll(baseAiFrame.getWorldFrame().getBots().keySet());
		
		// 2. add new entry
		for (BotID botId : allSeenBotIds)
		{
			newTacticalField.getBotPosBuffer().putIfAbsent(botId, new TreeMap<>());
			SortedMap<Long, IVector2> map = newTacticalField.getBotPosBuffer().get(botId);
			TrackedTigerBot tBot = baseAiFrame.getWorldFrame().getBot(botId);
			if ((map.size() > 1) && (tBot != null))
			{
				Long lastKey = map.lastKey();
				float dist = GeoMath.distancePP(map.get(lastKey), tBot.getPos());
				if (dist < betweenPointsTol)
				{
					map.remove(lastKey);
				}
				if (map.size() >= maxBufferSizeBots)
				{
					map.remove(map.firstKey());
				}
			}
			Set<Long> toBeRemoved = new HashSet<Long>();
			for (Long t : map.keySet())
			{
				if ((SumatraClock.nanoTime() - t) > (1e6 * maxPastTimeBots))
				{
					toBeRemoved.add(t);
				}
			}
			for (Long t : toBeRemoved)
			{
				map.remove(t);
			}
			if (tBot != null)
			{
				map.put(SumatraClock.nanoTime(), tBot.getPos());
			}
		}
		
		
		List<TrackedBall> lastBallBuffer = baseAiFrame.getPrevFrame().getTacticalField().getBallBuffer();
		List<TrackedBall> newBallBuffer = newTacticalField.getBallBuffer();
		newBallBuffer.addAll(lastBallBuffer);
		if (newBallBuffer.size() == maxBufferSizeBalls)
		{
			newBallBuffer.remove(0);
		}
		newBallBuffer.add(baseAiFrame.getWorldFrame().getBall());
	}
}
