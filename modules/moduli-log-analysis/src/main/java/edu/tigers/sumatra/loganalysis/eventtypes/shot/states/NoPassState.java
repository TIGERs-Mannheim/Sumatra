/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot.states;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.loganalysis.ELogAnalysisShapesLayer;
import edu.tigers.sumatra.loganalysis.GameMemory;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.PassTypeDetectionFrame;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;

import java.awt.Color;

import static edu.tigers.sumatra.loganalysis.GameMemory.GameLogObject.BALL;


public class NoPassState extends APassingDetectionState
{
	public NoPassState(final EPassingDetectionState stateId)
	{
		super(stateId);
	}
	
	
	@Override
	protected void nextFrameForDetection(PassTypeDetectionFrame frame)
	{
		SimpleWorldFrame wf = frame.getWorldFrameWrapper().getSimpleWorldFrame();
		ITrackedBot nextBotToBall = frame.getNextBotToBall();
		GameMemory memory = frame.getMemory();
		
		ITrackedBall ball = wf.getBall();
		
		frame.getShapeMap().get(ELogAnalysisShapesLayer.PASSING).add(new DrawableCircle(Circle.createCircle(ball.getPos(),
				8), Color.blue));
		
		frame.getShapeMap().get(ELogAnalysisShapesLayer.PASSING)
				.add(new DrawableCircle(Circle.createCircle(nextBotToBall.getBotKickerPos(),
						8), Color.red));
		
		if (memory.get(BALL).isEmpty())
		{
			return;
		}
		
		checkInitKickEvent(frame);
	}
	
}
