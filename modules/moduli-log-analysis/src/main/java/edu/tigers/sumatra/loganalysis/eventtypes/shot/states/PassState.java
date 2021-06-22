/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot.states;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.PassTypeDetectionFrame;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotBuilder;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotDetection;
import edu.tigers.sumatra.loganalysis.microtypes.LineDetection;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


public class PassState extends APassingDetectionState
{

	private double toleranceBotToBallKick = 160d; // 180 to big; not smaller than 150
	private double maxPassVel = 0.7d;


	public PassState(final EPassingDetectionState stateId)
	{
		super(stateId);
	}


	@Override
	public void nextFrameForDetection(PassTypeDetectionFrame frame)
	{
		SimpleWorldFrame wf = frame.getWorldFrameWrapper().getSimpleWorldFrame();
		ShotDetection passingDetection = frame.getPassingDetection();
		ITrackedBall ball = wf.getBall();
		ShotBuilder shotBuilder = frame.getShotBuilder();

		if (ball.getVel().getLength() < maxPassVel)
		{
			shotBuilder.updateEndOfPassCause(ShotBuilder.EndOfPassCause.BALL_TOO_SLOW);
			passingDetection.setState(EPassingDetectionState.NO_PASS);
		}


		// Checks if the pass is a chip kick
		if (checkChipKick(frame))
			return;

		// Checks if ball is kicked again while following the line of a previous pass
		if (checkInitKickEvent(frame))
			return;

		// checks results of line detection and handle mismatches
		checkLineDetection(frame);
	}


	private void checkLineDetection(PassTypeDetectionFrame frame)
	{
		SimpleWorldFrame wf = frame.getWorldFrameWrapper().getSimpleWorldFrame();
		ShotDetection passingDetection = frame.getPassingDetection();
		LineDetection lineDetection = frame.getLineDetection();
		ShotBuilder shotBuilder = frame.getShotBuilder();
		ITrackedBot nextBotToBall = frame.getClosestBotToBall();
		if (nextBotToBall == null)
		{
			return;
		}
		ITrackedBall ball = wf.getBall();

		switch (lineDetection.getDetectionState())
		{
			case INIT:
				lineDetection.resetAndStart();
				break;
			case LINE_TOTAL_MISMATCH:
			case LINE_RELATIVE_MISMATCH:
				onLineMismatch(passingDetection, lineDetection, shotBuilder, nextBotToBall, ball);
				break;
			case BALL_OUT_OF_FIELD:
				shotBuilder.updateEndOfPassCause(ShotBuilder.EndOfPassCause.BALL_OUT_OF_FIELD);
				passingDetection.setState(EPassingDetectionState.NO_PASS);
				break;
			default:
				break;
		}
	}

	private void onLineMismatch(final ShotDetection passingDetection, final LineDetection lineDetection, final ShotBuilder shotBuilder, final ITrackedBot nextBotToBall, final ITrackedBall ball) {

		boolean isChipKick = shotBuilder.isChipKick();
		boolean noBotNextToBall = nextBotToBall.getPos().distanceTo(ball.getPos()) > toleranceBotToBallKick + Geometry.getBallRadius() + Geometry.getBotRadius();

		if (isChipKick && noBotNextToBall)
		{
			// ball pos jump from chip kick caused line mismatch
			lineDetection.ignoreMismatchAndContinueTracking();
		}
		else if (noBotNextToBall)
		{
			// Line mismatch and no bot object -> probably a chip following
			passingDetection.setState(EPassingDetectionState.WAITING_CHIP);
		}
		else
		{
			// Line mismatch and bot object
			shotBuilder.updateEndOfPassCause(ShotBuilder.EndOfPassCause.INTERCEPT_BY_BOT);
			passingDetection.setState(EPassingDetectionState.NO_PASS);
		}

	}
}
