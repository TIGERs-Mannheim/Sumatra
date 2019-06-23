/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.08.2012
 * Author(s): Gero, AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector;

import org.apache.commons.configuration.HierarchicalConfiguration;


/**
 * This interface allows transparent access to {@link Vector3} and {@link Vector3f}
 * 
 * @see Vector3
 * @see Vector3f
 * 
 * @see Vector2
 * @see Vector2f
 * 
 * @author Gero, AndreR
 * 
 */
public interface IVector3
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
	 * Adds 'this' and 'vector' and returns the result in a new object, while 'this' and 'vector' stay unaffected.
	 * 
	 * @param vector
	 * @return The result as a new object
	 */
	Vector3 addNew(IVector3 vector);
	
	
	/**
	 * Subtracts the given 'vector' from 'this' and returns the result in a new object, while 'this' and 'vector' stay
	 * unaffected.
	 * 
	 * @param vector
	 * @return The result as a new object
	 */
	Vector3 subtractNew(IVector3 vector);
	
	
	/**
	 * Multiply with constant factor.
	 * 
	 * @param f factor
	 * @return New vector with x, y and z multiplied with f
	 */
	Vector3 multiplyNew(float f);
	
	
	/**
	 * Turn a new vector rotated around Z axis.
	 * 
	 * @param angle Angle in [rad]
	 * @return Rotated vector
	 */
	Vector3 turnAroundZNew(float angle);
	
	
	/**
	 * @return The length of the vector.
	 *         <span style="white-space: nowrap; font-size:larger">
	 *         &radic;<span style="text-decoration:overline;">&nbsp;X� + Y� + Z�&nbsp;</span>
	 *         </span>
	 */
	float getLength3();
	
	
	/**
	 * @return The length of the vector.
	 *         <span style="white-space: nowrap; font-size:larger">
	 *         &radic;<span style="text-decoration:overline;">&nbsp;X� + Y�&nbsp;</span>
	 *         </span>
	 */
	float getLength2();
	
	
	/**
	 * Check if vectors are equal.
	 * Warning: Floating point comparison
	 * 
	 * @param vec
	 * @return true if equal, false otherwise
	 */
	boolean equalsVector(IVector3 vec);
	
	
	/**
	 * @param vec
	 * @param tolerance
	 * @return Whether the given {@link IVector3} nearly equals this.
	 */
	boolean equalsVector(IVector3 vec, float tolerance);
	
	
	/**
	 * Check if vectors are equal with individual tolerances.
	 * 
	 * Tolerances are always positive!
	 * 
	 * @param vec vector to check
	 * @param dx x tolerance
	 * @param dy y tolerance
	 * @param dz z tolerance
	 * @return true if equal within tolerances, false otherwise
	 */
	boolean equals(IVector3 vec, float dx, float dy, float dz);
	
	
	/**
	 * Return {x, y, z}
	 * 
	 * @return 3 element array
	 * 
	 * @author AndreR
	 */
	float[] toArray();
	
	
	/**
	 * Return (double){x, y, z}
	 * 
	 * @return 3 element array
	 * 
	 * @author AndreR
	 */
	double[] toDoubleArray();
	
	
	/**
	 * Get XY-component vector.
	 * 
	 * @return XY vector
	 */
	Vector2 getXYVector();
	
	
	/**
	 * Get a configuration node for this vector.
	 * Node consists of x, y and z.
	 * 
	 * @return
	 */
	HierarchicalConfiguration getConfiguration();
}
