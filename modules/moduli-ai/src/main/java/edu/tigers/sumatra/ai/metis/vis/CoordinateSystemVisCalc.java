/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 6, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.vis;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableLine.ETextLocation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CoordinateSystemVisCalc extends ACalculator
{
	private static final int	STEP_SIZE			= 1000;
	private static final int	STEP_MARKER_LEN	= 100;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame frame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EShapesLayer.COORDINATE_SYSTEM);
		
		Color color = frame.getTeamColor() == ETeamColor.YELLOW ? Color.yellow : Color.blue;
		
		double maxY = Geometry.getFieldWidth() / 2.0;
		double maxX = Geometry.getFieldLength() / 2.0;
		
		DrawableLine xAxis = new DrawableLine(new Line(new Vector2(0, 0), new Vector2(maxX, 0)), color);
		xAxis.setText("x");
		xAxis.setTextLocation(ETextLocation.HEAD);
		shapes.add(xAxis);
		
		DrawableLine yAxis = new DrawableLine(new Line(new Vector2(0, 0), new Vector2(0, maxY)), color);
		yAxis.setText("y");
		yAxis.setTextLocation(ETextLocation.HEAD);
		shapes.add(yAxis);
		
		for (double step = STEP_SIZE; step < ((Geometry.getFieldLength() / 2.0) - (STEP_SIZE / 2.0)); step += STEP_SIZE)
		{
			DrawableLine xStep1 = new DrawableLine(new Line(new Vector2(step, (STEP_MARKER_LEN) / 2.0), new Vector2(0,
					-STEP_MARKER_LEN)), color);
			xStep1.setText(String.valueOf(step));
			xStep1.setTextLocation(ETextLocation.HEAD);
			xStep1.setDrawArrowHead(false);
			shapes.add(xStep1);
		}
		for (double step = STEP_SIZE; step < ((Geometry.getFieldWidth() / 2.0) - (STEP_SIZE / 2.0)); step += STEP_SIZE)
		{
			DrawableLine yStep1 = new DrawableLine(new Line(new Vector2((STEP_MARKER_LEN) / 2, step), new Vector2(
					-STEP_MARKER_LEN, 0)), color);
			yStep1.setText(String.valueOf(step));
			yStep1.setTextLocation(ETextLocation.HEAD);
			yStep1.setDrawArrowHead(false);
			shapes.add(yStep1);
		}
	}
	
}
