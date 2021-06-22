/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Gero
 */
public interface IWorldFrameObserver
{
	/**
	 * A new worldframe is coming in. Note that there is a worldframe for each team color,
	 * so you may need to sort some out
	 *
	 * @param wFrameWrapper
	 */
	default void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
	}


	/**
	 * This is called if the WP is stopped (Sumatra closes/stops)
	 */
	default void onClearWorldFrame()
	{
	}


	/**
	 * @param frame
	 */
	default void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
	}


	/**
	 * Vision lost. Clear your state.
	 */
	default void onClearCamDetectionFrame()
	{
	}


	/**
	 * Update the shape map with given identifier
	 *
	 * @param timestamp
	 * @param shapeMap the new shape map
	 * @param source the source identifier for this shape map that should be replaced
	 */
	default void onNewShapeMap(final long timestamp, ShapeMap shapeMap, ShapeMapSource source)
	{
	}


	/**
	 * Remove shapes from given shape map source name
	 *
	 * @param sourceName
	 */
	default void onRemoveSourceFromShapeMap(String sourceName)
	{
	}


	/**
	 * Remove shapes from given shape map category
	 *
	 * @param category
	 */
	default void onRemoveCategoryFromShapeMap(String... category)
	{
	}
}
