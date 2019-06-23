/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.generic;


import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * Vector subclass which carries a timestamp corresponding to the time the vector was captured
 * 
 * @author "Lukas Magel"
 */
public class TimedPosition
{
	private final long		timestamp;
	private final IVector2	position;
	private final IVector3 pos3D;
	
	
	/**
	 * default constructor
	 */
	public TimedPosition()
	{
		timestamp = 0;
		position = Vector2.ZERO_VECTOR;
		pos3D = Vector3.zero();
	}
	
	
	/**
	 * @param timestamp
	 * @param position
	 */
	public TimedPosition(final long timestamp, final IVector2 position)
	{
		this.position = position;
		this.timestamp = timestamp;
		pos3D = position.getXYZVector();
	}
	
	
	public TimedPosition(final long timestamp, final IVector3 position)
	{
		this.timestamp = timestamp;
		this.pos3D = position;
		this.position = position.getXYVector();
	}
	
	
	/**
	 * @return the ts
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	public IVector2 getPos()
	{
		return position;
	}
	
	
	public IVector3 getPos3D()
	{
		return pos3D;
	}
}
