/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.callbacks;

import edu.tigers.sumatra.math.vector.IVector2;


@FunctionalInterface
public interface MousePointTransformer
{
	IVector2 toGlobal(int dx, int dy);
}
