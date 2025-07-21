/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter.callbacks;

import edu.tigers.sumatra.math.vector.IVector2;


@FunctionalInterface
public interface MousePointTransformer
{
	IVector2 toGlobal(int dx, int dy);
}
