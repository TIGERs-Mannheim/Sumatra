/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes;

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

	/** closest bot to ball position */
	private final ITrackedBot nextBotToBall;

	/** class for drawing shapeMap in the visualizer */
	private final ShapeMap shapeMap;
	
	
	public TypeDetectionFrame(final WorldFrameWrapper worldFrameWrapper, final GameMemory memory,
			final ITrackedBot nextBotToBall,
			final ShapeMap shapes)
	{
		this.worldFrameWrapper = worldFrameWrapper;
		this.memory = memory;
		this.nextBotToBall = nextBotToBall;
		this.shapeMap = shapes;
	}
	
	
	public WorldFrameWrapper getWorldFrameWrapper()
	{
		return worldFrameWrapper;
	}
	
	
	public GameMemory getMemory()
	{
		return memory;
	}
	
	
	public ITrackedBot getNextBotToBall()
	{
		return nextBotToBall;
	}
	
	
	public ShapeMap getShapeMap()
	{
		return shapeMap;
	}
}
