/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.ellipse;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Interface for an ellipse
 * 
 * @see <a href="http://de.wikipedia.org/wiki/Ellipse">Ellipse (wikipedia)</a>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IEllipse extends I2DShape
{
	/**
	 * The center of the ellipse
	 * 
	 * @return
	 */
	IVector2 center();
	
	
	/**
	 * Distance between center and left/right apex ("Scheitel")
	 * 
	 * @return
	 */
	double getRadiusX();
	
	
	/**
	 * Distance between center and upper/lower apex ("Scheitel")
	 * 
	 * @return
	 */
	double getRadiusY();
	
	
	/**
	 * The angle, the ellipse is turned to.
	 * 
	 * @return angle in [rad] anti-clockwise
	 */
	double getTurnAngle();
	
	
	/**
	 * Get the focus ("Brennpunkt") in positive direction from center
	 * 
	 * @return
	 */
	IVector2 getFocusPositive();
	
	
	/**
	 * Get the focus ("Brennpunkt") in negative direction from center
	 * 
	 * @return
	 */
	IVector2 getFocusNegative();
	
	
	/**
	 * Get a vector pointing from center of ellipse to the positive focus
	 * 
	 * @return vector pointing from center to positive focus
	 */
	IVector2 getFocusFromCenter();
	
	
	/**
	 * Calculate the circumference ("Umfang") of the ellipse
	 *
	 * @return
	 */
	double getCircumference();
	
	
	/**
	 * Get the double of the greater radius which is the distance of F1P + PF2,
	 * where Fn are the focus points and P is a point on the curve
	 *
	 * @return
	 */
	double getDiameterMax();
	
	
	/**
	 * Step on the curve of this ellipse, starting on start and stepping clockwise (use negative value for
	 * anti-clockwise)
	 * 
	 * @param start must be a point on the curve or IllegalArgument will be thrown
	 * @param step clockwise
	 * @return
	 */
	IVector2 stepOnCurve(IVector2 start, double step);
}
