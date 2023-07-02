/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.math.botshape;

import edu.tigers.sumatra.math.circle.ICircular;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Represents a bot shape consisting of a circle with a flat side.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public interface IBotShape extends ICircular
{
	/**
	 * Get center to dribbler distance.
	 * 
	 * @return
	 */
	double getCenter2Dribbler();
	
	
	/**
	 * Get orientation.
	 * 
	 * @return
	 */
	double getOrientation();
	
	
	/**
	 * Calculates the (center) position of the dribbler/kicker
	 *
	 * @return kicker center position
	 */
	IVector2 getKickerCenterPos();
	
	
	/**
	 * Calculates the line of the robot's flat front.
	 * 
	 * @return
	 */
	ILineSegment getKickerLine();
	
	
	/**
	 * Calculate the width of the kicker (flat robot side)
	 * 
	 * @return
	 */
	double getKickerWidth();
	
	
	/**
	 * Check if a given point is in the rectangle formed by the kicker line and the given <i>zoneLength</i>.
	 * 
	 * @param point Point to check.
	 * @param zoneLength max distance from kicker line in [mm]
	 * @return
	 */
	boolean isPointInKickerZone(IVector2 point, double zoneLength);
	
	
	/**
	 * Check if a given point is in the rectangle formed by the kicker line (extended to zoneWidth) and the given
	 * <i>zoneLength</i>.
	 * 
	 * @param point Point to check.
	 * @param zoneLength max distance from kicker line in [mm]
	 * @param zoneWidth extended/shrinked width of the kicker line.
	 * @return
	 */
	boolean isPointInKickerZone(IVector2 point, double zoneLength, double zoneWidth);
	
	
	@Override
	IBotShape withMargin(final double margin);
}
