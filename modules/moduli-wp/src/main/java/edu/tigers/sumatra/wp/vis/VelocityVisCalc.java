/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import java.awt.*;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VelocityVisCalc implements IWpCalc
{
	
	
	@Override
	public void process(final WorldFrameWrapper wfw)
	{
		List<IDrawableShape> shapes = wfw.getShapeMap().get(EWpShapesLayer.VELOCITY);
		
		ITrackedBall ball = wfw.getSimpleWorldFrame().getBall();
		if (ball.getVel().getLength() > 0.1)
		{
			ILine ballVelLine = Line.fromDirection(ball.getPos(), ball.getVel().multiplyNew(1000));
			DrawableLine dBallVelLine = new DrawableLine(ballVelLine, Color.cyan);
			dBallVelLine.setStrokeWidth(20);
			shapes.add(dBallVelLine);
		}
		
		for (ITrackedBot bot : wfw.getSimpleWorldFrame().getBots().values())
		{
			if (bot.getVel().getLength2() > 0.1)
			{
				ILine velLine = Line.fromDirection(bot.getPos(), bot.getVel().multiplyNew(1000));
				DrawableLine line = new DrawableLine(velLine, Color.cyan);
				line.setStrokeWidth(20);
				shapes.add(line);
			}
		}
	}
	
}
