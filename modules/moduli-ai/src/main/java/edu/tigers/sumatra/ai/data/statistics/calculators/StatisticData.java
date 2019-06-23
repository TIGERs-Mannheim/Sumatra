/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 31, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics.calculators;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.data.Percentage;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.statistics.MarkovChain;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
@Persistent
public class StatisticData
{
	private Map<BotID, ? extends Object>	botspecificStatistics;
	private Object									generalStatistic;
	
	
	@SuppressWarnings("unused")
	private StatisticData()
	{
		
	}
	
	
	/**
	 * This is the constructor for the StatisticsData class.
	 * This class should be used to store data for statistics in a generic way.
	 * 
	 * @param botspecificStatistics The statistics that are different for every bot
	 * @param generalStatistic The general statistic, this should be in the most cases the sum of the single Counts
	 */
	public StatisticData(final Map<BotID, ? extends Object> botspecificStatistics, final Object generalStatistic)
	{
		this.botspecificStatistics = botspecificStatistics;
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
		
		for (BotID bot : mappedChain.keySet())
		{
			Integer botTransitions = mappedChain.get(bot).getAbsoluteCountTransitions(stateOne, stateTwo);
			tempList.put(bot, botTransitions);
			sumTransitions += botTransitions;
		}
		
		botspecificStatistics = tempList;
		generalStatistic = sumTransitions;
	}
	
	
	/**
	 * Returns a map that contains the text representation of a bot specific statistic
	 * 
	 * @return Textual representation
	 */
	public Map<BotID, String> getTextualRepresentationOfBotStatistics()
	{
		Map<BotID, String> textualRepresentation = new HashMap<BotID, String>();
		
		for (BotID bot : botspecificStatistics.keySet())
		{
			textualRepresentation.put(bot, getTextualRepresentation(botspecificStatistics.get(bot)));
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
