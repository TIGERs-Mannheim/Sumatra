/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictionInformation;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class SimpleTimeAwareRobotObstacle implements IObstacle
{
	private final FieldPredictionInformation	predictionInfo;
	private final float								radius;
	private float										maxPredictionTime	= 1;
	
	
	@SuppressWarnings("unused")
	private SimpleTimeAwareRobotObstacle()
	{
		predictionInfo = null;
		radius = 0;
	}
	
	
	/**
	 * @param predictionInfo
	 * @param radius
	 */
	public SimpleTimeAwareRobotObstacle(final FieldPredictionInformation predictionInfo, final float radius)
	{
		super();
		this.predictionInfo = predictionInfo;
		this.radius = radius;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final float t)
	{
		IVector2 pos = predictionInfo.getPosAt(Math.min(t, maxPredictionTime));
		ICircle circle = new Circle(pos, radius);
		return circle.isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final float t)
	{
		IVector2 pos = predictionInfo.getPosAt(Math.min(t, maxPredictionTime));
		ICircle circle = new Circle(pos, radius + 20);
		return circle.nearestPointOutside(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		IVector2 pos = predictionInfo.getPosAt(0);
		Circle circle = new Circle(pos, radius);
		circle.generateObstacleAvoidancePoints(curBotPos, rnd, subPoints);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		final float guiRadius = fieldPanel.scaleXLength(radius);
		
		IVector2 lastPos = null;
		for (float t = 0; t < 1; t += 0.2f)
		{
			IVector2 botPos = predictionInfo.getPosAt(t);
			if ((lastPos != null) && lastPos.equals(botPos, 1))
			{
				break;
			}
			lastPos = botPos;
			final IVector2 center = fieldPanel.transformToGuiCoordinates(botPos, invert);
			
			g.setColor(Color.RED);
			g.setStroke(new BasicStroke(1));
			g.drawOval((int) (center.x() - guiRadius), (int) (center.y() - guiRadius), (int) guiRadius * 2,
					(int) guiRadius * 2);
			if (t > 0)
			{
				g.drawString(String.format("%.1f", t), (center.x() - guiRadius), (center.y() - guiRadius));
			}
		}
	}
	
	
	/**
	 * @return the maxPredictionTime
	 */
	public final float getMaxPredictionTime()
	{
		return maxPredictionTime;
	}
	
	
	/**
	 * @param maxPredictionTime the maxPredictionTime to set
	 */
	public final void setMaxPredictionTime(final float maxPredictionTime)
	{
		this.maxPredictionTime = maxPredictionTime;
	}
}
