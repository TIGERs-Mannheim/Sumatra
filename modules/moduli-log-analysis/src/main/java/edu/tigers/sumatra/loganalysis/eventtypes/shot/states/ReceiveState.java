/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot.states;

import edu.tigers.sumatra.loganalysis.eventtypes.shot.PassTypeDetectionFrame;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotDetection;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


public class ReceiveState extends APassingDetectionState
{
	private double toleranceAcc = 2d;
	
	
	public ReceiveState(final EPassingDetectionState stateId)
	{
		super(stateId);
	}
	
	
	@Override
	public void nextFrameForDetection(final PassTypeDetectionFrame frame)
	{
		SimpleWorldFrame wf = frame.getWorldFrameWrapper().getSimpleWorldFrame();
		ShotDetection passingDetection = frame.getPassingDetection();
		
		ITrackedBall ball = wf.getBall();
		double ballAccLen = ball.getAcc().getLength();
		
		if (ballAccLen < toleranceAcc) {
			passingDetection.setState(EPassingDetectionState.NO_PASS);
		}
	}
}
