/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.function.Function;

import org.apache.commons.math3.linear.RealVector;
import org.json.simple.JSONArray;

import edu.tigers.sumatra.export.IJsonString;
import edu.tigers.sumatra.export.INumberListable;


/**
 * Generic Vector with flexible size
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IVector extends IJsonString, INumberListable
{
	/**
	 * @return The X part of the vector
	 */
	double x();
	
	
	/**
	 * @return The Y part of the vector
	 */
	double y();
	
	
	/**
	 * @return The Z part of the vector
	 */
	double z();
	
	
	/**
	 * @param i
	 * @return the value at index i
	 */
	double get(int i);
	
	
	/**
	 * @return
	 */
	int getNumDimensions();
	
	
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
	 * Multiplies each element of the given 'vector' with the corresponding element of 'this' and returns the result in a
	 * new object, while 'this' and 'vector' stay
	 * unaffected.
	 * 
	 * @param vector
	 * @return The result as a new object
	 */
	IVector multiplyNew(IVector vector);
	
	
	/**
	 * Multiply with constant factor.
	 * 
	 * @param f factor
	 * @return New vector with x, y and z multiplied with f
	 */
	IVector multiplyNew(double f);
	
	
	/**
	 * Returns the normalized Vector. 'this' stays unaffected!!!
	 * 
	 * @author Malte
	 * @return
	 */
	IVector normalizeNew();
	
	
	/**
	 * Make all components positive.
	 * 
	 * @return Absolute vector.
	 */
	IVector absNew();
	
	
	/**
	 * @param function
	 * @return
	 */
	IVector applyNew(Function<Double, Double> function);
	
	
	/**
	 * @return The length of the vector.
	 */
	double getLength();
	
	
	/**
	 * @return The 2dim-length of the vector.
	 */
	double getLength2();
	
	
	/**
	 * @return Whether this is a Null-vector
	 */
	boolean isZeroVector();
	
	
	/**
	 * Are all elements of this vector finite? (not NaN or inf)
	 * 
	 * @return
	 */
	boolean isFinite();
	
	
	/**
	 * @param vec some vector
	 * @param tolerance max distance
	 * @return true, if the given vector is close to this vector with a given tolerance
	 */
	boolean isCloseTo(IVector vec, double tolerance);
	
	
	/**
	 * @param vec some vector
	 * @return true, if the given vector is close to this vector
	 */
	boolean isCloseTo(final IVector vec);
	
	
	/**
	 * Get vector in saveable form. Use in XML configs!
	 * 
	 * @return x,y
	 */
	String getSaveableString();
	
	
	/**
	 * Return {x, y, z, ...}
	 * 
	 * @return n element array
	 */
	double[] toArray();
	
	
	/**
	 * Get XY-component vector.
	 * 
	 * @return XY vector
	 */
	IVector2 getXYVector();
	
	
	/**
	 * Get XY-component vector.
	 * 
	 * @return XY vector
	 */
	IVector3 getXYZVector();
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return
	 */
	RealVector toRealVector();
	
	
	/**
	 * Return a JSON array.
	 * 
	 * @return
	 */
	JSONArray toJSONArray();
}
