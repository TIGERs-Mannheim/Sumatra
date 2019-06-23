/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.data;

import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author: MarkG
 */
public class TemporaryOffensiveInformation
{
	private ITrackedBot primaryBot;
	
	
	public ITrackedBot getPrimaryBot()
	{
		return primaryBot;
	}
	
	
	public void setPrimaryBot(final ITrackedBot primaryBot)
	{
		this.primaryBot = primaryBot;
	}
}
