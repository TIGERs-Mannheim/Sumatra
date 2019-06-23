package edu.tigers.sumatra.vision;

import edu.tigers.sumatra.vision.data.FilteredVisionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@FunctionalInterface
public interface IVisionFilterObserver
{
	/**
	 * @param filteredVisionFrame a filtered and complete vision frame
	 */
	void onNewFilteredVisionFrame(FilteredVisionFrame filteredVisionFrame);
}
