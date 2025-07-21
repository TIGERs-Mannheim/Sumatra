/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import java.util.Optional;


/**
 * Abstract base class for all line types which implements the generic methods that are consistent for all types.
 *
 * @author Lukas Magel
 */
abstract class ALine implements ILineBase
{


	@Override
	public Optional<Double> getSlope()
	{
		return LineMath.getSlope(this);
	}


	@Override
	public Optional<Double> getAngle()
	{
		return LineMath.getAngle(this);
	}


	@Override
	public boolean isHorizontal()
	{
		return isValid() && directionVector().isHorizontal();
	}


	@Override
	public boolean isVertical()
	{
		return isValid() && directionVector().isVertical();
	}


	@Override
	public boolean isParallelTo(final ILineBase other)
	{
		return isValid()
				&& other.isValid()
				&& directionVector().isParallelTo(other.directionVector());
	}
}
