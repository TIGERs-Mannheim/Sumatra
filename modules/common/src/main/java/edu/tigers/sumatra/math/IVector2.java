/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.util.function.Function;


/**
 * This interface allows transparent access to {@link Vector2} and {@link Vector2f}
 * 
 * @see Vector2
 * @see Vector2f
 * @author Gero
 */
public interface IVector2 extends IVector
{
	@Override
	default int getNumDimensions()
	{
		return 2;
	}
	
	
	@Override
	Vector2 addNew(IVector vector);
	
	
	@Override
	Vector2 subtractNew(IVector vector);
	
	
	@Override
	Vector2 multiplyNew(IVector vector);
	
	
	@Override
	Vector2 multiplyNew(double f);
	
	
	@Override
	Vector2 normalizeNew();
	
	
	@Override
	Vector2 absNew();
	
	
	@Override
	Vector2 applyNew(Function<Double, Double> function);
	
	
	/**
	 * Scales the length of 'this' to the given 'length', and returns the result in a new object, while 'this' stays
	 * uneffected.<br>
	 * 
	 * <pre>
	 * l
	 * ---- * v
	 * | v |
	 * 
	 * <pre>
	 * 
	 * @param newLength
	 * @author Malte
	 * @return
	 */
	Vector2 scaleToNew(double newLength);
	
	
	/**
	 * Turns 'this' with the given 'angle' and returns the result in a new object,
	 * while 'this' stay unaffected. The new vector has the same length as the old one.
	 * The angle is added anti-clockwise.
	 * 
	 * @param angle
	 * @author Malte
	 * @return
	 */
	Vector2 turnNew(double angle);
	
	
	/**
	 * Returns a new vector with the given 'angle'.
	 * Input and output vector are of equal length.
	 * 
	 * @param angle
	 * @author Malte
	 * @return
	 * @see Vector2#turn(double) turn
	 */
	Vector2 turnToNew(double angle);
	
	
	/**
	 * @return
	 */
	boolean isVertical();
	
	
	/**
	 * @return
	 */
	boolean isHorizontal();
	
	
	/**
	 * Calculates the angle between x-Axis and the given vector.<br>
	 * Angle is calculated anti-clockwise in range (-pi to pi).
	 * If you want to know how the angle is added, look here:
	 * <a href="http://tigers-mannheim.de/trac/wiki/Informatik#Spiefeld">Field.</a>
	 * If you input a zero-vector, an exeption is thrown.
	 * 
	 * @author Malte
	 * @return
	 * @throws IllegalArgumentException if vector is a zero-vector
	 */
	double getAngle();
	
	
	/**
	 * Same as {@link IVector2#getAngle()}, but if vector has zero length, return
	 * the given default value.
	 * 
	 * @param defAngle
	 * @return
	 */
	double getAngle(double defAngle);
	
	
	/**
	 * Returns the scalar product of 'this' and the given vector. (this * v)
	 * 
	 * @param v
	 * @return this * v
	 * @author Malte
	 */
	double scalarProduct(IVector2 v);
	
	
	/**
	 * Returns the surface normal to the given vector.
	 * It is turned by 90� clockwise. Returned vector is
	 * NOT normalized !
	 * 
	 * @author Malte
	 * @return
	 */
	Vector2 getNormalVector();
	
	
	/**
	 * rotates a new created Vector2 around axis and returns the new object.
	 * 
	 * @param axis
	 * @param angle
	 * @return
	 */
	Vector2 turnAroundNew(final IVector2 axis, final double angle);
}