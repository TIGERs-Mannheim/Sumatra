/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import java.awt.*;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.ETeamColor;
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
	public void process(final WorldFrameWrapper wfw)
	{
		List<IDrawableShape> shapes = wfw.getShapeMap().get(EWpShapesLayer.FIELD_BORDERS);
		
		double center2PenArea = (Geometry.getFieldLength() / 2.0)
				- Geometry.getPenaltyAreaOur().getRadius();
		double penAreaFrontLineHalf = Geometry.getPenaltyAreaOur().getFrontLineHalfLength();
		
		shapes.add(new DrawableRectangle(Geometry.getField(), Color.WHITE));
		shapes.add(new DrawableCircle(Geometry.getCenterCircle(), Color.WHITE));
		shapes.add(new DrawableLine(Line.fromPoints(Vector2.fromXY(0, -Geometry.getFieldWidth() / 2.0),
				Vector2.fromXY(0, Geometry.getFieldWidth() / 2.0)), Color.WHITE));
				
		shapes.add(new DrawableLine(Line.fromPoints(
				Vector2.fromXY(center2PenArea, -penAreaFrontLineHalf),
				Vector2.fromXY(center2PenArea, penAreaFrontLineHalf)),
				Color.WHITE));
		shapes.add(new DrawableLine(Line.fromPoints(
				Vector2.fromXY(-center2PenArea, -penAreaFrontLineHalf),
				Vector2.fromXY(-center2PenArea, penAreaFrontLineHalf)),
				Color.WHITE));
				
		shapes.add(new DrawableArc(Geometry.getPenaltyAreaOur().getArcNeg(), Color.white));
		shapes.add(new DrawableArc(Geometry.getPenaltyAreaOur().getArcPos(), Color.white));
		shapes.add(new DrawableArc(Geometry.getPenaltyAreaTheir().getArcNeg(), Color.white));
		shapes.add(new DrawableArc(Geometry.getPenaltyAreaTheir().getArcPos(), Color.white));
				
		Color ourColor = wfw.getRefereeMsg().getLeftTeam() == ETeamColor.BLUE ? Color.blue : Color.yellow;
		drawGoal(Geometry.getGoalOur(), shapes, ourColor);
		
		Color theirColor = wfw.getRefereeMsg().getLeftTeam() != ETeamColor.BLUE ? Color.blue : Color.yellow;
		drawGoal(Geometry.getGoalTheir(), shapes, theirColor);
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
