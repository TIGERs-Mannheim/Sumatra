/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.vis.EWpShapesLayer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Produce {@link ShapeMap}s with data from all cameras.
 */
public class CamFrameShapeMapProducer
{
	private final Map<Integer, ExtendedCamDetectionFrame> camFrames = new HashMap<>();


	public void reset()
	{
		camFrames.clear();
	}


	public void updateCamFrameShapes(final ExtendedCamDetectionFrame frame)
	{
		camFrames.put(frame.getCameraId(), frame);
		camFrames.values().removeIf(f -> Math.abs(frame.gettCapture() - f.gettCapture()) / 1e9 > 1);
	}


	public ShapeMap createShapeMap()
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		var mergedFrames = camFrames.values().stream()
				.sorted(Comparator.comparing(CamDetectionFrame::gettCapture))
				.toList();
		for (ExtendedCamDetectionFrame mergedCamFrame : mergedFrames)
		{
			for (CamRobot bot : mergedCamFrame.getRobotsBlue())
			{
				shapes.add(createDrawableShape(bot, Color.BLUE));
			}

			for (CamRobot bot : mergedCamFrame.getRobotsYellow())
			{
				shapes.add(createDrawableShape(bot, Color.YELLOW));
			}

			Color ballsColor = new Color(50, 100, 20);
			for (CamBall ball : mergedCamFrame.getBalls())
			{
				DrawableCircle ballCircle = new DrawableCircle(ball.getPos().getXYVector(), Geometry
						.getBallRadius(), ballsColor);
				ballCircle.setFill(true);
				shapes.add(ballCircle);
			}
		}

		if (!mergedFrames.isEmpty())
		{
			Color ballColor = new Color(50, 100, 200);
			double age = (mergedFrames.get(0).gettCapture() - mergedFrames.get(0).getBall().gettCapture()) / 1e9;
			double size = ((Geometry.getBallRadius() - 5) * (1 - Math.min(1, Math.max(0, age / 0.2)))) + 5;
			DrawableCircle ballCircle = new DrawableCircle(mergedFrames.get(0).getBall().getPos().getXYVector(),
					size, ballColor);
			ballCircle.setFill(true);
			shapes.add(ballCircle);
		}

		ShapeMap shapeMap = new ShapeMap();
		shapeMap.get(EWpShapesLayer.RAW_VISION).addAll(shapes);
		return shapeMap;
	}


	private DrawableBotShape createDrawableShape(final CamRobot bot, final Color color)
	{
		DrawableBotShape botShape = new DrawableBotShape(bot.getPos(), bot.getOrientation(),
				Geometry.getBotRadius(), 75);
		botShape.setFillColor(null);
		botShape.setBorderColor(color);
		botShape.setFontColor(Color.white);
		botShape.setId(String.valueOf(bot.getRobotID()));
		return botShape;
	}
}
