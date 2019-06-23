/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 23, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.shapes.circle.Arc;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.Goal;
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
				- Geometry.getPenaltyAreaOur().getRadiusOfPenaltyArea();
		double penAreaFrontLineHalf = Geometry.getPenaltyAreaTheir().getLengthOfPenaltyAreaFrontLineHalf();
		
		shapes.add(new DrawableRectangle(Geometry.getField(), Color.WHITE));
		shapes.add(new DrawableCircle(Geometry.getCenterCircle(), Color.WHITE));
		shapes.add(new DrawableLine(Line.newLine(new Vector2(0, -Geometry.getFieldWidth() / 2.0),
				new Vector2(0, Geometry.getFieldWidth() / 2.0)), Color.WHITE, false));
				
		shapes.add(new DrawableLine(Line.newLine(
				new Vector2(center2PenArea, -penAreaFrontLineHalf),
				new Vector2(center2PenArea, penAreaFrontLineHalf)),
				Color.WHITE, false));
		shapes.add(new DrawableLine(Line.newLine(
				new Vector2(-center2PenArea, -penAreaFrontLineHalf),
				new Vector2(-center2PenArea, penAreaFrontLineHalf)),
				Color.WHITE, false));
				
		shapes.add(new DrawableArc(getPenAreaArc(
				Geometry.getPenaltyAreaOur().getPenaltyCircleNegCentre(),
				Geometry.getPenaltyAreaOur().getRadiusOfPenaltyArea()), Color.white));
		shapes.add(new DrawableArc(getPenAreaArc(
				Geometry.getPenaltyAreaOur().getPenaltyCirclePosCentre(),
				Geometry.getPenaltyAreaOur().getRadiusOfPenaltyArea()), Color.white));
		shapes.add(new DrawableArc(getPenAreaArc(
				Geometry.getPenaltyAreaTheir().getPenaltyCircleNegCentre(),
				Geometry.getPenaltyAreaTheir().getRadiusOfPenaltyArea()), Color.white));
		shapes.add(new DrawableArc(getPenAreaArc(
				Geometry.getPenaltyAreaTheir().getPenaltyCirclePosCentre(),
				Geometry.getPenaltyAreaTheir().getRadiusOfPenaltyArea()), Color.white));
				
		Color ourColor = TeamConfig.getLeftTeam() == ETeamColor.BLUE ? Color.blue : Color.yellow;
		drawGoal(Geometry.getGoalOur(), shapes, ourColor);
		
		Color theirColor = TeamConfig.getLeftTeam() != ETeamColor.BLUE ? Color.blue : Color.yellow;
		drawGoal(Geometry.getGoalTheir(), shapes, theirColor);
	}
	
	
	private void drawGoal(final Goal goal, final List<IDrawableShape> shapes, final Color color)
	{
		IVector2 gpl = goal.getGoalPostLeft();
		double inv = -Math.signum(gpl.x());
		IVector2 gplb = gpl.addNew(new Vector2(-Geometry.getGoalDepth() * inv, 0));
		IVector2 gpr = goal.getGoalPostRight();
		IVector2 gprb = gpr.addNew(new Vector2(-Geometry.getGoalDepth() * inv, 0));
		shapes.add(new DrawableLine(Line.newLine(gpl, gplb), color, false));
		shapes.add(new DrawableLine(Line.newLine(gpr, gprb), color, false));
		shapes.add(new DrawableLine(Line.newLine(gplb, gprb), color, false));
	}
	
	
	private Arc getPenAreaArc(final IVector2 center, final double radius)
	{
		double startAngle = AVector2.X_AXIS.multiplyNew(-center.x()).getAngle();
		double stopAngle = AVector2.Y_AXIS.multiplyNew(center.y()).getAngle();
		double rotation = AngleMath.getShortestRotation(startAngle, stopAngle);
		return new Arc(center, radius, startAngle, rotation);
	}
}
