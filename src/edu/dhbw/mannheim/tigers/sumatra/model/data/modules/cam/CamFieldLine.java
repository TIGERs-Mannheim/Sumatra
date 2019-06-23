/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;


/**
 * SSL vision field line
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CamFieldLine extends Line
{
	private final String	name;
	private final float	thickness;
	
	
	/**
	 * @param line
	 * @param name
	 * @param thickness
	 */
	public CamFieldLine(final ILine line, final String name, final float thickness)
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
	public final float getThickness()
	{
		return thickness;
	}
}
