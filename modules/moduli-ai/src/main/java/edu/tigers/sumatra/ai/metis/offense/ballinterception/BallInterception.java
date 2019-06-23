/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import edu.tigers.sumatra.ids.BotID;


public class BallInterception
{
	private final BotID botID;
	private final boolean interceptable;
	private final double ballContactTime;
	private final double distanceToBallWhenOnBallLine;
	
	
	public BallInterception(final BotID botID, final boolean interceptable, final double ballContactTime,
			final double distanceToBallWhenOnBallLine)
	{
		this.botID = botID;
		this.interceptable = interceptable;
		this.ballContactTime = ballContactTime;
		this.distanceToBallWhenOnBallLine = distanceToBallWhenOnBallLine;
	}
	
	
	public BotID getBotID()
	{
		return botID;
	}
	
	
	public boolean isInterceptable()
	{
		return interceptable;
	}
	
	
	public double getBallContactTime()
	{
		return ballContactTime;
	}
	
	
	public double getDistanceToBallWhenOnBallLine()
	{
		return distanceToBallWhenOnBallLine;
	}
}
