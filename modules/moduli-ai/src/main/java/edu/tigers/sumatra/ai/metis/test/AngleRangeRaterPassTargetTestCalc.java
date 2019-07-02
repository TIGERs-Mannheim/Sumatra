/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.test;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Test calc for visualizing angle range rater for pass targets (a bot)
 */
public class AngleRangeRaterPassTargetTestCalc extends ACalculator
{
	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		
		BotID botID = BotID.createBotId(1, baseAiFrame.getTeamColor());
		
		ITrackedBot bot = baseAiFrame.getWorldFrame().getBot(botID);
		Goal goal = Geometry.getGoalTheir();
		double rangeDist = 1000;
		IVector2 p1 = LineMath.stepAlongLine(bot.getBotKickerPos(), goal.getCenter(), rangeDist);
		IVector2 p2 = LineMath.stepAlongLine(bot.getBotKickerPos(), goal.getCenter(), -rangeDist);
		IVector2 start = baseAiFrame.getWorldFrame().getBall().getPos();
		ILineSegment targetLine = Lines.segmentFromPoints(p1, p2);
		
		AngleRangeRater rater = AngleRangeRater.forLineSegment(targetLine);
		rater.setStraightBallConsultant(baseAiFrame.getWorldFrame().getBall().getStraightConsultant());
		rater.setObstacles(baseAiFrame.getWorldFrame().getBots().values());
		rater.setExcludedBots(Collections.singleton(botID));
		Optional<IRatedTarget> target = rater.rate(start);
		
		if (target.isPresent())
		{
			List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.TEST_ANGLE_RANGE_RATER);
			shapes.add(createDrawable(start, Lines.segmentFromPoints(p1, p2), target.get()));
		}
	}
	
	
	private DrawableTriangle createDrawable(IVector2 start, ILineSegment targetLine, IRatedTarget range)
	{
		double baseAngle = range.getTarget().getPos().subtractNew(start).getAngle();
		
		Color color = colorPicker.getColor(range.getScore());
		
		IVector2 interceptionWithGoalLeft = targetLine.intersectLine(
				Lines.lineFromDirection(start,
						Vector2.fromAngle(baseAngle - range.getRange() / 2)))
				.orElse(Vector2f.ZERO_VECTOR);
		IVector2 interceptionWithGoalRight = targetLine.intersectLine(
				Lines.lineFromDirection(start,
						Vector2.fromAngle(baseAngle + range.getRange() / 2)))
				.orElse(Vector2f.ZERO_VECTOR);
		
		DrawableTriangle triangle = new DrawableTriangle(Triangle
				.fromCorners(start, interceptionWithGoalLeft, interceptionWithGoalRight),
				color);
		triangle.setFill(true);
		
		return triangle;
	}
}
