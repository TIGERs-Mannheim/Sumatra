/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.*;

import com.sleepycat.persist.model.Persistent;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public abstract class ADrawable implements IDrawableShape
{
	private Color color = Color.black;
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		g.setColor(color);
	}
	
	
	@Override
	public void setColor(final Color color)
	{
		this.color = color;
	}
	
	
	protected Color getColor()
	{
		return color;
	}
}
