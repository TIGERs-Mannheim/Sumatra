/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector;

/**
 * Generic Vector with flexible size
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IVector
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
	 * @return The Z part of the vector
	 */
	float z();
	
	
	/**
	 * @return The W part of the vector (4th element!)
	 */
	float w();
	
	
	/**
	 * @param i
	 * @return the value at index i
	 */
	float get(int i);
	
	
	/**
	 * Adds 'this' and 'vector' and returns the result in a new object, while 'this' and 'vector' stay unaffected.
	 * 
	 * @param vector
	 * @return The result as a new object
	 */
	IVector addNew(IVector vector);
	
	
	/**
	 * Subtracts the given 'vector' from 'this' and returns the result in a new object, while 'this' and 'vector' stay
	 * unaffected.
	 * 
	 * @param vector
	 * @return The result as a new object
	 */
	IVector subtractNew(IVector vector);
	
	
	/**
	 * Multiply with constant factor.
	 * 
	 * @param f factor
	 * @return New vector with x, y and z multiplied with f
	 */
	IVector multiplyNew(float f);
	
	
	/**
	 * Turn a new vector rotated around Z axis.
	 * 
	 * @param angle Angle in [rad]
	 * @return Rotated vector
	 */
	IVector turnAroundZNew(float angle);
	
	
	/**
	 * @return The length of the vector.
	 *         <span style="white-space: nowrap; font-size:larger">
	 *         &radic;<span style="text-decoration:overline;">&nbsp;X� + Y�&nbsp;</span>
	 *         </span>
	 */
	float getLength();
	
	
	/**
	 * Check if vectors are equal.
	 * Warning: Floating point comparison
	 * 
	 * @param vec
	 * @return true if equal, false otherwise
	 */
	boolean equalsVector(IVector vec);
	
	
	/**
	 * @param vec
	 * @param tolerance
	 * @return Whether the given {@link IVector} nearly equals this.
	 */
	boolean equalsVector(IVector vec, float tolerance);
	
	
	/**
	 * Check if vectors are equal with individual tolerances.
	 * Tolerances are always positive!
	 * 
	 * @param vec vector to check
	 * @param dx x tolerance
	 * @param dy y tolerance
	 * @param dz z tolerance
	 * @return true if equal within tolerances, false otherwise
	 */
	boolean equals(IVector vec, float dx, float dy, float dz);
	
	
	/**
	 * Return {x, y, z}
	 * 
	 * @return 3 element array
	 * @author AndreR
	 */
	float[] toArray();
	
	
	/**
	 * Return (double){x, y, z}
	 * 
	 * @return 3 element array
	 * @author AndreR
	 */
	double[] toDoubleArray();
	
	
	/**
	 * Get XY-component vector.
	 * 
	 * @return XY vector
	 */
	IVector getXYVector();
	
	
	/**
	 * @return
	 */
	IVector mirrorXY();
}
