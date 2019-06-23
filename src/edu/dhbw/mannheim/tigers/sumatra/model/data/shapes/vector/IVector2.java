/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector;

import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.IJsonString;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.INumberListable;


/**
 * This interface allows transparent access to {@link Vector2} and {@link Vector2f}
 * 
 * @see Vector2
 * @see Vector2f
 * @author Gero
 */
public interface IVector2 extends IJsonString, INumberListable
{
	/**
	 * @return The X part of the vector
	 */
	float x();
	
	
	/**
	 * @return The Y part of the vector
	 */
	float y();
	
	
	/**
	 * Returns the normalized Vector. 'this' stays unaffected!!!
	 * 
	 * @author Malte
	 * @return
	 */
	Vector2 normalizeNew();
	
	
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
	 * Multiplies 'this' by the given 'factor' and returns the result in a new object, while 'this' stays unaffected
	 * 
	 * @param factor
	 * @return
	 */
	Vector2 multiplyNew(float factor);
	
	
	/**
	 * Scales the length of 'this' to the given 'length', and returns the result in a new object, while 'this' stays
	 * uneffected.<br>
	 * 
	 * <pre>
	 *   l
	 * ----  *   v
	 * | v |
	 * 
	 * <pre>
	 * 
	 * @param newLength
	 * @author Malte
	 * @return
	 */
	Vector2 scaleToNew(float newLength);
	
	
	/**
	 * Turns 'this' with the given 'angle' and returns the result in a new object,
	 * while 'this' stay unaffected. The new vector has the same length as the old one.
	 * The angle is added anti-clockwise.
	 * 
	 * @param angle
	 * @author Malte
	 * @return
	 */
	Vector2 turnNew(float angle);
	
	
	/**
	 * Returns a new vector with the given 'angle'.
	 * Input and output vector are of equal length.
	 * 
	 * @param angle
	 * @author Malte
	 * @return
	 * @see Vector2#turn(float) turn
	 */
	Vector2 turnToNew(float angle);
	
	
	/**
	 * @param digits Number of digits to round at (negative values will be ignored!)
	 * @return A new {@link IVector2} containing the values of this rounded with the given number of digits
	 * @see Vector2#round(int)
	 * @author Gero
	 */
	IVector2 roundNew(int digits);
	
	
	/**
	 * @return The length of the vector.
	 *         <span style="white-space: nowrap; font-size:larger">
	 *         &radic;<span style="text-decoration:overline;">&nbsp;X� + Y�&nbsp;</span>
	 *         </span>
	 */
	float getLength2();
	
	
	/**
	 * @return Whether this is a Null-vector
	 */
	boolean isZeroVector();
	
	
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
	float getAngle();
	
	
	/**
	 * Returns the scalar product of 'this' and the given vector. (this * v)
	 * 
	 * @param v
	 * @return this * v
	 * @author Malte
	 */
	float scalarProduct(IVector2 v);
	
	
	/**
	 * does a projection of this onto v.
	 * the result is a vector in direction of v but with possibly different length (even negative)
	 * 
	 * @param v
	 * @return a vector in direction of v
	 * @author DanielW
	 */
	IVector2 projectToNew(IVector2 v);
	
	
	/**
	 * @return Whether the given {@link IVector2} nearly equals this.
	 * @author Gero
	 * @param vec
	 * @param tolerance
	 */
	boolean equals(IVector2 vec, float tolerance);
	
	
	/**
	 * Make all components positive.
	 * 
	 * @return Absolute vector.
	 */
	Vector2 absNew();
	
	
	/**
	 * Returns the surface normal to the given vector.
	 * It is turned by 90� clockwise and normalized.
	 * 
	 * @author Malte
	 * @return
	 */
	IVector2 getNormalVector();
	
	
	/**
	 * Get vector in saveable form. Use in XML configs!
	 * 
	 * @return x,y
	 */
	String getSaveableString();
	
	
	/**
	 * Check if given vector is similar to this one
	 * 
	 * @param vec vector to compare
	 * @param treshold how similar?
	 * @return
	 */
	boolean similar(IVector2 vec, float treshold);
	
	
	/**
	 * rotates a new created Vector2 around axis and returns the new object.
	 * 
	 * @param axis
	 * @param angle
	 * @return
	 */
	public Vector2 turnAroundNew(final IVector2 axis, final float angle);
	
}