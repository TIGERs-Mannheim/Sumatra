/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.planarcurve;

/**
 * Defines different types of planar (2D) curve segments.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public enum EPlanarCurveSegmentType
{
	/** position */
	POINT,
	/** position, velocity */
	FIRST_ORDER,
	/** position, velocity, acceleration */
	SECOND_ORDER,
}
