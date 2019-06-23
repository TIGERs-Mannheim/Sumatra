/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.interchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.tigers.sumatra.ids.BotID;


/**
 * Data structure for interchange bots
 */
public class BotInterchange
{
	private List<BotID> weakBots = new ArrayList<>();
	private Set<BotID> desiredInterchangeBots = new HashSet<>();
	private int numInterchangeBots = 0;
	
	
	public List<BotID> getWeakBots()
	{
		return Collections.unmodifiableList(weakBots);
	}
	
	
	public void setWeakBots(final List<BotID> weakBots)
	{
		this.weakBots = weakBots;
	}
	
	
	public Set<BotID> getDesiredInterchangeBots()
	{
		return Collections.unmodifiableSet(desiredInterchangeBots);
	}
	
	
	public void setDesiredInterchangeBots(final Set<BotID> desiredInterchangeBots)
	{
		this.desiredInterchangeBots = desiredInterchangeBots;
	}
	
	
	public int getNumInterchangeBots()
	{
		return numInterchangeBots;
	}
	
	
	public void setNumInterchangeBots(final int numInterchangeBots)
	{
		this.numInterchangeBots = numInterchangeBots;
	}
}
