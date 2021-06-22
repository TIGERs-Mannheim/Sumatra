/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;


/**
 * Abstract base drawable class.
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
	public ADrawable setColor(final Color color)
	{
		this.color = color;
		return this;
	}


	protected Color getColor()
	{
		return color;
	}
}
