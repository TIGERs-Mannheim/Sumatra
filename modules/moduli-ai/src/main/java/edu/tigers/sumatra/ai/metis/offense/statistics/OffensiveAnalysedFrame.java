/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.statistics;

import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;


/**
 * @author MarkG
 */
@Persistent(version = 1)
public class OffensiveAnalysedFrame
{
	private double avgDesiredRoles = 0;
	private Map<BotID, Double> primaryPercantages = new HashMap<>();
	private Map<BotID, OffensiveAnalysedBotFrame> botFrames = new HashMap<>();
	
	
	/**
	 * @return
	 */
	public double getAvgDesiredRoles()
	{
		return avgDesiredRoles;
	}
	
	
	/**
	 * @param avgDesiredRoles
	 */
	public void setAvgDesiredRoles(final double avgDesiredRoles)
	{
		this.avgDesiredRoles = avgDesiredRoles;
	}
	
	
	/**
	 * @return
	 */
	public Map<BotID, Double> getPrimaryPercantages()
	{
		return primaryPercantages;
	}
	
	
	/**
	 * @return
	 */
	public Map<BotID, OffensiveAnalysedBotFrame> getBotFrames()
	{
		return botFrames;
	}
}
