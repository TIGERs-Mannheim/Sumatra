/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;

import java.util.HashMap;
import java.util.Map;


/**
 * @author MarkG
 */
@Persistent()
public class OffensiveAnalysedFrame
{
	
	private double												avgDesiredRoles		= 0;
	private double												avgMinRoles				= 0;
	private double												avgMaxRoles				= 0;
	private Map<BotID, Double>								primaryPercantages	= new HashMap<>();
	private Map<BotID, OffensiveAnalysedBotFrame>	botFrames				= new HashMap<>();
	
	
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
	public double getAvgMinRoles()
	{
		return avgMinRoles;
	}
	
	
	/**
	 * @param avgMinRoles
	 */
	public void setAvgMinRoles(final double avgMinRoles)
	{
		this.avgMinRoles = avgMinRoles;
	}
	
	
	/**
	 * @return
	 */
	public double getAvgMaxRoles()
	{
		return avgMaxRoles;
	}
	
	
	/**
	 * @param avgMaxRoles
	 */
	public void setAvgMaxRoles(final double avgMaxRoles)
	{
		this.avgMaxRoles = avgMaxRoles;
	}
	
	
	/**
	 * @return
	 */
	public Map<BotID, Double> getPrimaryPercantages()
	{
		return primaryPercantages;
	}
	
	
	/**
	 * @param primaryPercantages
	 */
	public void setPrimaryPercantages(final Map<BotID, Double> primaryPercantages)
	{
		this.primaryPercantages = primaryPercantages;
	}
	
	
	/**
	 * @return
	 */
	public Map<BotID, OffensiveAnalysedBotFrame> getBotFrames()
	{
		return botFrames;
	}
	
	
	/**
	 * @param botFrames
	 */
	public void setBotFrames(final Map<BotID, OffensiveAnalysedBotFrame> botFrames)
	{
		this.botFrames = botFrames;
	}
}
