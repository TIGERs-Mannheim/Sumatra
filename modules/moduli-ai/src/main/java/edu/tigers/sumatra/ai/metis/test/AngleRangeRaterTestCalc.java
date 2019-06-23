/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.test;

import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Test calculator for visualizing the result of {@link AngleRangeRater}
 */
public class AngleRangeRaterTestCalc extends ACalculator
{
	IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		AngleRangeRater rangeRater = AngleRangeRater
				.forLineSegment(Lines.segmentFromPoints(Vector2.fromY(-500), Vector2.fromY(500)));
		rangeRater.setStraightBallConsultant(baseAiFrame.getWorldFrame().getBall().getStraightConsultant());
		rangeRater.setObstacles(baseAiFrame.getWorldFrame().getBots().values());
		
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.TEST_ANGLE_RANGE_RATER);
		
		double minX = Geometry.getFieldLength() / 2;
		double minY = Geometry.getFieldWidth() / 2;
		double step = 100;
		for (double x = -minX; x < minX; x += step)
		{
			for (double y = -minY; y < minY; y += step)
			{
				IVector2 p = Vector2.fromXY(x, y);
				Optional<IRatedTarget> score = rangeRater.rate(p);
				DrawablePoint point = new DrawablePoint(p,
						colorPicker.getColor(score.map(IRatedTarget::getScore).orElse(0.0)));
				shapes.add(point);
			}
		}
	}
}
