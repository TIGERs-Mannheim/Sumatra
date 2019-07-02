/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot.states;

import edu.tigers.sumatra.loganalysis.eventtypes.shot.PassTypeDetectionFrame;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotDetection;
import edu.tigers.sumatra.loganalysis.microtypes.LineDetection;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


public class PassDetectionPhaseState extends APassingDetectionState
{
	
	private int countDetectionFrames = 0;
	
	private double ballLeavesBotDistance = 100d;
	private int framesOnKickDetectionPhase = 12;
	
	
	public PassDetectionPhaseState(final EPassingDetectionState stateId)
	{
		super(stateId);
	}
	
	
	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		countDetectionFrames = 0;
	}
	
	
	@Override
	protected void nextFrameForDetection(final PassTypeDetectionFrame frame)
	{
		SimpleWorldFrame wf = frame.getWorldFrameWrapper().getSimpleWorldFrame();
		ShotDetection passingDetection = frame.getPassingDetection();
		LineDetection lineDetection = frame.getLineDetection();
		
		ITrackedBall ball = wf.getBall();

		if (ball.isChipped())
		{
			// Chip Detected
			passingDetection.setState(EPassingDetectionState.CHIP);
			return;
		}

		double distanceBallToPassStart = frame.getShotBuilder().getStartPassPos().distanceTo(ball.getPos());
		
		// Ball leaves Bot
		boolean ballLeftBot = distanceBallToPassStart > ballLeavesBotDistance;
		
		if (countDetectionFrames == 0)
		{
			lineDetection.resetAndStart();
		}
		
		switch (lineDetection.getDetectionState())
		{
			case INIT:
				lineDetection.resetAndStart();
				break;
			case LINE:
				if (countDetectionFrames >= framesOnKickDetectionPhase && ballLeftBot)
				{
					// Pass detected
					passingDetection.setState(EPassingDetectionState.PASS);
				}
				break;
			case LINE_TOTAL_MISMATCH:
			case LINE_RELATIVE_MISMATCH:
			case BALL_OUT_OF_FIELD:
				passingDetection.setState(EPassingDetectionState.NO_PASS);
				break;
			
			default:
				break;
		}
		
		countDetectionFrames++;
	}
}
