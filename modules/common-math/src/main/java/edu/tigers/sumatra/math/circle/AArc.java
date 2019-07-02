/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Abstract implementation of an arc
 */
@Persistent(version = 1)
abstract class AArc extends ACircular implements IArc
{
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return CircleMath.isPointInArc(this, point, margin);
	}
}
