package edu.tigers.sumatra.vision;

import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IVisionFilterObserver
{
	/**
	 * @param filteredVisionFrame a filtered and complete vision frame
	 */
	void onNewFilteredVisionFrame(FilteredVisionFrame filteredVisionFrame);
	
	
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
