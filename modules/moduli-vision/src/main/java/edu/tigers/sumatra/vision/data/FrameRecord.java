package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;
import lombok.Value;

import java.util.List;


/**
 * Data container for a single frame with robots and a single ball.
 */
@Value
public class FrameRecord
{
	MergedBall ball;
	List<FilteredVisionBot> robots;
}