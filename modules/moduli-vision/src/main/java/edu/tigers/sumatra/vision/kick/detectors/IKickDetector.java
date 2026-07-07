package edu.tigers.sumatra.vision.kick.detectors;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.KickEvent;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;

import java.util.List;


/**
 * Kick detector interface.
 *
 * @author AndreR <andre@ryll.cc>
 */
public interface IKickDetector
{
	/**
	 * Add new flat ball record to the kick detector.
	 *
	 * @param mergedBall
	 * @param mergedRobots
	 * @return KickEvent if one occured, null otherwise
	 */
	KickEvent addRecord(final MergedBall mergedBall, final List<FilteredVisionBot> mergedRobots);


	/**
	 * Clear history and kick timestamp.
	 */
	void reset();


	/**
	 * Get shapes.
	 *
	 * @return
	 */
	List<IDrawableShape> getDrawableShapes();
}
