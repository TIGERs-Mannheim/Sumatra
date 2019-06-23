package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - field raster generator
 * Date: 11.08.2010
 * Authors:
 * Oliver Steinbrecher
 * *********************************************************
 */

import java.util.HashSet;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;


/**
 * 
 * This class represents a rectangle. It is used to describe a part-rectangle of the field.
 * The reference point is used for rectangle positioning on the field. This extends {@link Rectangle} with a explicit
 * identifier.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class AIRectangle extends Rectangle implements Comparable<AIRectangle>, IValueObject
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long	serialVersionUID	= -17346148584353658L;
	
	/** stores the id of a rectangle or ids when it is a combine-rectangle */
	private final Set<Integer>	rectangleID			= new HashSet<Integer>();
	
	private float					value;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	
	public AIRectangle(int rectangleID, Vector2 referencePoint, int rectangleLength, int rectangleWidth)
	{
		super(referencePoint, rectangleLength, rectangleWidth);
		
		this.rectangleID.add(rectangleID);
	}
	

	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public Set<Integer> getRectangleID()
	{
		return rectangleID;
	}
	
	@Override
	public float getValue()
	{
		return value;
	}
	
	@Override
	public void setValue(float value)
	{
		this.value = value;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("Rectangle ID" + rectangleID.toString() + "(");
		builder.append(+xExtend());
		builder.append("/");
		builder.append(+yExtend());
		builder.append(")");
		builder.append("ref" + topLeft().toString());
		return builder.toString();
	}
	

	/**
	 * TODO OliS: Covariant versions of "equals(Object)" imply implementations of equals(Object) itself and thus hashcode
	 * (Gero)
	 * Function is used to check if two rectangles are equal. First compare the length and width and then the content of
	 * idSet.
	 * @param rectangle
	 * @return true when equal.
	 */
	@Override
	public boolean equals(AIRectangle rectangle)
	{
		if (xExtend() == rectangle.xExtend() && yExtend() == rectangle.yExtend() && topLeft().equals(rectangle.topLeft()))
		{
			if (rectangleID.equals(rectangle.getRectangleID()))
			{
				return true;
			}
		}
		return false;
	}
	

	/**
	 * TODO OliS: If compare is overwritten, equals (and thus hashcode) should be implemented, or write a comment (See
	 * {@link Comparable#compareTo(Object)}) (Gero)
	 */
	@Override
	public int compareTo(AIRectangle o)
	{
		if (o.getValue() >= this.value)
		{
			return 1;
		}
		
		return -1;
	}
	
}