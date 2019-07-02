/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.angle;

/**
 * Some angular measure. Agnostic to the unit.
 * Available are Radian, Degree and Turns (number of rotations).<br>
 * Use {@link IAngle} for Implementations that are always normalized
 * (bound in the range [-pi..pi[) and for unbound angles (most likely
 * rotations) use {@link IRotation}.
 * 
 * @author DominikE
 */
public interface IAngularMeasure
{
	/**
	 * @return a double representing this angle as radian
	 */
	double asRad();
	
	
	/**
	 * @return a double representing this angle as degree
	 */
	double asDeg();
	
	
	/**
	 * @return a double representing this angle as rotations (one full turn equals 1)
	 */
	double asRotation();
	
	
	/**
	 * @param angle the angle to add to this angle
	 * @return a new angle that represents the sum
	 */
	IAngularMeasure add(IAngularMeasure angle);
	
	
	/**
	 * @param angle the angle to add to this angle
	 * @return a new angle that represents the sum
	 */
	IAngularMeasure add(double angle);
	
	
	/**
	 * @param angle the angle to subtract from this angle
	 * @return a new angle that represents the difference
	 */
	IAngularMeasure subtract(IAngularMeasure angle);
	
	
	/**
	 * @param angle the angle to subtract from this angle
	 * @return a new angle that represents the difference
	 */
	IAngularMeasure subtract(double angle);
	
	
	/**
	 * @param factor the factor to multiply with
	 * @return a new angle representing the multiplication
	 */
	IAngularMeasure multiply(double factor);
	
	
	/**
	 * @return the sine of this angle
	 */
	double sin();
	
	
	/**
	 * @return the cosine of this angle
	 */
	double cos();
	
	
	/**
	 * @return the tangent of this angle
	 */
	double tan();
}
