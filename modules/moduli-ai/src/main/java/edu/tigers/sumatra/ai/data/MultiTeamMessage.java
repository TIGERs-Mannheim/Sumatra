/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.data;

/**
 * @author JulianT
 */
public class MultiTeamMessage
{
	private final MultiTeamPlan teamPlan;
	private final boolean mixedTeamMode;
	
	/** a default instance in case of no mixed team mode */
	public static final MultiTeamMessage DEFAULT = new MultiTeamMessage(new MultiTeamPlan(), false);
	
	
	/**
	 * @param teamPlan
	 * @param mixedTeamMode
	 */
	public MultiTeamMessage(final MultiTeamPlan teamPlan, final boolean mixedTeamMode)
	{
		this.teamPlan = teamPlan;
		this.mixedTeamMode = mixedTeamMode;
	}
	
	
	/**
	 * @return
	 */
	public MultiTeamPlan getTeamPlan()
	{
		return teamPlan;
	}
	
	
	public boolean isMixedTeamMode()
	{
		return mixedTeamMode;
	}
}
