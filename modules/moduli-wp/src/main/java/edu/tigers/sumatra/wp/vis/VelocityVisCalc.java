/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Generate shapes with velocities of ball and bots
 */
public class VelocityVisCalc implements IWpCalc
{
	private DecimalFormat df = new DecimalFormat("0.0");
	
	
	@Override
	public void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		List<IDrawableShape> shapes = shapeMap.get(EWpShapesLayer.VELOCITY);
		
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
				final DrawableAnnotation annotation = new DrawableAnnotation(velLine.getEnd(),
						df.format(bot.getVel().getLength2()));
				annotation.setColor(Color.cyan);
				shapes.add(annotation);
			}
		}
	}
	
}
