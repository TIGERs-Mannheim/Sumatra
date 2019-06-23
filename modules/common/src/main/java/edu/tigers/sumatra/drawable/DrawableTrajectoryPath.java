/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 16, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableTrajectoryPath implements IDrawableShape
{
	private Color						color			= Color.black;
	private final List<IVector2>	points		= new ArrayList<>();
	private double						precision	= 0.01;
	
	
	@SuppressWarnings("unused")
	private DrawableTrajectoryPath()
	{
	}
	
	
	/**
	 * @param trajXY
	 */
	public DrawableTrajectoryPath(final ITrajectory<? extends IVector> trajXY)
	{
		this(trajXY, Color.black);
	}
	
	
	/**
	 * @param trajXY
	 * @param color
	 */
	public DrawableTrajectoryPath(final ITrajectory<? extends IVector> trajXY, final Color color)
	{
		this.color = color;
		
		IVector2 vLast = null;
		
		double stepSize = 0.1;
		for (double t = 0; t < (trajXY.getTotalTime() - stepSize); t += stepSize)
		{
			IVector2 pos = trajXY.getPositionMM(t).getXYVector();
			IVector2 vel = trajXY.getVelocity(t).getXYVector();
			if ((vLast == null) || (!vLast.isZeroVector() && !vel.isZeroVector()
					&& (Math.abs(AngleMath.getShortestRotation(vLast.getAngle(), vel.getAngle())) > precision)))
			{
				points.add(pos);
				vLast = vel;
			}
		}
		points.add(trajXY.getPositionMM(trajXY.getTotalTime()).getXYVector());
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		g.setColor(color);
		
		final GeneralPath drawPath = new GeneralPath();
		IVector2 pLast = points.get(0);
		IVector2 posTrans = tool.transformToGuiCoordinates(pLast, invert);
		drawPath.moveTo(posTrans.x(), posTrans.y());
		
		for (int i = 1; i < points.size(); i++)
		{
			IVector2 pos = points.get(i);
			posTrans = tool.transformToGuiCoordinates(pos, invert);
			drawPath.lineTo(posTrans.x(), posTrans.y());
			
			if (GeoMath.distancePP(pLast, pos) > 0.2)
			{
				// g.drawString(String.format("%.1f", t), (float) posTrans.x() + 2, (float) posTrans.y() + 0.8f);
				pLast = pos;
			}
		}
		g.draw(drawPath);
	}
	
}
