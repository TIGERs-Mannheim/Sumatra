/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 17, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.ellipse;

import java.util.List;

import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.I2DShape;


/**
 * Interface for an ellipse
 * 
 * @see <a href="http://de.wikipedia.org/wiki/Ellipse">Ellipse (wikipedia)</a>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IEllipse extends I2DShape
{
	/**
	 * Returns the apex ("Scheitel") of the specified type
	 * 
	 * @param apexType
	 * @return absolute vector to apex
	 */
	IVector2 getApex(EApexType apexType);
	
	
	/**
	 * The center of the ellipse
	 * 
	 * @return
	 */
	IVector2 getCenter();
	
	
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
	 * Step on the curve of this ellipse, starting on start and stepping clockwise (use negative value for
	 * anti-clockwise)
	 * 
	 * @param start must be a point on the curve or IllegalArgument will be thrown
	 * @param step clockwise
	 * @return
	 */
	IVector2 stepOnCurve(IVector2 start, double step);
	
	
	/**
	 * Get the intersecting lines of the line between p1 and p2. p1 and p2 build a limited line
	 * The result can have 0-2 intersecting points
	 * 
	 * @see IEllipse#lineIntersections(ILine)
	 * @param p1
	 * @param p2
	 * @return
	 */
	List<IVector2> getIntersectingPoints(IVector2 p1, IVector2 p2);
	
	
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
}
