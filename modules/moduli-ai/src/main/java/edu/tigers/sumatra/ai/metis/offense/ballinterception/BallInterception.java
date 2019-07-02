/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import java.util.List;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;


public class BallInterception
{
	private final BotID botID;
	private final boolean interceptable;
	private final double ballContactTime;
	private final double distanceToBallWhenOnBallLine;
	private final List<IDrawableShape> shapes;
	private final IVector2 botTarget;
	
	
	public BallInterception(
			final BotID botID,
			final boolean interceptable,
			final double ballContactTime,
			final double distanceToBallWhenOnBallLine,
			final List<IDrawableShape> shapes, final IVector2 botTarget)
	{
		this.botID = botID;
		this.interceptable = interceptable;
		this.ballContactTime = ballContactTime;
		this.distanceToBallWhenOnBallLine = distanceToBallWhenOnBallLine;
		this.shapes = shapes;
		this.botTarget = botTarget;
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
	
	
	public List<IDrawableShape> getShapes()
	{
		return shapes;
	}
	
	
	public IVector2 getBotTarget()
	{
		return botTarget;
	}
}
