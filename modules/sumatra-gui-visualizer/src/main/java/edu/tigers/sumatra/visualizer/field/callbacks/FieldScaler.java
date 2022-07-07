/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.callbacks;

import java.awt.Point;


@FunctionalInterface
public interface FieldScaler
{
	void scale(Point origin, double factor);
}
