/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import com.sleepycat.persist.model.Persistent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Statistics for a single metric with optional per-bot values.
 */
@Persistent(version = 2)
public class StatisticData
{
	private final Map<Integer, ?> botSpecificStatistics;
	private final Object generalStatistic;


	@SuppressWarnings("unused")
	private StatisticData()
	{
		botSpecificStatistics = new HashMap<>();
		generalStatistic = null;
	}


	/**
	 * Create instance with bot-specific data.
	 *
	 * @param botSpecificStatistics The statistics that are different for every bot
	 * @param generalStatistic      The general statistic, this should be in the most cases the sum of the single Counts
	 */
	public StatisticData(final Map<Integer, ?> botSpecificStatistics, final Object generalStatistic)
	{
		this.botSpecificStatistics = botSpecificStatistics;
		this.generalStatistic = generalStatistic;
	}


	/**
	 * Create instance without bot-specific data.
	 *
	 * @param generalStatistic The general statistic, this should be in the most cases the sum of the single Counts
	 */
	public StatisticData(final Object generalStatistic)
	{
		this.botSpecificStatistics = new HashMap<>();
		this.generalStatistic = generalStatistic;
	}


	/**
	 * @return all bot ids that are referenced in this statistics
	 */
	public Set<Integer> getContainedBotIds()
	{
		return botSpecificStatistics.keySet();
	}


	/**
	 * Returns a map that contains the text representation of a bot specific statistic
	 *
	 * @return Textual representation
	 */
	public Map<Integer, String> formattedBotStatistics()
	{
		Map<Integer, String> textualRepresentation = new HashMap<>();

		for (Map.Entry<Integer, ?> entry : botSpecificStatistics.entrySet())
		{
			textualRepresentation.put(entry.getKey(), format(entry.getValue()));
		}

		return textualRepresentation;
	}


	/**
	 * This will be used to get the general Statistic as human readable text
	 *
	 * @return The String that represents the general information
	 */
	public String formattedGeneralStatistic()
	{
		return format(generalStatistic);
	}


	public Object getGeneralStatistics()
	{
		if (generalStatistic == null)
		{
			return "";
		}
		if (generalStatistic.getClass().equals(Percentage.class))
		{
			return ((Percentage) generalStatistic).getPercent();
		}
		return generalStatistic;
	}


	private String format(final Object o)
	{
		if (o == null)
		{
			return "";
		}

		if (o.getClass().equals(Percentage.class))
		{
			DecimalFormat df = new DecimalFormat("###.#%");

			return df.format(((Percentage) o).getPercent());
		}

		if (o.getClass().equals(Double.class))
		{
			return String.format("%.2f", (Double) o);
		}

		return o.toString();
	}
}
