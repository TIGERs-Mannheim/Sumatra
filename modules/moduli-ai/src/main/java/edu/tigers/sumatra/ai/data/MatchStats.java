/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.statistics.StatisticData;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.statistics.Percentage;


/**
 * Data holder for StatisticsCalc elements for persisting them.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
@Persistent
public class MatchStats
{
	private final Map<EBallPossession, Percentage> ballPossessionGeneral = new EnumMap<>(EBallPossession.class);
	private Map<EMatchStatistics, StatisticData> statistics = new EnumMap<>(EMatchStatistics.class);
	
	/**
	 * This enum is giving an overview of the available statistics
	 */
	public enum EMatchStatistics
	{
		/** This is the statistic for ball possession */
		BALL_POSSESSION("Ball possession"),
		/** This is the statistic for a count how many times it was an active pass target */
		ACTIVE_PASS_TARGET("pass target"),
		/** This is the statstic for total passes in the game */
		COUNT_PASSES("pass count"),
		/** This is the percentage of succesful passes */
		RATIO_SUCCESFUL_PASSES("ratio of succesful passes"),
		/** This is the statistic for scored goals */
		GOALS_SCORED("Goals"),
		/** Shows the relative frames as Offensive */
		FRAMES_AS_OFFENSIVE("Offensive"),
		/** Shows the relative frames as Supporter */
		FRAMES_AS_SUPPORT("Supporter"),
		/** Shows the relative frames as Defender */
		FRAMES_AS_DEFENDER("Defender"),
		/** This is the statistic for won tackles */
		DUELS_WON("Duels won"),
		/** This is the statistic for lost tackles */
		DUELS_LOST("Duels lost"),
		/** This is the statistic for transitions from defensive to offensive */
		DEFENSIVE_TO_OFFENSIVE("Def->Off"),
		/** This is the statistic for transitions from defensive to support */
		DEFENSIVE_TO_SUPPORT("Def->Sup"),
		/** This is the statistic for transitions from offensive to support */
		OFFENSIVE_TO_SUPPORT("Off->Sup"),
		/** This is the statistic for transitions from offensive to defensive */
		OFFENSIVE_TO_DEFENSIVE("Off->Def"),
		/** This is the statistic for transitions from support to defensive */
		SUPPORT_TO_DEFENSIVE("Sup->Def"),
		/** This is the statistic for transitions from support to offensive */
		SUPPORT_TO_OFFENSIVE("Sup->Off"),
		/** This is the statistic for direct Shots*/
		DIRECT_SHOTS("Direct shots"),
		DIRECT_SHOTS_SUCCESS_RATE("Direct shots success Rate"),;
		
		private String descriptor;
		
		
		EMatchStatistics(final String descriptor)
		{
			this.descriptor = descriptor;
		}
		
		
		/**
		 * @return A human readable Descriptor of a specific Statistic
		 */
		public String getDescriptor()
		{
			return descriptor;
		}
	}
	
	
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
	 * @return the statistics
	 */
	public Map<EMatchStatistics, StatisticData> getStatistics()
	{
		return statistics;
	}
	
	
	/**
	 * This adds a specific type of Statistic data to be displayed in the statisticsPanel
	 *
	 * @param key The Statistic Type to be put
	 * @param value The Statistic to be put
	 */
	public void putStatisticData(final EMatchStatistics key, final StatisticData value)
	{
		statistics.put(key, value);
	}
	
	
	/**
	 * @return the ballPossessionGeneral
	 */
	public Map<EBallPossession, Percentage> getBallPossessionGeneral()
	{
		return ballPossessionGeneral;
	}
	
	
	/**
	 * @return all contained bots
	 */
	public Set<BotID> getAllBots()
	{
		return statistics.values().stream()
				.map(StatisticData::getContainedBotIds)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}
}
