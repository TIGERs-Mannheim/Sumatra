package edu.tigers.sumatra.vision;

import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.IBallModelIdentificationObserver;


/**
 * Observer for updates from vision filter
 */
public interface IVisionFilterObserver extends IBallModelIdentificationObserver
{
	/**
	 * @param filteredVisionFrame a filtered and complete vision frame
	 */
	default void onNewFilteredVisionFrame(FilteredVisionFrame filteredVisionFrame)
	{
	}
	
	
	/**
	 * Calculated viewport changed.
	 * 
	 * @param cameraId
	 * @param viewport
	 */
	default void onViewportUpdated(final int cameraId, final IRectangle viewport)
	{
	}
}
