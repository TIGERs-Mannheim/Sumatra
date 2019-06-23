/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 25, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes;

import java.awt.Color;
import java.io.Serializable;

import com.sleepycat.persist.model.Persistent;


/**
 * This wrapper class will wrap a {@link Color} object. This object will be persistable.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Persistent
public class ColorWrapper implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 952878454594587069L;
	private transient Color		color;
	private int						colorValue;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private ColorWrapper()
	{
		color = null;
	}
	
	
	/**
	 * @param color
	 */
	public ColorWrapper(Color color)
	{
		setColor(color);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the color
	 */
	public final Color getColor()
	{
		if (color == null)
		{
			color = new Color(colorValue);
		}
		return color;
	}
	
	
	/**
	 * @param color the color to set
	 */
	public final void setColor(Color color)
	{
		this.color = color;
		colorValue = color.getRGB();
	}
}
