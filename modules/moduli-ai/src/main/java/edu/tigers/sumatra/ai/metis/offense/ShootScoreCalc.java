/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.kick.MovingRobotPassRater;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ShootScoreCalc extends ACalculator
{
	private final IColorPicker cp = ColorPickerFactory.greenRedGradient();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.UNSORTED);
		
		List<IVector2> points = goalPoints(20);
		getWFrame().getTigerBotsAvailable().values().forEach(tBot -> points.add(tBot.getBotKickerPos()));
		
		MovingRobotPassRater rater = new MovingRobotPassRater(getWFrame().getFoeBots().values());
		
		double passEndVel = 4;
		for (IVector2 point : points)
		{
			double kickSpeed = BallFactory.createStraightConsultant()
					.getInitVelForDist(getBall().getPos().distanceTo(point), passEndVel);
			double score = rater.rateLine(getBall().getPos(), point, kickSpeed);
			Color color = cp.getColor(score);
			shapes.add(new DrawableLine(Line.fromPoints(getBall().getPos(), point), color));
		}
	}
	
	
	private List<IVector2> goalPoints(int numGoalPoints)
	{
		List<IVector2> points = new ArrayList<>(numGoalPoints);
		double step = Geometry.getGoalOur().getWidth() / (numGoalPoints - 1);
		IVector2 point = Geometry.getGoalTheir().getRightPost();
		IVector2 dir = Vector2.fromY(step);
		for (int i = 0; i < numGoalPoints; i++)
		{
			points.add(point);
			point = point.addNew(dir);
		}
		return points;
	}
}
