/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.summary;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.ESelectionReason;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.PlayStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlayState;


/**
 * all play stats according to one play are collected here
 * 
 * statistic meta values can be calculated out of this
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class PlayInformation
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private List<PlayStats>						playStats;
	private double									avg			= 0;
	private int										avgBase		= 0;
	private double									min			= Double.MAX_VALUE;
	private double									max			= Double.MIN_VALUE;
	private double									botsAvg		= 0;
	private int										botsAvgBase	= 0;
	private double									botsMin		= Double.MAX_VALUE;
	private double									botsMax		= Double.MIN_VALUE;
	private Map<ESelectionReason, Integer>	reason		= new HashMap<ESelectionReason, Integer>();
	private Map<EPlayState, Integer>			result		= new HashMap<EPlayState, Integer>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PlayInformation()
	{
		playStats = new LinkedList<PlayStats>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * adds a play stat
	 * 
	 * @param s
	 */
	public void addPlayStats(PlayStats s)
	{
		playStats.add(s);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int size()
	{
		return playStats.size();
	}
	
	
	/**
	 * fills statistic values out of the list of plays
	 * 
	 */
	public void calcStats()
	{
		for (PlayStats playStat : playStats)
		{
			double duration = playStat.getEndTime() - playStat.getStartTime();
			addToAvg(duration);
			min = Math.min(duration, min);
			max = Math.max(duration, max);
			addToBotAvg(playStat.getNumberOfRoles());
			botsMin = Math.min(playStat.getNumberOfRoles(), botsMin);
			botsMax = Math.max(playStat.getNumberOfRoles(), botsMax);
			addReason(playStat.getSelectionReason());
			addResult(playStat.getResult());
		}
	}
	
	
	private void addToAvg(double toAdd)
	{
		avgBase++;
		avg = ((avg * (avgBase - 1)) + toAdd) / avgBase;
	}
	
	
	private void addToBotAvg(double toAdd)
	{
		botsAvgBase++;
		botsAvg = ((botsAvg * (botsAvgBase - 1)) + toAdd) / botsAvgBase;
	}
	
	
	private void addReason(ESelectionReason sr)
	{
		if (!reason.containsKey(sr))
		{
			reason.put(sr, 0);
		}
		reason.put(sr, reason.get(sr) + 1);
	}
	
	
	private void addResult(EPlayState ps)
	{
		if (!result.containsKey(ps))
		{
			result.put(ps, 0);
		}
		result.put(ps, result.get(ps) + 1);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the playStats
	 */
	public List<PlayStats> getPlayStats()
	{
		return playStats;
	}
	
	
	/**
	 * @return the avg
	 */
	public double getAvg()
	{
		return avg;
	}
	
	
	/**
	 * @return the avgBase
	 */
	public int getAvgBase()
	{
		return avgBase;
	}
	
	
	/**
	 * @return the min
	 */
	public double getMin()
	{
		return min;
	}
	
	
	/**
	 * @return the max
	 */
	public double getMax()
	{
		return max;
	}
	
	
	/**
	 * @return the botsAvg
	 */
	public double getBotsAvg()
	{
		return botsAvg;
	}
	
	
	/**
	 * @return the botsAvgBase
	 */
	public int getBotsAvgBase()
	{
		return botsAvgBase;
	}
	
	
	/**
	 * @return the botsMin
	 */
	public double getBotsMin()
	{
		return botsMin;
	}
	
	
	/**
	 * @return the botsMax
	 */
	public double getBotsMax()
	{
		return botsMax;
	}
	
	
	/**
	 * @return the reason
	 */
	public Map<ESelectionReason, Integer> getReason()
	{
		return reason;
	}
	
	
	/**
	 * @return the result
	 */
	public Map<EPlayState, Integer> getResult()
	{
		return result;
	}
	
}
