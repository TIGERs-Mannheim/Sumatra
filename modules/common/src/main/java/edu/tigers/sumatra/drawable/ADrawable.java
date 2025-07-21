/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * Abstract base drawable class.
 */
public abstract class ADrawable implements IDrawableShape
{
	private Color color = Color.black;


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		g.setColor(color);
	}


	@Override
	public void paintBorder(Graphics2D g, int width, int height)
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
