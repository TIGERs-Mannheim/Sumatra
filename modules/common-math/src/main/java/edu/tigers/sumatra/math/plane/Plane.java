/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.math.plane;

import java.util.Optional;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * Primary plane implementation.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class Plane implements IPlane
{
	private final IVector3 supportVector;
	private final IVector3 normalVector;
	
	
	private Plane(final IVector3 supportVector, final IVector3 normalVector)
	{
		this.supportVector = supportVector;
		this.normalVector = normalVector;
	}
	
	
	/**
	 * Creates a new plane instance which uses the specified {@code supportVector} and {@code normalVector}.
	 *
	 * @param supportVector
	 *           The support vector to use for the plane
	 * @param normalVector
	 *           The normal vector to use for the plane
	 * @return
	 * 			A new plane instance which is defined by the two parameters
	 */
	public static IPlane fromNormal(final IVector3 supportVector, final IVector3 normalVector)
	{
		return new Plane(supportVector, normalVector);
	}
	
	
	@Override
	public IVector3 supportVector()
	{
		return supportVector;
	}
	
	
	@Override
	public IVector3 normalVector()
	{
		return normalVector;
	}
	
	
	@Override
	public Optional<IVector3> lineIntersection(final IVector3 lineSupport, final IVector3 lineDirection)
	{
		double numerator = supportVector.subtractNew(lineSupport).dotNew(normalVector);
		double denominator = lineDirection.dotNew(normalVector);
		
		if (SumatraMath.isZero(numerator) || SumatraMath.isZero(denominator))
		{
			return Optional.empty();
		}
		
		double lambda = numerator / denominator;
		
		return Optional.of(lineDirection.multiplyNew(lambda).add(lineSupport));
	}
}
