/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

/**
 * This interface allows transparent access to {@link Vector2} and {@link Vector2f}
 * 
 * @see Vector2
 * @see Vector2f
 * 
 * @author Gero
 * 
 */
public interface IVector2
{
	/**
	 * @return The X part of the vector
	 */
	public abstract float x();
	

	/**
	 * @return The Y part of the vector
	 */
	public abstract float y();
	
	
	/**
	 * Returns the normalized Vector. 'this' stays unaffected!!!
	 * 
	 * @author Malte
	 */
	public Vector2 normalizeNew();

	/**
	 * Adds 'this' and 'vector' and returns the result in a new object, while 'this' and 'vector' stay unaffected.
	 * 
	 * @param vector
	 * @return The result as a new object
	 */
	public Vector2 addNew(IVector2 vector);
	

	/**
	 * Subtracts the given 'vector' from 'this' and returns the result in a new object, while 'this' and 'vector' stay
	 * unaffected.
	 * 
	 * @param vector
	 * @return The result as a new object
	 */
	public Vector2 subtractNew(IVector2 vector);
	

	/**
	 * Multiplies 'this' by the given 'factor' and returns the result in a new object, while 'this' stays unaffected
	 * 
	 * @param factor
	 */
	public Vector2 multiplyNew(float factor);
	

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
	 */
	public Vector2 scaleToNew(float newLength);
	

	/**
	 * Turns 'this' with the given 'angle' and returns the result in a new object,
	 * while 'this' stay unaffected. The new vector has the same length as the old one.
	 * The angle is added anti-clockwise.
	 * 
	 * @param angle
	 * @author Malte
	 */
	public Vector2 turnNew(float angle);
	

	/**
	 * Returns a new vector with the given 'angle'.
	 * Input and output vector are of equal length.
	 * 
	 * @param angle
	 * @author Malte
	 * @see Vector2#turn(float) turn
	 */
	public Vector2 turnToNew(float angle);
	

	/**
	 * @param digits Number of digits to round at (negative values will be ignored!)
	 * @return A new {@link IVector2} containing the values of this rounded with the given number of digits
	 * @see Vector2#round(int)
	 * @author Gero
	 */
	public IVector2 roundNew(int digits);
	

	/**
	 * @return The length of the vector.
	 *         <span style="white-space: nowrap; font-size:larger">
	 *         &radic;<span style="text-decoration:overline;">&nbsp;X² + Y²&nbsp;</span>
	 *         </span>
	 */
	public float getLength2();
	

	/**
	 * @return Whether this is a Null-vector
	 */
	public boolean isZeroVector();
	

	public boolean isVertical();
	

	public boolean isHorizontal();
	

	/**
	 * Calculates the angle between x-Axis and the given vector.<br>
	 * Angle is calculated anti-clockwise in range (-pi to pi).
	 * If you want to know how the angle is added, look here:
	 * <a href="http://tigers-mannheim.de/trac/wiki/Informatik#Spiefeld">Field.</a>
	 * If you input a zero-vector, an exeption is thrown.
	 * 
	 * @author Malte
	 */
	public float getAngle();
	

	/**
	 * Returns the scalar product of 'this' and the given vector. (this * v)
	 * 
	 * @param v
	 * @return this * v
	 * @author Malte
	 */
	public float scalarProduct(IVector2 v);
	

	/**
	 * does a projection of this onto v.
	 * the result is a vector in direction of v but with possibly different length (even negative)
	 * @param v
	 * @return a vector in direction of v
	 * @author DanielW
	 */
	public IVector2 projectToNew(IVector2 v);
	

	/**
	 * {@link Object#equals(Object)} for {@link IVector2}s
	 * 
	 * @param vec
	 * @return
	 */
	public boolean equals(IVector2 vec);
	

	/**
	 * @return Whether the given {@link IVector2} nearly equals this.
	 * 
	 * @author Gero
	 */
	public boolean equals(IVector2 vec, float tolerance);
	
	/**
	 * Make all components positive.
	 * 
	 * @return Absolute vector.
	 */
	public Vector2 absNew();


	/**
	 * Returns the surface normal to the given vector.
	 * It is turned by 90° clockwise and normalized.
	 * 
	 * @author Malte
	 */
	public abstract IVector2 getNormalVector();
	
	/**
	 * Get vector in saveable form. Use in XML configs!
	 * 
	 * @return x,y
	 */
	public String getSaveableString();
}
