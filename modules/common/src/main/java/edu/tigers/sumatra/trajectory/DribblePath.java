/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.01.2016
 * Author(s): kisle
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawablePath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 */
@Persistent
public class DribblePath
{
	private final HermiteSplinePart2D	simplePath;
	private IVector2							target;
	private IVector2							offset	= new Vector2(0, 0);
																
																
	/**
	 * 
	 */
	public DribblePath()
	{
		simplePath = null;
	}
	
	
	/**
	 * @param path path
	 */
	public DribblePath(final HermiteSplinePart2D path)
	{
		simplePath = path;
	}
	
	
	/**
	 * @param t value t [0,1]
	 * @return Position to time t
	 */
	public IVector2 getPosition(final double t)
	{
		return simplePath.getPosition(t).addNew(offset);
	}
	
	
	/**
	 * @param iterations
	 * @param color
	 * @return drawablePath
	 */
	public DrawablePath getDrawablePath(final int iterations, final Color color)
	{
		List<IVector2> points = new ArrayList<IVector2>();
		for (int i = 0; i <= iterations; i++)
		{
			points.add(simplePath.value(i / ((double) iterations)).addNew(offset));
		}
		return new DrawablePath(points, color);
	}
	
	
	/**
	 * @return the target
	 */
	public IVector2 getTarget()
	{
		return target;
	}
	
	
	/**
	 * @param target the target to set
	 */
	public void setTarget(final IVector2 target)
	{
		this.target = target;
	}
	
	
	/**
	 * @return the offset
	 */
	public IVector2 getOffset()
	{
		return offset;
	}
	
	
	/**
	 * @param offset the offset to set
	 */
	public void setOffset(final IVector2 offset)
	{
		this.offset = offset;
	}
}
