package edu.tigers.sumatra.vision.kick.validators;

import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;

import java.util.List;


/**
 * Check a single criterion for a kick.
 *
 * @author AndreR <andre@ryll.cc>
 */
public interface IKickValidator
{
	/**
	 * Name of this validator for visualization purposes.
	 *
	 * @return
	 */
	String getName();


	/**
	 * Check the validator's condition.
	 *
	 * @param bots Filtered bots
	 * @param balls Most recent merged balls. Number of balls is set in KickDetector.
	 * @return true if the validator's condition for a kick is satisfied
	 */
	boolean validateKick(List<FilteredVisionBot> bots, List<MergedBall> balls);
}
