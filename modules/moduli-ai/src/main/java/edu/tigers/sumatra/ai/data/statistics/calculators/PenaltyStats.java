/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 16, 2015
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics.calculators;

import java.util.Comparator;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
@Persistent
public class PenaltyStats
{
	private BotID	bot;
	int				penaltyScored		= 0;
	int				penaltyNotScored	= 0;
	
	
	@SuppressWarnings("unused")
	private PenaltyStats()
	{
	}
	
	
	/**
	 * @param bot
	 */
	public PenaltyStats(final BotID bot)
	{
		this.bot = bot;
	}
	
	
	/**
	 * Increments penalty goal shots with goal
	 */
	public void addScoredGoal()
	{
		penaltyScored++;
	}
	
	
	/**
	 * Increments penalty goal shots without goal
	 */
	public void addNotScoredGoal()
	{
		penaltyNotScored++;
	}
	
	
	/**
	 * @return
	 */
	public int getSummedScore()
	{
		return penaltyScored - penaltyNotScored;
	}
	
	
	/**
	 * @return
	 */
	public BotID getBotID()
	{
		return bot;
	}
	
	/**
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	public static class PenaltyStatsComparator implements Comparator<PenaltyStats>
	{
		@Override
		public int compare(final PenaltyStats bot1, final PenaltyStats bot2)
		{
			return (int) Math.signum(bot2.getSummedScore() - bot1.getSummedScore());
		}
	}
}
