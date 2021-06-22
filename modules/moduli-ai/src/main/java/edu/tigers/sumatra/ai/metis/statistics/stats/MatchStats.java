/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import lombok.Value;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Data holder for MatchStatisticsCalc elements.
 */
@Persistent
@Value
public class MatchStats
{
	Map<EBallPossession, Percentage> ballPossessionGeneral = new EnumMap<>(EBallPossession.class);
	Map<EMatchStatistics, StatisticData> statistics = new EnumMap<>(EMatchStatistics.class);

	/**
	 * Default
	 */
	public MatchStats()
	{
		for (EBallPossession bp : EBallPossession.values())
		{
			ballPossessionGeneral.put(bp, new Percentage());
		}
	}

	/**
	 * This adds a specific type of Statistic data to be displayed in the statisticsPanel
	 *
	 * @param key   The Statistic Type to be put
	 * @param value The Statistic to be put
	 */
	public void putStatisticData(final EMatchStatistics key, final StatisticData value)
	{
		statistics.put(key, value);
	}

	/**
	 * @return all contained bots
	 */
	public Set<Integer> getAllBots()
	{
		return statistics.values().stream()
				.map(StatisticData::getContainedBotIds)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}


	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject jsonObject = new JSONObject();
		for (Map.Entry<EBallPossession, Percentage> entry : ballPossessionGeneral.entrySet())
		{
			jsonObject.put(entry.getKey().name(), Double.toString(entry.getValue().getPercent()));
		}
		for (Map.Entry<EMatchStatistics, StatisticData> entry : statistics.entrySet())
		{
			jsonObject.put(entry.getKey(), entry.getValue().formattedGeneralStatistic());
			for (Map.Entry<Integer, String> e : entry.getValue().formattedBotStatistics().entrySet())
			{
				jsonObject.put(entry.getKey().toString() + e.getKey().toString(), e.getValue());
			}
		}
		return jsonObject;
	}
}
