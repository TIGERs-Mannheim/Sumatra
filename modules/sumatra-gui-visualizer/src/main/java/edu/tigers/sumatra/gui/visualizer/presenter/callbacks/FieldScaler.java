/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter.callbacks;

import java.awt.Point;


@FunctionalInterface
public interface FieldScaler
{
	void scale(Point origin, double factor);
}
