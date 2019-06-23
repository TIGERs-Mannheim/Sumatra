/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.SplineGenerator;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * Draws any ISpline
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableSpline implements IDrawableShape
{
	private final ISpline	spline;
	private final float		colorBlue;
	
	
	@SuppressWarnings("unused")
	private DrawableSpline()
	{
		this(null, 0);
	}
	
	
	/**
	 * @param spline
	 * @param colorBlue [0..1] blue color value, def is 0
	 */
	public DrawableSpline(final ISpline spline, final float colorBlue)
	{
		this.spline = spline;
		this.colorBlue = colorBlue;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		if (spline != null)
		{
			for (float t = 0; t < spline.getTotalTime(); t += 0.020)
			{
				if (t > 10)
				{
					// stop drawing here, because this would slow down Sumatra extensivly
					break;
				}
				IVector2 pointToDraw = new Vector2(spline.getPositionByTime(t).getXYVector());
				IVector3 vel = spline.getVelocityByTime(t);
				IVector3 acc = spline.getAccelerationByTime(t);
				
				float curvature = acc.getLength2();
				if (!vel.getXYVector().isZeroVector()
						&& !acc.getXYVector().isZeroVector()
						&& (Math.abs(AngleMath.difference(vel.getXYVector().getAngle(), acc.getXYVector().getAngle())) > AngleMath.PI_HALF))
				{
					curvature *= -1;
				}
				// -1..1
				curvature = Math.max(-1, Math.min(1, curvature / SplineGenerator.getMaxLinearAcceleration()));
				
				float colorGreen = 1;
				float colorRed = 1;
				if (curvature > 0)
				{
					colorRed = 1 - curvature;
				}
				if (curvature < 0)
				{
					colorGreen = 1 + curvature;
				}
				
				g.setColor(new Color(colorRed, colorGreen, colorBlue));
				// flip x and y since field is vertically drawn
				final IVector2 pointToDrawGUI = fieldPanel.transformToGuiCoordinates(pointToDraw,
						invert);
				g.fillOval((int) pointToDrawGUI.x() - 1, (int) pointToDrawGUI.y() - 1, 2, 2);
			}
			
			IVector2 curPoint = spline.getPositionByTime(spline.getCurrentTime()).getXYVector();
			IVector2 curPointGui = fieldPanel.transformToGuiCoordinates(curPoint, invert);
			g.setColor(Color.magenta);
			g.fillOval((int) curPointGui.x() - 1, (int) curPointGui.y() - 1, 2, 2);
			
		}
	}
	
}
