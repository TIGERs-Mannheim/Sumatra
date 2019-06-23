/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.math.plane;

import java.util.Optional;

import edu.tigers.sumatra.math.vector.IVector3;


/**
 * A plane in 3D space.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public interface IPlane
{
	/**
	 * Returns the support vector of this plane. The support vector represents the starting point of the plane, i.e.
	 * where
	 * it is anchored.
	 *
	 * @return
	 * 			The support vector which may have a length of zero
	 */
	IVector3 supportVector();
	
	
	/**
	 * Returns the normal vector of this plane. The normal vector represents the direction the plane is facing.
	 *
	 * @return
	 * 			The normal vector which may have a length of zero
	 */
	IVector3 normalVector();
	
	
	/**
	 * Calculate intersection of a 3D line with this plane.
	 * 
	 * @param lineSupport
	 * @param lineDirection
	 * @return Optional intersection point.
	 */
	Optional<IVector3> lineIntersection(final IVector3 lineSupport, final IVector3 lineDirection);
}
