/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import edu.tigers.sumatra.ids.BotID;


/**
 * Assignment of bot ids to a threat.
 */
public class DefenseThreatAssignment
{
	private final IDefenseThreat threat;
	private final Set<BotID> botIds;
	
	
	/**
	 * Creates a new DefenseThreatAssignment
	 * 
	 * @param threat
	 * @param botIds
	 */
	public DefenseThreatAssignment(final IDefenseThreat threat, final Set<BotID> botIds)
	{
		this.threat = threat;
		this.botIds = Collections.unmodifiableSet(botIds);
	}
	
	
	public IDefenseThreat getThreat()
	{
		return threat;
	}
	
	
	public Set<BotID> getBotIds()
	{
		return botIds;
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("threat", threat)
				.append("botIds", botIds)
				.toString();
	}
}
