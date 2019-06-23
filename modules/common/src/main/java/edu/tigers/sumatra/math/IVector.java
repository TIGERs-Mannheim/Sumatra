/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.util.function.Function;

import org.apache.commons.math3.linear.RealVector;

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
	default double z()
	{
		return 0;
	}
	
	
	/**
	 * @return The W part of the vector (4th element!)
	 */
	default double w()
	{
		return 0;
	}
	
	
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
	 * @return Whether the given {@link IVector} nearly equals this.
	 * @param vec
	 * @param tolerance
	 */
	boolean equals(IVector vec, double tolerance);
	
	
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
	 * Return (double){x, y, z, ...}
	 * 
	 * @return n element array
	 */
	double[] toDoubleArray();
	
	
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
}
