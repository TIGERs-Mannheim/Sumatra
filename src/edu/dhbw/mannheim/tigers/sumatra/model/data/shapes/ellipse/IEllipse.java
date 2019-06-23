/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 17, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.I2DShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;


/**
 * Interface for an ellipse
 * @see <a href="http://de.wikipedia.org/wiki/Ellipse">Ellipse (wikipedia)</a>
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
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
	float getRadiusX();
	
	
	/**
	 * Distance between center and upper/lower apex ("Scheitel")
	 * 
	 * @return
	 */
	float getRadiusY();
	
	
	/**
	 * The angle, the ellipse is turned to.
	 * 
	 * @return angle in [rad] anti-clockwise
	 */
	float getTurnAngle();
	
	
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
	IVector2 stepOnCurve(IVector2 start, float step);
	
	
	/**
	 * Get the points of the given (endless) line that intersect the ellipse.
	 * The result can have 0-2 intersecting points
	 * 
	 * @see IEllipse#getIntersectingPoints(IVector2, IVector2)
	 * @param line
	 * @return
	 */
	List<IVector2> getIntersectingPoints(ILine line);
	
	
	/**
	 * Get the intersecting lines of the line between p1 and p2. p1 and p2 build a limited line
	 * The result can have 0-2 intersecting points
	 * 
	 * @see IEllipse#getIntersectingPoints(ILine)
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
	float getCircumference();
	
	
	/**
	 * Get the double of the greater radius which is the distance of F1P + PF2,
	 * where Fn are the focus points and P is a point on the curve
	 * 
	 * @return
	 */
	float getDiameterMax();
}
