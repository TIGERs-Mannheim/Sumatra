package edu.tigers.sumatra.wp.data;

import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
@Value
public class TrajTrackingQuality
{
	double curDistance;
	double maxDistance;
	double timeOffTrajectory;


	@SuppressWarnings("unused")
	public TrajTrackingQuality()
	{
		curDistance = 0;
		maxDistance = 0;
		timeOffTrajectory = 0;
	}
}
