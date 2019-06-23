/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;


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
	private final float	thickness;
	private final float	startAngle, endAngle;
	
	
	/**
	 * @param circle
	 * @param name
	 * @param thickness
	 * @param startAngle
	 * @param endAngle
	 */
	public CamFieldArc(final ICircle circle, final String name, final float thickness, final float startAngle,
			final float endAngle)
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
	public final float getThickness()
	{
		return thickness;
	}
	
	
	/**
	 * @return the startAngle
	 */
	public final float getStartAngle()
	{
		return startAngle;
	}
	
	
	/**
	 * @return the endAngle
	 */
	public final float getEndAngle()
	{
		return endAngle;
	}
}
