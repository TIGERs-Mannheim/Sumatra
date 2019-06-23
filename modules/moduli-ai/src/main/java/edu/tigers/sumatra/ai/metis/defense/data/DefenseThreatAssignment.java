/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class DefenseThreatAssignment
{
	private final AObjectID objectID;
	private final IDefenseThreat threat;
	private final int numDefenders;
	private final Set<BotID> botIds = new HashSet<>();
	private final EDefenseGroup defenseGroup;
	private final boolean crucial;
	
	
	/**
	 * Creates a new DefenseThreatAssignment
	 * 
	 * @param objectID
	 * @param threat
	 * @param numDefenders
	 * @param defenseGroup
	 * @param crucial
	 */
	public DefenseThreatAssignment(final AObjectID objectID, final IDefenseThreat threat,
			final int numDefenders, final EDefenseGroup defenseGroup, final boolean crucial)
	{
		this.objectID = objectID;
		this.threat = threat;
		this.numDefenders = numDefenders;
		this.defenseGroup = defenseGroup;
		this.crucial = crucial;
	}
	
	
	public AObjectID getObjectID()
	{
		return objectID;
	}
	
	
	public IDefenseThreat getThreat()
	{
		return threat;
	}
	
	
	public int getNumDefenders()
	{
		return numDefenders;
	}
	
	
	public Set<BotID> getBotIds()
	{
		return Collections.unmodifiableSet(botIds);
	}
	
	
	public EDefenseGroup getDefenseGroup()
	{
		return defenseGroup;
	}
	
	
	/**
	 * Add bot ids that should be assigned
	 * 
	 * @param botId the bot id to add
	 */
	public void addBotId(final BotID botId)
	{
		botIds.add(botId);
	}
	
	
	/**
	 * @return the crucial
	 */
	public boolean isCrucial()
	{
		return crucial;
	}
}
