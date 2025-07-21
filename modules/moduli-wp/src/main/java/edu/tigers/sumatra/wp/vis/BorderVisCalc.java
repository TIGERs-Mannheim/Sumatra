/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableFieldBackground;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.DrawableShapeBoundary;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.awt.Color;
import java.util.List;
import java.util.stream.Stream;


/**
 * Generate field lines
 */
public class BorderVisCalc implements IWpCalc
{
	private static final double GOAL_BORDER_WIDTH_MM = 20;


	@Override
	public void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		List<IDrawableShape> shapes = shapeMap.get(EWpShapesLayer.FIELD_LINES_REGULAR);

		var widthHalf = Geometry.getFieldWidth() / 2.0;
		var lengthHalf = Geometry.getFieldLength() / 2.0;
		var lengthQuarter = Geometry.getFieldLength() / 4.0;

		shapeMap.get(EWpShapesLayer.FIELD_BACKGROUND)
				.add(new DrawableFieldBackground(Geometry.getField(), Geometry.getBoundaryWidth()));
		drawLine(shapes, new DrawableRectangle(Geometry.getField()));
		drawLine(shapes, new DrawableCircle(Geometry.getCenterCircle()));
		drawLine(shapes, new DrawableLine(Vector2.fromY(-widthHalf), Vector2.fromY(widthHalf)));
		drawLine(shapes, new DrawableShapeBoundary(Geometry.getPenaltyAreaOur()));
		drawLine(shapes, new DrawableShapeBoundary(Geometry.getPenaltyAreaTheir()));

		List<IDrawableShape> additionalShapes = shapeMap.get(EWpShapesLayer.FIELD_LINES_ADDITIONAL);
		drawLine(additionalShapes, new DrawableLine(Vector2.fromX(-lengthHalf), Vector2.fromX(lengthHalf)));
		drawLine(additionalShapes,
				new DrawableLine(Vector2.fromXY(-lengthQuarter, -widthHalf), Vector2.fromXY(-lengthQuarter, widthHalf)));
		drawLine(additionalShapes,
				new DrawableLine(Vector2.fromXY(lengthQuarter, -widthHalf), Vector2.fromXY(lengthQuarter, widthHalf)));


		Color ourColor = wfw.getRefereeMsg().getNegativeHalfTeam() == ETeamColor.BLUE ? Color.blue : Color.yellow;
		drawGoal(Geometry.getGoalOur(), shapes, ourColor);

		Color theirColor = wfw.getRefereeMsg().getNegativeHalfTeam() != ETeamColor.BLUE ? Color.blue : Color.yellow;
		drawGoal(Geometry.getGoalTheir(), shapes, theirColor);

		if (wfw.getGameState().isPenaltyOrPreparePenalty())
		{
			var markSize = 2 * Geometry.getBallRadius() + 20;
			shapes.add(new DrawablePoint(Geometry.getPenaltyMarkTheir()).withSize(markSize).setColor(Color.WHITE));
			shapes.add(new DrawablePoint(Geometry.getPenaltyMarkOur()).withSize(markSize).setColor(Color.WHITE));
		}
	}


	private void drawLine(List<IDrawableShape> list, IDrawableShape newShape)
	{
		list.add(newShape.setColor(Color.WHITE).setStrokeWidth(Geometry.getLineWidth() * 2));
	}


	private void drawGoal(Goal goal, List<IDrawableShape> shapes, Color color)
	{
		var sign = Math.signum(goal.getCenter().x());
		var x = Math.abs(goal.getCenter().x());
		var width = goal.getWidth();

		var positiveFront = Vector2.fromXY(x + Geometry.getLineWidth() / 2 + GOAL_BORDER_WIDTH_MM / 2,
				width / 2 + GOAL_BORDER_WIDTH_MM / 2).multiply(sign);
		var negativeFront = Vector2.fromXY(x + Geometry.getLineWidth() / 2 + GOAL_BORDER_WIDTH_MM / 2,
				-width / 2 - GOAL_BORDER_WIDTH_MM / 2).multiply(sign);

		var backDistance = SumatraMath.max(
				Geometry.getBoundaryWidth() - GOAL_BORDER_WIDTH_MM - Geometry.getLineWidth() / 2,
				goal.getDepth()
		);

		var backOffset = Vector2.fromX(backDistance).multiply(sign);
		var middleOffset = Vector2.fromX(goal.getDepth()).multiply(sign);

		var positiveBack = positiveFront.addNew(backOffset);
		var negativeBack = negativeFront.addNew(backOffset);

		var positiveMiddle = positiveFront.addNew(middleOffset);
		var negativeMiddle = negativeFront.addNew(middleOffset);

		Stream.of(
						new DrawableLine(positiveFront, positiveBack),
						new DrawableLine(negativeFront, negativeBack),
						new DrawableLine(positiveMiddle, negativeMiddle)
				)
				.map(shape -> shape.setColor(color).setStrokeWidth(GOAL_BORDER_WIDTH_MM))
				.forEach(shapes::add);
	}
}
