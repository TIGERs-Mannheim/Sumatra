/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena.roleassigner;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ids.BotID;


/**
 * Info about role mappings
 */
public class RoleMapping
{
	private final List<BotID> desiredBots = new ArrayList<>(8);
	
	
	/**
	 * @return the desiredBots
	 */
	public List<BotID> getDesiredBots()
	{
		return desiredBots;
	}
	
	
	@Override
	public String toString()
	{
		return desiredBots.toString();
	}
}
