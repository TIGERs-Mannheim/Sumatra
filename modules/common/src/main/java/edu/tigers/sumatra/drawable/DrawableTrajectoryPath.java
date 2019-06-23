/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableTrajectoryPath implements IDrawableShape
{
	private static final double	PRECISION	= 0.01;
	
	private Color						color			= Color.black;
	private final List<IVector2>	points		= new ArrayList<>();
	
	
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
			if ((vLast == null)
					|| (vel.angleToAbs(vLast).orElse(vel.getAngle(0)) > PRECISION))
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
			
			if (VectorMath.distancePP(pLast, pos) > 0.2)
			{
				pLast = pos;
			}
		}
		g.draw(drawPath);
	}
	
}
