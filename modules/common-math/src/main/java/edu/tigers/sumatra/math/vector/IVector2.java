/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import edu.tigers.sumatra.math.IEuclideanDistance;


/**
 * Interface for a 2-dimensional vector
 *
 * @author nicolai.ommer
 */
public interface IVector2 extends IVector, IEuclideanDistance
{
	@Override
	default int getNumDimensions()
	{
		return 2;
	}
	
	
	/**
	 * Adds 'this' and 'vector' and returns the result in a new object, while 'this' and 'vector' stay unaffected.
	 *
	 * @param vector
	 * @return The result as a new object
	 */
	Vector2 addNew(IVector2 vector);
	
	
	/**
	 * Subtracts the given 'vector' from 'this' and returns the result in a new object, while 'this' and 'vector' stay
	 * unaffected.
	 *
	 * @param vector
	 * @return The result as a new object
	 */
	Vector2 subtractNew(IVector2 vector);
	
	
	/**
	 * Multiplies each element of the given 'vector' with the corresponding element of 'this' and returns the result in a
	 * new object, while 'this' and 'vector' stay
	 * unaffected.
	 *
	 * @param vector
	 * @return The result as a new object
	 */
	Vector2 multiplyNew(IVector2 vector);
	
	
	@Override
	Vector2 multiplyNew(double f);
	
	
	@Override
	Vector2 normalizeNew();
	
	
	@Override
	Vector2 absNew();
	
	
	@Override
	Vector2 applyNew(Function<Double, Double> function);
	
	
	/**
	 * @param point some point
	 * @return the squared distance to the point
	 */
	double distanceToSqr(IVector2 point);
	
	
	/**
	 * @return a new deep copy
	 */
	IVector2 copy();
	
	
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
	 * @param newLength the new length of the vector
	 * @return the scaled vector
	 */
	Vector2 scaleToNew(double newLength);
	
	
	/**
	 * Turns 'this' with the given 'angle' and returns the result in a new object,
	 * while 'this' stay unaffected. The new vector has the same length as the old one.
	 * The angle is added anti-clockwise.
	 * 
	 * @param angle angle step to turn
	 * @return the turned angle
	 */
	Vector2 turnNew(double angle);
	
	
	/**
	 * Returns a new vector with the given absolute 'angle' with respect to the x-axis.
	 * Input and output vector are of equal length.
	 * 
	 * @param angle the new angle
	 * @return a vector with given angle and same length as current vector
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
	 * If you input a zero-vector, an exception is thrown.
	 *
	 * @return the angle of this vector [-PI..PI]
	 * @throws IllegalArgumentException if vector is a zero-vector
	 */
	double getAngle();
	
	
	/**
	 * Same as {@link IVector2#getAngle()}, but if vector has zero length, return
	 * the given default value.
	 * 
	 * @param defAngle the default angle value
	 * @return the angle of this vector
	 */
	double getAngle(double defAngle);
	
	
	/**
	 * Returns the scalar product of 'this' and the given vector.
	 * 
	 * @param v some vector
	 * @return this * v
	 */
	double scalarProduct(IVector2 v);
	
	
	/**
	 * Returns the surface normal to the given vector.
	 * It is turned by 90 degree clockwise.
	 * Returned vector is NOT normalized!
	 *
	 * @return the normal vector of this vector
	 */
	Vector2 getNormalVector();
	
	
	/**
	 * Check if this vector is parallel to the other giver vector.
	 * 
	 * @param vector some other vector
	 * @return true, if both vectors are parallel
	 */
	boolean isParallelTo(IVector2 vector);
	
	
	/**
	 * Angle difference between this toVector and the given one, so that:
	 * <br>
	 * angle(this) + angle = angle(toVector)
	 * <br>
	 * So the operation is: <br>
	 * angle(toVector) - angle(this)
	 *
	 * @param toVector towards this toVector
	 * @return angle difference from this toVector to the given one in [-PI, PI]
	 */
	Optional<Double> angleTo(IVector2 toVector);
	
	
	/**
	 * Absolute angle difference between this toVector and the given one.
	 *
	 * @see IVector2#angleTo(IVector2)
	 * @param toVector towards this toVector
	 * @return absolute angle difference from this toVector to the given one in [0, PI]
	 */
	Optional<Double> angleToAbs(IVector2 toVector);
	
	
	/**
	 * Calculate the nearest point in the given list to this vector.
	 *
	 * @param points list of points to compare
	 * @return nearest point in list to point p
	 */
	IVector2 nearestTo(Collection<IVector2> points);
	
	
	/**
	 * Calculate the nearest point in the given list to this vector.
	 * The list can be empty. An empty Optional will be returned in this case.
	 *
	 * @param points list of points to compare
	 * @return nearest point in list to point p, or empty if list is empty
	 */
	Optional<IVector2> nearestToOpt(Collection<IVector2> points);
	
	
	/**
	 * Calculate the nearest point in the given list to this vector.
	 *
	 * @param points list of points to compare
	 * @return nearest point in list to point p
	 */
	IVector2 nearestTo(IVector2... points);
	
	
	/**
	 * Calculate the farthest point in the given list to this vector.
	 *
	 * @param points list of points to compare
	 * @return farthest point in list to point p
	 */
	IVector2 farthestTo(Collection<IVector2> points);
	
	
	/**
	 * Calculate the farthest point in the given list to this vector.
	 * The list can be empty. An empty Optional will be returned in this case.
	 *
	 * @param points list of points to compare
	 * @return farthest point in list to point p, or empty if list is empty
	 */
	Optional<IVector2> farthestToOpt(Collection<IVector2> points);
	
	
	/**
	 * Calculate the farthest point in the given list to this vector.
	 *
	 * @param points list of points to compare
	 * @return farthest point in list to point p
	 */
	IVector2 farthestTo(IVector2... points);
}