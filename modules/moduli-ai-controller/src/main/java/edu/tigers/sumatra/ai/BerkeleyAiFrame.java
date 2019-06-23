/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Data entity for AI data
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Entity
public class BerkeleyAiFrame
{
	@PrimaryKey
	private final long timestamp;
	
	private final Map<ETeamColor, VisualizationFrame> visFrames = new EnumMap<>(ETeamColor.class);
	
	
	@SuppressWarnings("unused")
	private BerkeleyAiFrame()
	{
		timestamp = 0;
	}
	
	
	/**
	 * @param timestamp
	 */
	public BerkeleyAiFrame(final long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	
	/**
	 * @return the timestampMs
	 */
	public final long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @param visFrame to be added
	 */
	public final synchronized void addVisFrame(final VisualizationFrame visFrame)
	{
		assert visFrame != null;
		visFrames.put(visFrame.getTeamColor(), visFrame);
	}
	
	
	/**
	 * @param teamColor for frame
	 * @return frame for given team
	 */
	public final synchronized VisualizationFrame getVisFrame(final ETeamColor teamColor)
	{
		return visFrames.get(teamColor);
	}
	
	
	/**
	 * @return all frames
	 */
	public final synchronized Collection<VisualizationFrame> getVisFrames()
	{
		return Collections.unmodifiableCollection(visFrames.values());
	}
}
