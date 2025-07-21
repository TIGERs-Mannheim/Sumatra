/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePlanarCurve;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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
					.add(new DrawableAnnotation(ball.getPos(), String.format("Invisible for:%n%.2fs", invisibleFor))
							.withCenterHorizontally(true)
							.withOffset(Vector2.fromY(90))
							.setColor(Color.orange));
		}

		for (IVector2 touch : ball.getTrajectory().getTouchdownLocations())
		{
			DrawableCircle land = new DrawableCircle(touch, 30, Color.GREEN);
			shapeMap.get(EWpShapesLayer.BALL_PREDICTION).add(land);
		}

		for (ILineSegment line : ball.getTrajectory().getTravelLinesInterceptableByRobot())
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
			var ballCurve = new DrawablePlanarCurve(wfw.getSimpleWorldFrame().getBall().getTrajectory().getPlanarCurve());
			ballCurve.setStrokeWidth(10);
			ballCurve.setColor(Color.PINK);
			shapeMap.get(EWpShapesLayer.BALL_PREDICTION).add(ballCurve);

		}

		ball.getTrajectory().getTravelLinesRolling().forEach(rollLine -> {
			if (rollLine.directionVector().getLength2() > 1)
			{
				DrawableLine roll = new DrawableLine(rollLine, Color.orange);
				roll.setStrokeWidth(5);
				shapeMap.get(EWpShapesLayer.BALL_PREDICTION).add(roll);
			}
		});

		wfw.getSimpleWorldFrame().getKickedBall().ifPresent(kickedBall -> shapeMap.get(EWpShapesLayer.BALL_PREDICTION)
				.add(new DrawableArrow(kickedBall.getKickPos(), kickedBall.getKickVel().getXYVector().multiplyNew(100))
						.setColor(Color.magenta)));

		createBallParameterShapes(shapeMap);
	}


	private void createBallParameterShapes(ShapeMap shapeMap)
	{
		BallParameters params = Geometry.getBallParameters();
		String chip = "Chip";
		List<String> textLines = new ArrayList<>();
		textLines.add("Straight");
		textLines.add("AccSlide: " + params.getAccSlide());
		textLines.add("AccRoll: " + params.getAccRoll());
		textLines.add("KSwitch: " + params.getKSwitch());
		textLines.add(chip);
		textLines.add("DampingXY 1. Hop: " + params.getChipDampingXYFirstHop());
		textLines.add("DampingXY n. Hops: " + params.getChipDampingXYOtherHops());
		textLines.add("DampingZ: " + params.getChipDampingZ());
		double posXStraight = 1.0;
		double posY = 8.0;
		for (int i = 0; i < textLines.size(); i++)
		{
			if (Objects.equals(textLines.get(i), chip))
			{
				posY = posY + 0.3;
			}
			DrawableBorderText sText = new DrawableBorderText(Vector2.fromXY(posXStraight, posY),
					textLines.get(i));
			shapeMap.get(EWpShapesLayer.BALL_MODELS)
					.add(sText.setColor(i < textLines.size() / 2 ? Color.CYAN : Color.ORANGE));
			posY = posY + 1.1;
		}
	}
}
