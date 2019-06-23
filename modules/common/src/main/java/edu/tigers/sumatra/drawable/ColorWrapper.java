/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 25, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.io.Serializable;

import com.sleepycat.persist.model.Persistent;


/**
 * This wrapper class will wrap a {@link Color} object. This object will be persistable.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class ColorWrapper implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 952878454594587069L;
	private transient Color		color;
	private int						colorValue;
	private int						colorAlpha;
	
	
	@SuppressWarnings("unused")
	private ColorWrapper()
	{
		color = null;
	}
	
	
	/**
	 * @param color
	 */
	public ColorWrapper(final Color color)
	{
		setColor(color);
	}
	
	
	/**
	 * @return the color
	 */
	public final Color getColor()
	{
		if (color == null)
		{
			color = new Color(colorValue);
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), colorAlpha);
		}
		return color;
	}
	
	
	/**
	 * @param color the color to set
	 */
	public final void setColor(final Color color)
	{
		this.color = color;
		colorValue = color.getRGB();
		colorAlpha = color.getAlpha();
	}
}
