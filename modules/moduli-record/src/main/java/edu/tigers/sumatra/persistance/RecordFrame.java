/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistance;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Entity
public class RecordFrame
{
	@PrimaryKey
	private final long											timestamp;
	
	private final long											timestampMs	= System.currentTimeMillis();
	
	private final WorldFrameWrapper							worldFrameWrapper;
	
	private final Map<ETeamColor, VisualizationFrame>	visFrames	= new HashMap<>(2);
	
	
	@SuppressWarnings("unused")
	private RecordFrame()
	{
		timestamp = 0;
		worldFrameWrapper = null;
	}
	
	
	/**
	 * @param worldFrameWrapper
	 */
	public RecordFrame(final WorldFrameWrapper worldFrameWrapper)
	{
		timestamp = worldFrameWrapper.getSimpleWorldFrame().getTimestamp();
		this.worldFrameWrapper = worldFrameWrapper;
	}
	
	
	/**
	 * @return the timestampMs
	 */
	public final long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the timestampMs
	 */
	public final long getTimestampMs()
	{
		return timestampMs;
	}
	
	
	/**
	 * @param visFrame
	 */
	public synchronized final void addVisFrame(final VisualizationFrame visFrame)
	{
		assert visFrame != null;
		visFrames.put(visFrame.getTeamColor(), visFrame);
	}
	
	
	/**
	 * @param teamColor
	 * @return
	 */
	public synchronized final VisualizationFrame getVisFrame(final ETeamColor teamColor)
	{
		VisualizationFrame visFrame = visFrames.get(teamColor);
		visFrame.setWorldFrameWrapper(worldFrameWrapper);
		return visFrame;
	}
	
	
	/**
	 * @return
	 */
	public synchronized final Collection<VisualizationFrame> getVisFrames()
	{
		for (VisualizationFrame visFrame : visFrames.values())
		{
			assert visFrame != null;
			visFrame.setWorldFrameWrapper(worldFrameWrapper);
		}
		return Collections.unmodifiableCollection(visFrames.values());
	}
	
	
	/**
	 * @return the worldFrameWrapper
	 */
	public final WorldFrameWrapper getWorldFrameWrapper()
	{
		return worldFrameWrapper;
	}
}
