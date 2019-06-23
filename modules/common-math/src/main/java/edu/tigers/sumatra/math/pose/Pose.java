/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.pose;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A pose consists of a position and an orientation.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Pose
{
	private final IVector2	pos;
	private final double		orientation;
	
	
	private Pose(final IVector2 pos, final double orientation)
	{
		this.pos = pos;
		this.orientation = orientation;
	}
	
	
	/**
	 * @param pos position of object
	 * @param orientation of object
	 * @return new pose
	 */
	public static Pose from(final IVector2 pos, final double orientation)
	{
		return new Pose(pos, orientation);
	}
	
	
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	public double getOrientation()
	{
		return orientation;
	}
}
