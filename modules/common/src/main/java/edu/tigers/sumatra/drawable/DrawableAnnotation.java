/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableAnnotation implements IDrawableShape
{
	private final IVector2	center;
	private final String		text;
									
	private double				margin	= 0;
	private ELocation			location	= ELocation.TOP;
	private Color				color		= Color.BLACK;
	private int					fontSize	= 10;
	private transient Font	font		= null;
												
	/**
	 * 
	 */
	public enum ELocation
	{
		/**  */
		TOP,
		/**  */
		BOTTOM,
		/**  */
		LEFT,
		/**  */
		RIGHT
	}
	
	
	@SuppressWarnings("unused")
	private DrawableAnnotation()
	{
		center = null;
		text = null;
	}
	
	
	/**
	 * @param center
	 * @param text
	 */
	public DrawableAnnotation(final IVector2 center, final String text)
	{
		super();
		this.center = center;
		this.text = text;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		if (font == null)
		{
			font = new Font("", Font.PLAIN, fontSize);
		}
		g.setFont(font);
		g.setColor(color);
		
		final IVector2 transPoint = tool.transformToGuiCoordinates(center, invert);
		
		String[] lines = text.split("\n");
		int numLines = lines.length;
		double maxWidth = 0;
		for (String line : lines)
		{
			maxWidth = Math.max(maxWidth, g.getFontMetrics(font).getStringBounds(line, g).getWidth());
		}
		double lineHeight = g.getFontMetrics(font).getHeight();
		double textHeight = lineHeight * numLines;
		
		double drawingX;
		double drawingY;
		switch (location)
		{
			case BOTTOM:
				drawingX = transPoint.x() - (maxWidth / 2.0);
				drawingY = transPoint.y() + tool.scaleYLength(margin);
				break;
			case LEFT:
				drawingX = transPoint.x() - maxWidth - tool.scaleXLength(margin);
				drawingY = transPoint.y() - (textHeight / 2.0) - (lineHeight / 2.0);
				break;
			case RIGHT:
				drawingX = transPoint.x() + tool.scaleXLength(margin);
				drawingY = transPoint.y() - (textHeight / 2.0) - (lineHeight / 2.0);
				break;
			case TOP:
				drawingX = transPoint.x() - (maxWidth / 2.0);
				drawingY = transPoint.y() - tool.scaleYLength(margin) - textHeight;
				break;
			default:
				return;
		}
		
		for (String txt : lines)
		{
			g.drawString(txt, (float) drawingX, (float) (drawingY += lineHeight));
		}
	}
	
	
	/**
	 * @return the margin
	 */
	public final double getMargin()
	{
		return margin;
	}
	
	
	/**
	 * @param margin the margin to set
	 */
	public final void setMargin(final double margin)
	{
		this.margin = margin;
	}
	
	
	/**
	 * @return the location
	 */
	public final ELocation getLocation()
	{
		return location;
	}
	
	
	/**
	 * @param location the location to set
	 */
	public final void setLocation(final ELocation location)
	{
		this.location = location;
	}
	
	
	/**
	 * @return the color
	 */
	public final Color getColor()
	{
		return color;
	}
	
	
	/**
	 * @param color the color to set
	 */
	@Override
	public final void setColor(final Color color)
	{
		this.color = color;
	}
	
	
	/**
	 * @return the fontSize
	 */
	public final int getFontSize()
	{
		return fontSize;
	}
	
	
	/**
	 * @param fontSize the fontSize to set
	 */
	public final void setFontSize(final int fontSize)
	{
		this.fontSize = fontSize;
	}
	
	
	/**
	 * @return the center
	 */
	public final IVector2 getCenter()
	{
		return center;
	}
	
	
	/**
	 * @return the text
	 */
	public final String getText()
	{
		return text;
	}
	
}
