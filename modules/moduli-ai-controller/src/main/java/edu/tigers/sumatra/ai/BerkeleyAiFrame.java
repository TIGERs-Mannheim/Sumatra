/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import edu.tigers.sumatra.ids.ETeamColor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;


/**
 * Data entity for AI data
 */
@Entity
@Value
@RequiredArgsConstructor
public class BerkeleyAiFrame
{
	@PrimaryKey
	long timestamp;

	Map<ETeamColor, VisualizationFrame> visFrames = new EnumMap<>(ETeamColor.class);

	@SuppressWarnings("unused")
	private BerkeleyAiFrame()
	{
		timestamp = 0;
	}


	/**
	 * @param visFrame to be added
	 */
	public synchronized void addVisFrame(final VisualizationFrame visFrame)
	{
		visFrames.put(visFrame.getTeamColor(), visFrame);
	}


	/**
	 * @param teamColor for frame
	 * @return frame for given team
	 */
	public synchronized VisualizationFrame getVisFrame(final ETeamColor teamColor)
	{
		return visFrames.get(teamColor);
	}


	/**
	 * @return all frames
	 */
	public synchronized Collection<VisualizationFrame> getVisFrames()
	{
		return Collections.unmodifiableCollection(visFrames.values());
	}
}
