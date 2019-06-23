/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BorderVisCalc implements IWpCalc
{
	
	
	@Override
	public void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		List<IDrawableShape> shapes = shapeMap.get(EWpShapesLayer.FIELD_BORDERS);
		
		shapes.add(new DrawableRectangle(Geometry.getField(), Color.WHITE));
		shapes.add(new DrawableCircle(Geometry.getCenterCircle(), Color.WHITE));
		shapes.add(new DrawableLine(Line.fromPoints(Vector2.fromXY(0, -Geometry.getFieldWidth() / 2.0),
				Vector2.fromXY(0, Geometry.getFieldWidth() / 2.0)), Color.WHITE));
		
		shapes.addAll(Geometry.getPenaltyAreaOur().getDrawableShapes());
		shapes.addAll(Geometry.getPenaltyAreaTheir().getDrawableShapes());
		
		Color ourColor = wfw.getRefereeMsg().getNegativeHalfTeam() == ETeamColor.BLUE ? Color.blue : Color.yellow;
		drawGoal(Geometry.getGoalOur(), shapes, ourColor);
		
		Color theirColor = wfw.getRefereeMsg().getNegativeHalfTeam() != ETeamColor.BLUE ? Color.blue : Color.yellow;
		drawGoal(Geometry.getGoalTheir(), shapes, theirColor);
		
		shapes.add(new DrawableCircle(Circle.createCircle(Geometry.getPenaltyMarkTheir(), Geometry.getBallRadius() + 10),
				Color.white).withFill(true));
		shapes.add(new DrawableCircle(Circle.createCircle(Geometry.getPenaltyMarkOur(), Geometry.getBallRadius() + 10),
				Color.white).withFill(true));
	}
	
	
	private void drawGoal(final Goal goal, final List<IDrawableShape> shapes, final Color color)
	{
		IVector2 gpl = goal.getLeftPost();
		double inv = -Math.signum(gpl.x());
		IVector2 gplb = gpl.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth() * inv, 0));
		IVector2 gpr = goal.getRightPost();
		IVector2 gprb = gpr.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth() * inv, 0));
		shapes.add(new DrawableLine(Line.fromPoints(gpl, gplb), color));
		shapes.add(new DrawableLine(Line.fromPoints(gpr, gprb), color));
		shapes.add(new DrawableLine(Line.fromPoints(gplb, gprb), color));
	}
}
