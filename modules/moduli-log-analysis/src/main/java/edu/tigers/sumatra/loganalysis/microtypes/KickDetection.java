/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.microtypes;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.loganalysis.ELogAnalysisShapesLayer;
import edu.tigers.sumatra.loganalysis.GameMemory;
import edu.tigers.sumatra.loganalysis.eventtypes.TypeDetectionFrame;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;

import java.awt.Color;
import java.util.List;
import java.util.Optional;


public class KickDetection implements IMicroTypeDetection
{
	private KickDetectionState detectionState = KickDetectionState.NO_KICK;
	
	private double toleranceBotToBallKick = 100d;
	
	
	@Override
	public void nextFrameForDetection(final TypeDetectionFrame frame)
	{
		SimpleWorldFrame wf = frame.getWorldFrameWrapper().getSimpleWorldFrame();
		GameMemory memory = frame.getMemory();
		
		Optional<IKickEvent> kickEvent = wf.getKickEvent();
		ITrackedBall ball = wf.getBall();
		
		kickEvent.ifPresent(iKickEvent -> frame.getShapeMap().get(ELogAnalysisShapesLayer.PASSING).add(
				new DrawableCircle(Circle.createCircle(iKickEvent.getPosition(), toleranceBotToBallKick), Color.CYAN)));

		boolean newKickEvent = kickEvent.isPresent() &&
				(memory.getLastKickEvent() == null || (memory.getLastKickEvent() != null && !kickEquals(kickEvent.get(), memory.getLastKickEvent())));
		
		boolean ballCloseToKick = kickEvent.isPresent() &&
				ball.getPos().distanceTo(kickEvent.get().getPosition()) < toleranceBotToBallKick;
		
		if (newKickEvent)
		// Initial Event
		{
			if(ballCloseToKick)
			{
				detectionState = KickDetectionState.KICK;
			}
			else
			{
				tryToFindCloseBallToKickInBallHistory(frame, memory, kickEvent.get());
			}

			Color kickCircleColor = detectionState == KickDetectionState.KICK ? Color.ORANGE : Color.DARK_GRAY;

			frame.getShapeMap().get(ELogAnalysisShapesLayer.PASSING).add(
					new DrawableCircle(Circle.createCircle(ball.getPos(), toleranceBotToBallKick - 5), kickCircleColor));



			memory.updateLastKickEvent(kickEvent.get());
		}
		else
		{
			detectionState = KickDetectionState.NO_KICK;
		}
		
	}

	private void tryToFindCloseBallToKickInBallHistory(final TypeDetectionFrame frame, final GameMemory memory, final IKickEvent kickEvent)
	{
		List<ITrackedObject> ballHistory = memory.get(GameMemory.GameLogObject.BALL_LINE_DETECTION);

		for(ITrackedObject pastBall : ballHistory)
		{
			double pastBallToKicEventDistance = pastBall.getPos().distanceTo(kickEvent.getPosition());
			boolean pastBallCloseToKick = pastBallToKicEventDistance < toleranceBotToBallKick;

			frame.getShapeMap().get(ELogAnalysisShapesLayer.PASSING).add(new DrawablePoint(
					pastBall.getPos(), Color.MAGENTA));


			if (pastBallCloseToKick)
			{
				detectionState = KickDetectionState.KICK;
				//kick found
				return;
			}

			if (pastBall.getTimestamp() < kickEvent.getTimestamp())
			{
				//no kick found in history until kick event timestamp
				return;
			}
		}
	}

	/**
	 * decide whether two kick events k1, k2 are referring to the same actual kick
	 * @param k1
	 * @param k2
	 * @return decision
	 */
	private boolean kickEquals(IKickEvent k1, IKickEvent k2)
	{
		boolean samePos = k1.getPosition().distanceTo(k2.getPosition()) < 100d;
		boolean sameBot = k1.getKickingBot().equals(k2.getKickingBot());
		boolean sameTime = Math.abs(k1.getTimestamp() - k2.getTimestamp()) * 1e-9 < 0.1;
		return samePos && sameBot && sameTime;
	}
	
	
	public KickDetectionState getState()
	{
		return detectionState;
	}
	
	public enum KickDetectionState
	{
		KICK,
		NO_KICK
	}
}
