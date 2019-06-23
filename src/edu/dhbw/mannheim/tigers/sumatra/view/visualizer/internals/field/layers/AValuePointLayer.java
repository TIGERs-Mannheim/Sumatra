/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 2, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Defines methods for handling value points in a layer
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AValuePointLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private final List<Color>	colors		= new ArrayList<Color>();
	protected static final int	POINT_SIZE	= 2;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param name
	 */
	public AValuePointLayer(final EFieldLayer name)
	{
		super(name, false);
		colors.add(new Color(0xF7181D));
		colors.add(new Color(0xF73E18));
		colors.add(new Color(0xF86A19));
		colors.add(new Color(0xF9951A));
		colors.add(new Color(0xF9C11B));
		colors.add(new Color(0xFAEC1B));
		colors.add(new Color(0xDEFB1C));
		colors.add(new Color(0xB4FB1D));
		colors.add(new Color(0x8AFC1E));
		colors.add(new Color(0x60FD1F));
		colors.add(new Color(0x37FE20));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	protected void drawValuePoints(final Graphics2D g, final List<ValuePoint> valuePoints, final boolean invert)
	{
		// --- draw goal points ---
		for (int i = valuePoints.size() - 1; i >= 0; i--)
		{
			ValuePoint point = valuePoints.get(i);
			float value = point.getValue();
			if (value > 1)
			{
				value = 1;
			} else if (value < 0)
			{
				value = 0;
			}
			// --- from SSLVision-mm to java2d-coordinates ---
			final IVector2 transPoint = getFieldPanel().transformToGuiCoordinates(point, invert);
			final int drawingX = (int) transPoint.x() - (POINT_SIZE / 2);
			final int drawingY = (int) transPoint.y() - (POINT_SIZE / 2);
			
			g.setColor(getColorByValue(value));
			// g.setColor(new Color((int) (255 * value), 255 - (int) (255 * value), 0));
			g.fillOval(drawingX, drawingY, POINT_SIZE, POINT_SIZE);
		}
	}
	
	
	private Color getColorByValue(final float value)
	{
		float step = 1f / colors.size();
		for (int i = 0; i < colors.size(); i++)
		{
			float val = (i + 1) * step;
			if (value <= val)
			{
				return colors.get(colors.size() - i - 1);
			}
		}
		return Color.black;
	}
	
	
	protected void drawValueLine(final Graphics2D g, final IVector2 origin, final IVector2 target,
			final boolean invert, float value)
	{
		if (value >= 0.9)
		{
			return;
		} else if (value < 0)
		{
			value = 0;
		}
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 transOrigin = getFieldPanel().transformToGuiCoordinates(origin, invert);
		final IVector2 transTarget = getFieldPanel().transformToGuiCoordinates(target, invert);
		
		g.setStroke(new BasicStroke());
		g.setColor(getColorByValue(value));
		// g.setColor(new Color((int) (255 * value), 255 - (int) (255 * value), 0));
		g.drawLine((int) transOrigin.x(), (int) transOrigin.y(), (int) transTarget.x(), (int) transTarget.y());
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
