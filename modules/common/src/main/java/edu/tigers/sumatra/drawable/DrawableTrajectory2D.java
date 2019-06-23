/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.*;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.StubTrajectory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableTrajectory2D implements IDrawableShape
{
	private final ITrajectory<? extends IVector>	trajXY;
	private final float									colorBlue;
	
	
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
			
			g.setColor(new Color((float) colorRed, (float) colorGreen, colorBlue));
			
			g.fillOval((int) posTrans.x() - 1, (int) posTrans.y() - 1, 2, 2);
			
			if (VectorMath.distancePP(pLast, trajXY.getPosition(t).getXYVector()) > 0.2)
			{
				pLast = trajXY.getPosition(t).getXYVector();
			}
			
			t += 0.1;
		}
	}
	
}
