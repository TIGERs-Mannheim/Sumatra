/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot.states;

import edu.tigers.sumatra.loganalysis.eventtypes.shot.PassTypeDetectionFrame;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotBuilder;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotDetection;
import edu.tigers.sumatra.loganalysis.microtypes.LineDetection;
import edu.tigers.sumatra.wp.data.ITrackedBall;


public class ChipState extends APassingDetectionState
{
	int chipCounter = 0;
	
	
	public ChipState(final EPassingDetectionState stateId)
	{
		super(stateId);
	}
	
	
	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		
		chipCounter = 0;
	}
	
	
	@Override
	protected void nextFrameForDetection(final PassTypeDetectionFrame frame)
	{
		ITrackedBall ball = frame.getWorldFrameWrapper().getSimpleWorldFrame().getBall();
		ShotBuilder shotBuilder = frame.getShotBuilder();
		LineDetection lineDetection = frame.getLineDetection();
		ShotDetection passingDetection = frame.getPassingDetection();
		
		shotBuilder.updateChipFlag(true);
		
		if (!ball.isChipped())
		{
			chipCounter++; // chipCounter for delay for detection line -> no jump
		}
		if (chipCounter > 5)
		{
			lineDetection.resetAndStart();
			passingDetection.setState(EPassingDetectionState.PASS);
		}
	}
}
