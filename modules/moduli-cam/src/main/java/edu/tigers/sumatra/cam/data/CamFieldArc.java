/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.circle.ICircle;


/**
 * TODO Nicolai Ommer <nicolai.ommer@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CamFieldArc extends Circle
{
	private final String	name;
	private final double	thickness;
	private final double	startAngle, endAngle;
	
	
	/**
	 * @param circle
	 * @param name
	 * @param thickness
	 * @param startAngle
	 * @param endAngle
	 */
	public CamFieldArc(final ICircle circle, final String name, final double thickness, final double startAngle,
			final double endAngle)
	{
		super(circle);
		this.name = name;
		this.thickness = thickness;
		this.startAngle = startAngle;
		this.endAngle = endAngle;
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
	
	
	/**
	 * @return the startAngle
	 */
	public final double getStartAngle()
	{
		return startAngle;
	}
	
	
	/**
	 * @return the endAngle
	 */
	public final double getEndAngle()
	{
		return endAngle;
	}
}
