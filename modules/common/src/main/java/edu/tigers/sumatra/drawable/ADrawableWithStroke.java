/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

import com.sleepycat.persist.model.Persistent;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public abstract class ADrawableWithStroke extends ADrawable
{
	private float					strokeWidth	= 10;
	private transient Stroke	stroke;
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);
		if (stroke == null)
		{
			stroke = new BasicStroke(tool.scaleXLength(strokeWidth));
		}
		g.setStroke(stroke);
	}
	
	
	@Override
	public void setStrokeWidth(final double strokeWidth)
	{
		this.strokeWidth = (float) strokeWidth;
	}
}
