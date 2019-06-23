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

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableTrajectory2D implements IDrawableShape
{
	private final ITrajectory<? extends IVector>	trajXY;
	private final double									colorBlue;
	
	
	@SuppressWarnings("unused")
	private DrawableTrajectory2D()
	{
		trajXY = null;
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
		this.colorBlue = colorBlue;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		double t = 0;
		IVector2 pLast = trajXY.getPosition(t).getXYVector();
		while (t < trajXY.getTotalTime())
		{
			t = Math.min(t, trajXY.getTotalTime());
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
			
			g.setColor(new Color((float) colorRed, (float) colorGreen, (float) colorBlue));
			
			g.fillOval((int) posTrans.x() - 1, (int) posTrans.y() - 1, 2, 2);
			
			if (GeoMath.distancePP(pLast, trajXY.getPosition(t).getXYVector()) > 0.2)
			{
				// g.drawString(String.format("%.1f", t), (float) posTrans.x() + 2, (float) posTrans.y() + 0.8f);
				pLast = trajXY.getPosition(t).getXYVector();
			}
			
			t += 0.1;
		}
	}
	
}
