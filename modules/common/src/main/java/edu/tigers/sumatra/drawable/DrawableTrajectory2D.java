/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.StubTrajectory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableTrajectory2D implements IDrawableShape
{
	private final ITrajectory<? extends IVector> trajXY;
	private final float colorBlue;
	
	
	@SuppressWarnings("unused")
	private DrawableTrajectory2D()
	{
		trajXY = StubTrajectory.vector2Zero();
		colorBlue = 0;
	}
	
	
	/**
	 * @param trajXY
	 */
	public DrawableTrajectory2D(final ITrajectory<? extends IVector> trajXY)
	{
		this(trajXY, 0);
	}
	
	
	/**
	 * @param trajXY
	 * @param colorBlue
	 */
	public DrawableTrajectory2D(final ITrajectory<? extends IVector> trajXY, final double colorBlue)
	{
		this.trajXY = trajXY;
		this.colorBlue = (float) colorBlue;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		int dotSize = tool.scaleXLength(20);
		for (double t = 0; t < trajXY.getTotalTime(); t += 0.05)
		{
			IVector2 pos = trajXY.getPositionMM(t).getXYVector();
			IVector2 posTrans = tool.transformToGuiCoordinates(pos, invert);
			IVector2 vel = trajXY.getVelocity(t).getXYVector();
			
			double relVel = Math.min(1, vel.getLength2() / 2.0);
			double colorGreen = 1;
			double colorRed = 1;
			if (relVel > 0)
			{
				colorRed = 1 - relVel;
			}
			if (relVel < 0)
			{
				colorGreen = 1 + relVel;
			}
			
			g.setColor(new Color((float) colorRed, (float) colorGreen, colorBlue));
			
			g.fillOval((int) Math.round(posTrans.x() - dotSize / 2.0), (int) Math.round(posTrans.y() - dotSize / 2.0),
					dotSize, dotSize);
		}
	}
	
}
