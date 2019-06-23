/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.statistics.MarkovChain;
import edu.tigers.sumatra.statistics.Percentage;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
@Persistent(version = 2)
public class StatisticData
{
	private Map<BotID, ?> botSpecificStatistics;
	private Object generalStatistic;
	
	
	@SuppressWarnings("unused")
	private StatisticData()
	{
	}
	
	
	/**
	 * This is the constructor for the StatisticsData class.
	 * This class should be used to store data for statistics in a generic way.
	 * 
	 * @param botSpecificStatistics The statistics that are different for every bot
	 * @param generalStatistic The general statistic, this should be in the most cases the sum of the single Counts
	 */
	public StatisticData(final Map<BotID, ?> botSpecificStatistics, final Object generalStatistic)
	{
		this.botSpecificStatistics = botSpecificStatistics;
		this.generalStatistic = generalStatistic;
	}
	
	
	/**
	 * This constructor will create a statistic data from a map of markov chains
	 * 
	 * @param mappedChain The map of chains that should be transitioned to statistic
	 * @param stateOne The state that is the beginning
	 * @param stateTwo The state that is the end
	 */
	public <T> StatisticData(final Map<BotID, MarkovChain<T>> mappedChain, final T stateOne, final T stateTwo)
	{
		Integer sumTransitions = 0;
		Map<BotID, Integer> tempList = new HashMap<>();
		
		for (Map.Entry<BotID, MarkovChain<T>> entry : mappedChain.entrySet())
		{
			Integer botTransitions = entry.getValue().getAbsoluteCountTransitions(stateOne, stateTwo);
			tempList.put(entry.getKey(), botTransitions);
			sumTransitions += botTransitions;
		}
		
		botSpecificStatistics = tempList;
		generalStatistic = sumTransitions;
	}
	
	
	/**
	 * @return all bot ids that are referenced in this statistics
	 */
	public Set<BotID> getContainedBotIds()
	{
		return botSpecificStatistics.keySet();
	}
	
	
	/**
	 * Returns a map that contains the text representation of a bot specific statistic
	 * 
	 * @return Textual representation
	 */
	public Map<BotID, String> getTextualRepresentationOfBotStatistics()
	{
		Map<BotID, String> textualRepresentation = new HashMap<>();
		
		for (Map.Entry<BotID, ?> entry : botSpecificStatistics.entrySet())
		{
			textualRepresentation.put(entry.getKey(), getTextualRepresentation(entry.getValue()));
		}
		
		return textualRepresentation;
	}
	
	
	/**
	 * This will be used to get the general Statistic as human readable text
	 * 
	 * @return The String that represents the general information
	 */
	public String getTextualRepresenationOfGeneralStatistic()
	{
		return getTextualRepresentation(generalStatistic);
	}
	
	
	private String getTextualRepresentation(final Object o)
	{
		if (o == null)
		{
			return null;
		}
		
		if (o instanceof Percentage)
		{
			DecimalFormat df = new DecimalFormat("###.#%");
			
			return df.format(((Percentage) o).getPercent());
		}
		return o.toString();
	}
}
