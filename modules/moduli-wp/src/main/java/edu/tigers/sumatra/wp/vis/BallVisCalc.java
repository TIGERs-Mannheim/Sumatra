/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePlanarCurve;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.awt.Color;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallVisCalc implements IWpCalc
{
	private static final double FLYING_BALL_SCALING = 0.01;


	@Override
	public void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		ITrackedBall ball = wfw.getSimpleWorldFrame().getBall();

		double heightFactor = Math.abs(FLYING_BALL_SCALING * ball.getHeight()) + 1;

		ICircle ballCircle = Circle.createCircle(ball.getPos(), RuleConstraints.getStopRadius());
		IDrawableShape crossHair = AnimatedCrosshair.aCrosshairWithContinuousRotation(ballCircle, 1.5f, Color.RED);
		shapeMap.get(EWpShapesLayer.BALL_HIGHLIGHTER).add(crossHair);

		DrawableCircle point = new DrawableCircle(ball.getPos(), heightFactor * Geometry.getBallRadius(), Color.ORANGE);
		point.setFill(true);
		shapeMap.get(EWpShapesLayer.BALL).add(point);

		var invisibleFor = ball.invisibleFor();
		if (invisibleFor > 0.05)
		{
			shapeMap.get(EWpShapesLayer.BALL)
					.add(new DrawableAnnotation(ball.getPos(), String.format("Invisible for:\n%.2fs", invisibleFor))
							.withCenterHorizontally(true)
							.withOffset(Vector2.fromY(90))
							.setColor(Color.orange));
		}

		for (IVector2 touch : ball.getTrajectory().getTouchdownLocations())
		{
			DrawableCircle land = new DrawableCircle(touch, 30, Color.GREEN);
			shapeMap.get(EWpShapesLayer.BALL_PREDICTION).add(land);
		}

		for (ILineSegment line : ball.getTrajectory().getTravelLinesInterceptable())
		{
			if (line.directionVector().getLength2() > 1)
			{
				DrawableLine inter = new DrawableLine(line, Color.DARK_GRAY);
				inter.setStrokeWidth(15);
				shapeMap.get(EWpShapesLayer.BALL_PREDICTION).add(inter);
			}
		}

		if (wfw.getSimpleWorldFrame().getBall().getVel().getLength2() > 0.1)
		{
			var ballCurve = new DrawablePlanarCurve(wfw.getSimpleWorldFrame().getBall().getTrajectory().getPlanarCurve(),
					0.1, 0.02);
			ballCurve.setStrokeWidth(10);
			ballCurve.setColor(Color.PINK);
			shapeMap.get(EWpShapesLayer.BALL_PREDICTION).add(ballCurve);

			wfw.getSimpleWorldFrame().getKickFitState().ifPresent(state -> shapeMap.get(EWpShapesLayer.BALL_PREDICTION)
					.add(new DrawableArrow(state.getKickPos(), state.getKickVel().getXYVector().multiplyNew(100))
							.setColor(Color.magenta)));
		}

		ILineSegment rollLine = ball.getTrajectory().getTravelLineRolling();
		if (rollLine.directionVector().getLength2() > 1)
		{
			DrawableLine roll = new DrawableLine(rollLine, Color.orange);
			roll.setStrokeWidth(5);
			shapeMap.get(EWpShapesLayer.BALL_PREDICTION).add(roll);
		}

		wfw.getSimpleWorldFrame().getKickEvent().ifPresent(event -> shapeMap.get(EWpShapesLayer.BALL_PREDICTION)
				.add(new DrawablePoint(event.getPosition(), Color.red)
						.withSize(50)));
	}
}
