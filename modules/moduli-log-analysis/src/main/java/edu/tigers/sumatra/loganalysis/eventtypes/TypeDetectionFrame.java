/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes;

import java.util.Optional;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.loganalysis.GameMemory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class TypeDetectionFrame
{
	/** current world frame */
	private final WorldFrameWrapper worldFrameWrapper;

	/** memory of last ITrackedObjects */
	private final GameMemory memory;

	/** close bots to ball position */
	private final ITrackedBot closestBotToBall;
	private final ITrackedBot secondClosestBotToBall;

	/** class for drawing shapeMap in the visualizer */
	private final ShapeMap shapeMap;

	private final long frameId;


	public TypeDetectionFrame(final WorldFrameWrapper worldFrameWrapper, final GameMemory memory,
			final ITrackedBot closestBotToBall, final Optional<ITrackedBot> secondClosestBotToBall,
			final ShapeMap shapes, final long frameId)
	{
		this.worldFrameWrapper = worldFrameWrapper;
		this.memory = memory;
		this.closestBotToBall = closestBotToBall;
		this.secondClosestBotToBall = secondClosestBotToBall.orElse(null);
		this.shapeMap = shapes;
		this.frameId = frameId;
	}


	public WorldFrameWrapper getWorldFrameWrapper()
	{
		return worldFrameWrapper;
	}


	public long getFrameId()
	{
		return frameId;
	}


	public GameMemory getMemory()
	{
		return memory;
	}


	public ITrackedBot getClosestBotToBall()
	{
		return closestBotToBall;
	}


	public Optional<ITrackedBot> getSecondClosestBotToBall()
	{
		return Optional.ofNullable(secondClosestBotToBall);
	}


	public ShapeMap getShapeMap()
	{
		return shapeMap;
	}
}
