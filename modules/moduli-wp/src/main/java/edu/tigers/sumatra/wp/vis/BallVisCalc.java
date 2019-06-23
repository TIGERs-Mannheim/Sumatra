/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallVisCalc implements IWpCalc
{
	private static final double FLYING_BALL_SCALING = 0.01;
	
	
	@Override
	public void process(final WorldFrameWrapper wfw)
	{
		List<IDrawableShape> shapes = wfw.getShapeMap().get(EWpShapesLayer.BALL);
		
		ITrackedBall ball = wfw.getSimpleWorldFrame().getBall();
		
		double heightFactor = Math.abs(FLYING_BALL_SCALING * ball.getHeight()) + 1;
		
		ICircle ballCircle = Circle.createCircle(ball.getPos(), 20 * Geometry.getBallRadius());
		IDrawableShape crossHair = AnimatedCrosshair.aCrosshairWithContinuousRotation(ballCircle, 1.5f, Color.RED);
		shapes.add(crossHair);
		
		DrawableCircle point = new DrawableCircle(ball.getPos(), heightFactor * Geometry.getBallRadius(), Color.ORANGE);
		point.setFill(true);
		shapes.add(point);
		
		for (IVector2 touch : ball.getTrajectory().getTouchdownLocations())
		{
			DrawableCircle land = new DrawableCircle(touch, 30, Color.GREEN);
			shapes.add(land);
		}
		
		for (ILine line : ball.getTrajectory().getTravelLinesInterceptable())
		{
			if (line.directionVector().getLength2() > 1)
			{
				DrawableLine inter = new DrawableLine(line, Color.DARK_GRAY);
				inter.setStrokeWidth(15);
				shapes.add(inter);
			}
		}
		
		ILine rollLine = ball.getTrajectory().getTravelLineRolling();
		if (rollLine.directionVector().getLength2() > 1)
		{
			DrawableLine roll = new DrawableLine(rollLine, Color.orange);
			roll.setStrokeWidth(5);
			shapes.add(roll);
		}
	}
}
