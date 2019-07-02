/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis;

import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Stores/memorize values of ITrackedObjects from previous frames
 */
public class GameMemory
{

	public enum GameLogObject
	{
		BALL_LINE_DETECTION,
		BALL,
		CLOSEST_BOT
	}
	
	private int memorySize;
	
	private List<Long> frameTimestamps = new LinkedList<>();
	
	private Map<GameLogObject, List<ITrackedObject>> trackedObjects = new EnumMap<>(GameLogObject.class);
	private IKickEvent lastKickEvent;
	
	
	public GameMemory(final int memorySize)
	{
		this.memorySize = memorySize;

		Arrays.stream(GameLogObject.values()).forEach(obj -> trackedObjects.put(obj, new LinkedList<>()));
	}
	
	
	public void updateFrame(SimpleWorldFrame frame)
	{
		frameTimestamps.add(0, frame.getTimestamp());
		
		if (frameTimestamps.size() > memorySize)
		{
			frameTimestamps.remove(frameTimestamps.size() - 1);
		}
	}
	
	
	public void update(GameLogObject id, ITrackedObject obj)
	{
		trackedObjects.get(id).add(0, obj);
		
		if (trackedObjects.get(id).size() > memorySize)
		{
			trackedObjects.get(id).remove(trackedObjects.get(id).size() - 1);
		}
	}
	
	
	public void updateLastKickEvent(IKickEvent kickEvent)
	{
		lastKickEvent = kickEvent;
	}

	public void clear()
	{
		frameTimestamps.clear();
		lastKickEvent = null;
		trackedObjects.values().forEach(List::clear);
	}
	

	public List<ITrackedObject> get(GameLogObject id)
	{
		return trackedObjects.get(id);
	}
	
	
	public ITrackedObject get(GameLogObject id, int index)
	{
		return trackedObjects.get(id).get(index);
	}
	
	
	public long getTimestamp(int index)
	{
		return frameTimestamps.get(index);
	}
	
	
	public int getTimestampSize()
	{
		return frameTimestamps.size();
	}
	
	
	public IKickEvent getLastKickEvent()
	{
		return lastKickEvent;
	}
	
}