/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 16, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.ITrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableTrajectory implements IDrawableShape
{
	private final ITrajectory2D	trajXY;
	private final float				colorBlue;
	
	
	@SuppressWarnings("unused")
	private DrawableTrajectory()
	{
		trajXY = null;
		colorBlue = 0;
	}
	
	
	/**
	 * @param trajXY
	 */
	public DrawableTrajectory(final ITrajectory2D trajXY)
	{
		this(trajXY, 0);
	}
	
	
	/**
	 * @param trajXY
	 * @param colorBlue
	 */
	public DrawableTrajectory(final ITrajectory2D trajXY, final float colorBlue)
	{
		this.trajXY = trajXY;
		this.colorBlue = colorBlue;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		float t = 0;
		while (t < trajXY.getTotalTime())
		{
			IVector2 pos = trajXY.getPosition(t);
			IVector2 posTrans = fieldPanel.transformToGuiCoordinates(pos, invert);
			IVector2 vel = trajXY.getVelocity(t);
			
			float relVel = Math.min(1, vel.getLength2() / 2f);
			float colorGreen = 1;
			float colorRed = 1;
			if (relVel > 0)
			{
				colorRed = 1 - relVel;
			}
			if (relVel < 0)
			{
				colorGreen = 1 + relVel;
			}
			
			g.setColor(new Color(colorRed, colorGreen, colorBlue));
			
			g.fillOval((int) posTrans.x() - 1, (int) posTrans.y() - 1, 2, 2);
			t += 0.1f;
		}
	}
	
}
