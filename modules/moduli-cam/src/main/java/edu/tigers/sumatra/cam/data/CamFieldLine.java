/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;


/**
 * SSL vision field line
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CamFieldLine extends Line
{
	private final String	name;
	private final double	thickness;
	
	
	/**
	 * @param line
	 * @param name
	 * @param thickness
	 */
	public CamFieldLine(final ILine line, final String name, final double thickness)
	{
		super(line);
		this.name = name;
		this.thickness = thickness;
	}
	
	
	/**
	 * @return the name
	 */
	public final String getName()
	{
		return name;
	}
	
	
	/**
	 * @return the thickness
	 */
	public final double getThickness()
	{
		return thickness;
	}
}
