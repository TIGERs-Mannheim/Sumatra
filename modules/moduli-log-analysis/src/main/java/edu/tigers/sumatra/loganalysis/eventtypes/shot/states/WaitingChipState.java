/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot.states;

import edu.tigers.sumatra.loganalysis.eventtypes.shot.PassTypeDetectionFrame;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotDetection;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotBuilder;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


public class WaitingChipState extends APassingDetectionState
{
	
	public WaitingChipState(final EPassingDetectionState stateId)
	{
		super(stateId);
	}
	
	
	@Override
	public void nextFrameForDetection(final PassTypeDetectionFrame frame)
	{
		SimpleWorldFrame wf = frame.getWorldFrameWrapper().getSimpleWorldFrame();
		ShotDetection passingDetection = frame.getPassingDetection();
		ShotBuilder shotBuilder = frame.getShotBuilder();
		
		ITrackedBall ball = wf.getBall();
		if (ball.isChipped())
		{
			// Chip Detected
			passingDetection.setState(EPassingDetectionState.CHIP);
		} else
		{
			if (getCountFrameSinceInit() > 5) // timeout waiting for chip kick
			{
				// waiting for chip timeout
				shotBuilder.updateEndOfPassCause(ShotBuilder.EndOfPassCause.UNKNOWN);
				passingDetection.setState(EPassingDetectionState.NO_PASS);
			}
		}
	}
}
